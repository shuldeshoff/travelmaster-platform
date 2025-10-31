output "kubernetes_cluster_id" {
  description = "Kubernetes cluster ID"
  value       = yandex_kubernetes_cluster.travelmaster_cluster.id
}

output "kubernetes_cluster_endpoint" {
  description = "Kubernetes cluster endpoint"
  value       = yandex_kubernetes_cluster.travelmaster_cluster.master[0].external_v4_endpoint
}

output "postgresql_cluster_id" {
  description = "PostgreSQL cluster ID"
  value       = yandex_mdb_postgresql_cluster.travelmaster_db.id
}

output "postgresql_host" {
  description = "PostgreSQL host"
  value       = yandex_mdb_postgresql_cluster.travelmaster_db.host[0].fqdn
}

output "kafka_cluster_id" {
  description = "Kafka cluster ID"
  value       = yandex_mdb_kafka_cluster.travelmaster_kafka.id
}

output "kafka_brokers" {
  description = "Kafka broker addresses"
  value       = [for host in yandex_mdb_kafka_cluster.travelmaster_kafka.host : "${host.name}:9092"]
}

output "network_id" {
  description = "VPC Network ID"
  value       = yandex_vpc_network.travelmaster_network.id
}

output "subnet_id" {
  description = "Subnet ID"
  value       = yandex_vpc_subnet.travelmaster_subnet.id
}

output "dns_zone_id" {
  description = "DNS Zone ID"
  value       = yandex_dns_zone.travelmaster_zone.id
}

output "api_endpoint" {
  description = "API endpoint URL"
  value       = "https://api.${var.domain_name}"
}

output "kubeconfig_command" {
  description = "Command to get kubeconfig"
  value       = "yc managed-kubernetes cluster get-credentials ${yandex_kubernetes_cluster.travelmaster_cluster.name} --external"
}

