terraform {
  required_version = ">= 1.0"
  
  required_providers {
    yandex = {
      source  = "yandex-cloud/yandex"
      version = "~> 0.100"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
  }
  
  backend "s3" {
    endpoint   = "storage.yandexcloud.net"
    bucket     = "travelmaster-terraform-state"
    region     = "ru-central1"
    key        = "terraform.tfstate"
    
    skip_region_validation      = true
    skip_credentials_validation = true
  }
}

provider "yandex" {
  token     = var.yandex_cloud_token
  cloud_id  = var.yandex_cloud_id
  folder_id = var.yandex_folder_id
  zone      = var.yandex_zone
}

# VPC Network
resource "yandex_vpc_network" "travelmaster_network" {
  name        = "travelmaster-network"
  description = "Network for TravelMaster Platform"
}

# Subnet
resource "yandex_vpc_subnet" "travelmaster_subnet" {
  name           = "travelmaster-subnet"
  zone           = var.yandex_zone
  network_id     = yandex_vpc_network.travelmaster_network.id
  v4_cidr_blocks = ["10.0.0.0/16"]
}

# Kubernetes Cluster
resource "yandex_kubernetes_cluster" "travelmaster_cluster" {
  name        = "travelmaster-cluster"
  description = "Kubernetes cluster for TravelMaster Platform"
  
  network_id = yandex_vpc_network.travelmaster_network.id
  
  master {
    version = var.kubernetes_version
    
    zonal {
      zone      = yandex_vpc_subnet.travelmaster_subnet.zone
      subnet_id = yandex_vpc_subnet.travelmaster_subnet.id
    }
    
    public_ip = true
    
    security_group_ids = [yandex_vpc_security_group.k8s_master_sg.id]
    
    maintenance_policy {
      auto_upgrade = true
      
      maintenance_window {
        start_time = "03:00"
        duration   = "4h"
      }
    }
  }
  
  service_account_id      = yandex_iam_service_account.k8s_sa.id
  node_service_account_id = yandex_iam_service_account.k8s_node_sa.id
  
  release_channel = "STABLE"
  
  depends_on = [
    yandex_resourcemanager_folder_iam_member.k8s_clusters_agent,
    yandex_resourcemanager_folder_iam_member.vpc_public_admin,
    yandex_resourcemanager_folder_iam_member.images_puller
  ]
}

# Node Group
resource "yandex_kubernetes_node_group" "travelmaster_nodes" {
  cluster_id  = yandex_kubernetes_cluster.travelmaster_cluster.id
  name        = "travelmaster-nodes"
  description = "Node group for TravelMaster services"
  version     = var.kubernetes_version
  
  instance_template {
    platform_id = "standard-v3"
    
    network_interface {
      nat                = true
      subnet_ids         = [yandex_vpc_subnet.travelmaster_subnet.id]
      security_group_ids = [yandex_vpc_security_group.k8s_nodes_sg.id]
    }
    
    resources {
      memory = 8
      cores  = 4
    }
    
    boot_disk {
      type = "network-ssd"
      size = 64
    }
    
    scheduling_policy {
      preemptible = false
    }
    
    container_runtime {
      type = "containerd"
    }
  }
  
  scale_policy {
    auto_scale {
      min     = 3
      max     = 10
      initial = 3
    }
  }
  
  allocation_policy {
    location {
      zone = var.yandex_zone
    }
  }
  
  maintenance_policy {
    auto_upgrade = true
    auto_repair  = true
    
    maintenance_window {
      start_time = "03:00"
      duration   = "4h"
    }
  }
}

# Managed PostgreSQL
resource "yandex_mdb_postgresql_cluster" "travelmaster_db" {
  name        = "travelmaster-postgresql"
  description = "PostgreSQL cluster for TravelMaster"
  environment = var.environment
  network_id  = yandex_vpc_network.travelmaster_network.id
  
  config {
    version = "14"
    resources {
      resource_preset_id = "s2.small"
      disk_type_id       = "network-ssd"
      disk_size          = 100
    }
    
    postgresql_config = {
      max_connections                = 200
      shared_buffers                 = 2147483648  # 2GB
      effective_cache_size           = 6442450944  # 6GB
      maintenance_work_mem           = 536870912   # 512MB
      checkpoint_completion_target   = 0.9
      wal_buffers                    = 16777216    # 16MB
      default_statistics_target      = 100
      random_page_cost               = 1.1
      effective_io_concurrency       = 200
      work_mem                       = 10485760    # 10MB
      min_wal_size                   = 1073741824  # 1GB
      max_wal_size                   = 4294967296  # 4GB
    }
  }
  
  host {
    zone      = var.yandex_zone
    subnet_id = yandex_vpc_subnet.travelmaster_subnet.id
  }
}

# PostgreSQL Database
resource "yandex_mdb_postgresql_database" "databases" {
  for_each   = toset([
    "travelmaster_user",
    "travelmaster_trip",
    "travelmaster_booking",
    "travelmaster_payment",
    "travelmaster_notification",
    "travelmaster_analytics"
  ])
  
  cluster_id = yandex_mdb_postgresql_cluster.travelmaster_db.id
  name       = each.key
  owner      = yandex_mdb_postgresql_user.db_user.name
}

# PostgreSQL User
resource "yandex_mdb_postgresql_user" "db_user" {
  cluster_id = yandex_mdb_postgresql_cluster.travelmaster_db.id
  name       = var.db_username
  password   = var.db_password
  
  permission {
    database_name = "travelmaster_user"
  }
  permission {
    database_name = "travelmaster_trip"
  }
  permission {
    database_name = "travelmaster_booking"
  }
  permission {
    database_name = "travelmaster_payment"
  }
  permission {
    database_name = "travelmaster_notification"
  }
  permission {
    database_name = "travelmaster_analytics"
  }
}

# Managed Kafka
resource "yandex_mdb_kafka_cluster" "travelmaster_kafka" {
  name        = "travelmaster-kafka"
  description = "Kafka cluster for TravelMaster"
  environment = var.environment
  network_id  = yandex_vpc_network.travelmaster_network.id
  
  config {
    version          = "3.5"
    brokers_count    = 1
    zones            = [var.yandex_zone]
    assign_public_ip = false
    
    kafka {
      resources {
        resource_preset_id = "s2.small"
        disk_type_id       = "network-ssd"
        disk_size          = 50
      }
    }
  }
}

# Kafka Topics
resource "yandex_mdb_kafka_topic" "topics" {
  for_each = toset([
    "booking-events",
    "payment-events",
    "notification-events"
  ])
  
  cluster_id         = yandex_mdb_kafka_cluster.travelmaster_kafka.id
  name               = each.key
  partitions         = 3
  replication_factor = 1
  
  topic_config {
    compression_type = "gzip"
    retention_ms     = 604800000  # 7 days
  }
}

# Service Account for K8s
resource "yandex_iam_service_account" "k8s_sa" {
  name        = "k8s-cluster-sa"
  description = "Service account for Kubernetes cluster"
}

resource "yandex_iam_service_account" "k8s_node_sa" {
  name        = "k8s-node-sa"
  description = "Service account for Kubernetes nodes"
}

# IAM bindings
resource "yandex_resourcemanager_folder_iam_member" "k8s_clusters_agent" {
  folder_id = var.yandex_folder_id
  role      = "k8s.clusters.agent"
  member    = "serviceAccount:${yandex_iam_service_account.k8s_sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "vpc_public_admin" {
  folder_id = var.yandex_folder_id
  role      = "vpc.publicAdmin"
  member    = "serviceAccount:${yandex_iam_service_account.k8s_sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "images_puller" {
  folder_id = var.yandex_folder_id
  role      = "container-registry.images.puller"
  member    = "serviceAccount:${yandex_iam_service_account.k8s_node_sa.id}"
}

# Security Groups
resource "yandex_vpc_security_group" "k8s_master_sg" {
  name        = "k8s-master-sg"
  description = "Security group for K8s master"
  network_id  = yandex_vpc_network.travelmaster_network.id
  
  ingress {
    protocol       = "TCP"
    description    = "Kubernetes API"
    v4_cidr_blocks = ["0.0.0.0/0"]
    port           = 6443
  }
  
  ingress {
    protocol       = "TCP"
    description    = "Kubernetes API alt"
    v4_cidr_blocks = ["0.0.0.0/0"]
    port           = 443
  }
  
  egress {
    protocol       = "ANY"
    description    = "Allow all outbound"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "yandex_vpc_security_group" "k8s_nodes_sg" {
  name        = "k8s-nodes-sg"
  description = "Security group for K8s nodes"
  network_id  = yandex_vpc_network.travelmaster_network.id
  
  ingress {
    protocol       = "TCP"
    description    = "Allow pod-to-pod"
    v4_cidr_blocks = ["10.0.0.0/16"]
    from_port      = 0
    to_port        = 65535
  }
  
  ingress {
    protocol       = "TCP"
    description    = "NodePort Services"
    v4_cidr_blocks = ["0.0.0.0/0"]
    from_port      = 30000
    to_port        = 32767
  }
  
  egress {
    protocol       = "ANY"
    description    = "Allow all outbound"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
}

# DNS Zone
resource "yandex_dns_zone" "travelmaster_zone" {
  name        = "travelmaster-zone"
  description = "Public DNS zone for TravelMaster"
  
  zone             = "${var.domain_name}."
  public           = true
}

# DNS Record for API
resource "yandex_dns_recordset" "api_record" {
  zone_id = yandex_dns_zone.travelmaster_zone.id
  name    = "api.${var.domain_name}."
  type    = "A"
  ttl     = 300
  data    = [yandex_kubernetes_cluster.travelmaster_cluster.master[0].external_v4_address]
}

