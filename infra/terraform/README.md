# TravelMaster Infrastructure as Code (Terraform)

Terraform конфигурация для развертывания TravelMaster Platform в Yandex Cloud.

## 📋 Содержание

- [Компоненты](#компоненты)
- [Требования](#требования)
- [Быстрый старт](#быстрый-старт)
- [Конфигурация](#конфигурация)
- [Развертывание](#развертывание)
- [Управление](#управление)

---

## 🏗️ Компоненты

Инфраструктура включает:

### Kubernetes
- **Managed Kubernetes Cluster** (Yandex Managed Service for Kubernetes)
  - Master: zonal, public IP
  - Node Group: 3-10 nodes, auto-scaling
  - Platform: standard-v3 (4 vCPU, 8 GB RAM)
  - Storage: 64 GB SSD per node

### Databases
- **PostgreSQL 14** (Managed PostgreSQL)
  - 6 databases (по одной на сервис)
  - s2.small preset (2 vCPU, 8 GB RAM)
  - 100 GB SSD storage
  - Automated backups

### Messaging
- **Apache Kafka 3.5** (Managed Kafka)
  - 1 broker
  - 3 topics (booking, payment, notification)
  - 50 GB SSD storage

### Networking
- **VPC Network**: 10.0.0.0/16
- **Security Groups**: master, nodes
- **DNS Zone**: public zone для домена

---

## 📦 Требования

### Локальные инструменты

```bash
# Terraform >= 1.0
terraform version

# Yandex Cloud CLI
yc version

# kubectl
kubectl version --client
```

### Yandex Cloud

1. **Аккаунт**: Yandex Cloud аккаунт
2. **OAuth Token**: [Получить токен](https://oauth.yandex.ru/authorize?response_type=token&client_id=1a6990aa636648e9b2ef855fa7bec2fb)
3. **Cloud/Folder ID**: Cloud ID и Folder ID
4. **Квоты**: Проверить квоты для K8s, PostgreSQL, Kafka

---

## 🚀 Быстрый старт

### 1. Подготовка

```bash
cd infra/terraform

# Скопировать пример конфигурации
cp terraform.tfvars.example terraform.tfvars

# Отредактировать переменные
vim terraform.tfvars
```

### 2. Инициализация

```bash
# Инициализировать Terraform
terraform init

# Проверить план
terraform plan
```

### 3. Развертывание

```bash
# Применить конфигурацию
terraform apply

# Подтвердить: yes
```

**⏱️ Время развертывания**: ~15-20 минут

### 4. Получить kubeconfig

```bash
# Получить credentials для kubectl
yc managed-kubernetes cluster get-credentials travelmaster-cluster --external

# Проверить подключение
kubectl cluster-info
kubectl get nodes
```

---

## ⚙️ Конфигурация

### terraform.tfvars

```hcl
# Yandex Cloud
yandex_cloud_token  = "YOUR_TOKEN"
yandex_cloud_id     = "b1g..."
yandex_folder_id    = "b1g..."
yandex_zone         = "ru-central1-a"

# Kubernetes
kubernetes_version = "1.28"
environment        = "PRODUCTION"

# Database
db_username = "travelmaster_user"
db_password = "STRONG_PASSWORD_HERE"

# Domain
domain_name = "travelmaster.example.com"

# Backups
enable_backup           = true
backup_retention_days   = 7
```

### Переменные окружения (альтернатива)

```bash
export TF_VAR_yandex_cloud_token="YOUR_TOKEN"
export TF_VAR_yandex_cloud_id="b1g..."
export TF_VAR_yandex_folder_id="b1g..."
export TF_VAR_db_password="STRONG_PASSWORD"
```

---

## 🔧 Управление

### Обновление инфраструктуры

```bash
# Изменить переменные в terraform.tfvars
vim terraform.tfvars

# Проверить изменения
terraform plan

# Применить
terraform apply
```

### Масштабирование

#### Увеличить количество нод

Изменить в `main.tf`:
```hcl
scale_policy {
  auto_scale {
    min     = 5  # было 3
    max     = 20 # было 10
    initial = 5  # было 3
  }
}
```

#### Увеличить ресурсы PostgreSQL

Изменить preset в `main.tf`:
```hcl
resources {
  resource_preset_id = "s2.medium"  # было s2.small
  disk_size          = 200          # было 100
}
```

### Бэкапы

```bash
# Список бэкапов PostgreSQL
yc managed-postgresql backup list --cluster-id=<cluster-id>

# Восстановление из бэкапа
yc managed-postgresql cluster restore \
  --backup-id=<backup-id> \
  --name=travelmaster-postgresql-restored
```

### Мониторинг

```bash
# Статус кластера K8s
yc managed-kubernetes cluster get travelmaster-cluster

# Статус PostgreSQL
yc managed-postgresql cluster get travelmaster-postgresql

# Статус Kafka
yc managed-kafka cluster get travelmaster-kafka

# Метрики в Yandex Cloud Console
https://console.cloud.yandex.ru/
```

---

## 🗑️ Удаление инфраструктуры

```bash
# ВНИМАНИЕ: Удалит всю инфраструктуру!
terraform destroy

# Подтвердить: yes
```

**⚠️ Важно**: Убедитесь, что у вас есть бэкапы данных!

---

## 💰 Стоимость

### Примерная стоимость (RUB/месяц)

| Компонент | Конфигурация | Стоимость |
|-----------|--------------|-----------|
| K8s Cluster (Master) | zonal | ~2,000 ₽ |
| K8s Nodes (3x) | 4 vCPU, 8GB, 64GB SSD | ~12,000 ₽ |
| PostgreSQL | s2.small, 100GB | ~6,000 ₽ |
| Kafka | s2.small, 50GB | ~4,000 ₽ |
| Network/Traffic | ~100GB egress | ~1,000 ₽ |
| **Total** | | **~25,000 ₽** |

**Примечание**: Цены приблизительные, актуальные тарифы см. на [Yandex Cloud Pricing](https://cloud.yandex.ru/prices).

---

## 🔒 Безопасность

### Best Practices

1. **Secrets**: Используйте Terraform Cloud или HashiCorp Vault для хранения secrets
2. **State**: Храните Terraform state в S3-совместимом хранилище с шифрованием
3. **IAM**: Используйте минимальные необходимые права для service accounts
4. **Network**: Настройте security groups и firewall правила
5. **Backups**: Регулярно проверяйте бэкапы и процедуру восстановления

### Terraform State Backend

Конфигурация S3 backend для state:

```hcl
terraform {
  backend "s3" {
    endpoint   = "storage.yandexcloud.net"
    bucket     = "travelmaster-terraform-state"
    region     = "ru-central1"
    key        = "terraform.tfstate"
    
    access_key = "YOUR_ACCESS_KEY"
    secret_key = "YOUR_SECRET_KEY"
    
    skip_region_validation      = true
    skip_credentials_validation = true
  }
}
```

---

## 📊 Outputs

После `terraform apply` доступны outputs:

```bash
# Показать все outputs
terraform output

# Конкретный output
terraform output kubernetes_cluster_endpoint
terraform output postgresql_host
terraform output kafka_brokers
```

---

## 🆘 Troubleshooting

### Ошибка: Insufficient quota

**Проблема**: Недостаточно квот для создания ресурсов.

**Решение**:
```bash
# Проверить квоты
yc resource-manager quota list --cloud-id=<cloud-id>

# Запросить увеличение квот через Support
```

### Ошибка: Timeout при создании кластера

**Проблема**: Kubernetes кластер долго создается.

**Решение**: Подождите 15-20 минут. Если не помогло:
```bash
# Проверить статус
yc managed-kubernetes cluster get travelmaster-cluster

# Логи
yc managed-kubernetes cluster list-operations travelmaster-cluster
```

### kubectl не подключается

**Проблема**: `kubectl` не может подключиться к кластеру.

**Решение**:
```bash
# Обновить kubeconfig
yc managed-kubernetes cluster get-credentials travelmaster-cluster --external --force

# Проверить context
kubectl config current-context
```

---

## 📚 Дополнительные ресурсы

- [Yandex Cloud Documentation](https://cloud.yandex.ru/docs)
- [Terraform Yandex Provider](https://registry.terraform.io/providers/yandex-cloud/yandex/latest/docs)
- [Kubernetes on Yandex Cloud](https://cloud.yandex.ru/docs/managed-kubernetes/)
- [Managed PostgreSQL](https://cloud.yandex.ru/docs/managed-postgresql/)
- [Managed Kafka](https://cloud.yandex.ru/docs/managed-kafka/)

---

**Версия**: 1.0  
**Последнее обновление**: 31 октября 2025

