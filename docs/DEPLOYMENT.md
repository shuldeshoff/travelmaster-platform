# 🚀 TravelMaster Platform - Deployment Guide

Полное руководство по развертыванию TravelMaster Platform в различных окружениях.

## 📋 Содержание

1. [Quick Start](#quick-start)
2. [Локальное развертывание](#локальное-развертывание)
3. [Docker Compose](#docker-compose)
4. [Kubernetes (локально)](#kubernetes-локально)
5. [Yandex Cloud (Production)](#yandex-cloud-production)
6. [Конфигурация](#конфигурация)
7. [Мониторинг](#мониторинг)
8. [Troubleshooting](#troubleshooting)

---

## ⚡ Quick Start

Самый быстрый способ запустить проект локально:

```bash
# 1. Clone repository
git clone https://github.com/your-org/travelmaster-platform.git
cd travelmaster-platform

# 2. Start infrastructure
docker-compose up -d postgres redis kafka zookeeper

# 3. Build project
mvn clean install -DskipTests

# 4. Start services
# Terminal 1: Gateway
java -jar gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar

# Terminal 2: User Service
java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar

# Terminal 3: Trip Service
java -jar trip-service/target/trip-service-1.0.0-SNAPSHOT.jar

# 5. Check health
curl http://localhost:8080/actuator/health
```

**Ready!** 🎉 API доступно на `http://localhost:8080`

---

## 🏠 Локальное развертывание

### Требования

**Обязательно:**
- Java 21 (OpenJDK или Oracle JDK)
- Maven 3.8+
- Docker 20.10+
- Docker Compose 2.0+

**Опционально:**
- PostgreSQL 14 (если не используете Docker)
- Redis 7
- Apache Kafka 3.5

### Проверка требований

```bash
# Java
java -version
# Expected: openjdk version "21.0.x"

# Maven
mvn -version
# Expected: Apache Maven 3.8.x or higher

# Docker
docker --version
# Expected: Docker version 20.10.x or higher

docker-compose --version
# Expected: Docker Compose version 2.x.x
```

### Установка зависимостей

#### macOS (Homebrew)

```bash
# Java 21
brew install openjdk@21

# Maven
brew install maven

# Docker Desktop
brew install --cask docker
```

#### Ubuntu/Debian

```bash
# Java 21
sudo apt update
sudo apt install openjdk-21-jdk

# Maven
sudo apt install maven

# Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose
sudo apt install docker-compose-plugin
```

#### Windows

1. **Java 21**: [Download from Oracle](https://www.oracle.com/java/technologies/downloads/#java21)
2. **Maven**: [Download from Apache](https://maven.apache.org/download.cgi)
3. **Docker Desktop**: [Download from Docker](https://www.docker.com/products/docker-desktop)

---

## 🐳 Docker Compose

### Полное развертывание

```bash
# 1. Start ALL services (infrastructure + application)
docker-compose up -d

# 2. Check status
docker-compose ps

# 3. View logs
docker-compose logs -f gateway-service

# 4. Stop all services
docker-compose down
```

### Только инфраструктура

```bash
# Start only infrastructure services
docker-compose up -d postgres redis kafka zookeeper prometheus grafana zipkin

# Check health
docker-compose ps
```

### Доступные сервисы

| Service | URL | Credentials |
|---------|-----|-------------|
| **Gateway** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **PostgreSQL** | localhost:5432 | travelmaster_user / password |
| **pgAdmin** | http://localhost:5050 | admin@travelmaster.com / admin |
| **Redis** | localhost:6379 | - |
| **Kafka UI** | http://localhost:8090 | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Zipkin** | http://localhost:9411 | - |

### Полезные команды

```bash
# View logs for specific service
docker-compose logs -f user-service

# Restart service
docker-compose restart booking-service

# Rebuild and restart
docker-compose up -d --build payment-service

# Remove all containers and volumes
docker-compose down -v

# Check resource usage
docker stats
```

---

## ☸️ Kubernetes (локально)

### Требования

- Kubernetes cluster (Minikube, Kind, Docker Desktop)
- kubectl
- Helm 3.x

### Установка Minikube

```bash
# macOS
brew install minikube

# Ubuntu
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Start cluster
minikube start --cpus=4 --memory=8192 --driver=docker

# Enable ingress
minikube addons enable ingress
minikube addons enable metrics-server
```

### Развертывание с kubectl

```bash
# 1. Create namespace
kubectl create namespace travelmaster

# 2. Apply ConfigMaps
kubectl apply -f k8s/base/configmaps/ -n travelmaster

# 3. Apply Secrets (CHANGE passwords first!)
kubectl apply -f k8s/base/secrets/ -n travelmaster

# 4. Apply Storage
kubectl apply -f k8s/base/storage/ -n travelmaster

# 5. Wait for StatefulSets
kubectl wait --for=condition=ready pod -l app=postgres -n travelmaster --timeout=300s

# 6. Apply Deployments
kubectl apply -f k8s/base/deployments/ -n travelmaster

# 7. Apply Services
kubectl apply -f k8s/base/services/ -n travelmaster

# 8. Apply HPA
kubectl apply -f k8s/base/hpa/ -n travelmaster

# 9. Apply Ingress
kubectl apply -f k8s/base/ingress/ -n travelmaster

# 10. Check status
kubectl get pods -n travelmaster
kubectl get svc -n travelmaster
```

### Развертывание с Helm

```bash
# 1. Install with default values
helm install travelmaster ./helm/travelmaster -n travelmaster --create-namespace

# 2. Or with custom values
helm install travelmaster ./helm/travelmaster \
  -n travelmaster \
  -f helm/travelmaster/values-dev.yaml

# 3. Check release
helm list -n travelmaster

# 4. Get status
helm status travelmaster -n travelmaster

# 5. Upgrade
helm upgrade travelmaster ./helm/travelmaster -n travelmaster

# 6. Uninstall
helm uninstall travelmaster -n travelmaster
```

### Доступ к сервисам

```bash
# Port forwarding
kubectl port-forward -n travelmaster svc/gateway-service 8080:8080

# Minikube tunnel (для LoadBalancer)
minikube tunnel

# Get Ingress URL
minikube service gateway-service -n travelmaster --url
```

### Полезные команды

```bash
# Logs
kubectl logs -f deployment/gateway-deployment -n travelmaster

# Exec into pod
kubectl exec -it gateway-pod-xyz -n travelmaster -- /bin/sh

# Scale manually
kubectl scale deployment gateway-deployment --replicas=3 -n travelmaster

# Check HPA
kubectl get hpa -n travelmaster

# Top pods/nodes
kubectl top pods -n travelmaster
kubectl top nodes

# Events
kubectl get events -n travelmaster --sort-by='.lastTimestamp'
```

---

## ☁️ Yandex Cloud (Production)

### Предварительная подготовка

1. **Yandex Cloud Account**
   - Зарегистрироваться на [cloud.yandex.ru](https://cloud.yandex.ru)
   - Создать Billing Account
   - Получить Cloud ID и Folder ID

2. **Yandex Cloud CLI**
   ```bash
   # Install
   curl -sSL https://storage.yandexcloud.net/yandexcloud-yc/install.sh | bash
   
   # Init
   yc init
   
   # Configure
   yc config set cloud-id <CLOUD_ID>
   yc config set folder-id <FOLDER_ID>
   ```

3. **OAuth Token**
   - Получить на [oauth.yandex.ru](https://oauth.yandex.ru/authorize?response_type=token&client_id=1a6990aa636648e9b2ef855fa7bec2fb)

### Terraform Deployment

```bash
# 1. Navigate to terraform directory
cd infra/terraform

# 2. Copy and edit variables
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars

# Required variables:
# - yandex_cloud_token
# - yandex_cloud_id
# - yandex_folder_id
# - db_password (STRONG password!)

# 3. Initialize Terraform
terraform init

# 4. Plan deployment
terraform plan

# 5. Apply (takes 15-20 minutes)
terraform apply

# Confirm: yes

# 6. Get outputs
terraform output
```

### Получить kubeconfig

```bash
# Get cluster credentials
yc managed-kubernetes cluster get-credentials travelmaster-cluster --external

# Verify connection
kubectl cluster-info
kubectl get nodes
```

### Deploy приложения

```bash
# 1. Create namespace
kubectl create namespace travelmaster

# 2. Install with Helm
helm install travelmaster ./helm/travelmaster \
  -n travelmaster \
  -f helm/travelmaster/values-prod.yaml \
  --set postgresql.host=$(terraform output -raw postgresql_host) \
  --set kafka.brokers=$(terraform output -json kafka_brokers)

# 3. Check status
kubectl get pods -n travelmaster -w

# 4. Get external IP
kubectl get ingress -n travelmaster
```

### SSL/TLS Setup

```bash
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Create ClusterIssuer
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@travelmaster.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF

# Update Ingress with TLS
kubectl annotate ingress travelmaster-ingress \
  cert-manager.io/cluster-issuer=letsencrypt-prod \
  -n travelmaster
```

### Мониторинг в Yandex Cloud

```bash
# Cluster status
yc managed-kubernetes cluster get travelmaster-cluster

# Logs
yc logging read --group-id=<log-group-id> --follow

# Metrics в Yandex Cloud Console
https://console.cloud.yandex.ru/folders/<folder-id>/monitoring
```

---

## ⚙️ Конфигурация

### Environment Variables

#### Gateway Service

```bash
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
SPRING_REDIS_HOST=redis-service
SPRING_REDIS_PORT=6379
JWT_SECRET=your-secret-key-change-in-production
```

#### User Service

```bash
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-service:5432/travelmaster_user
SPRING_DATASOURCE_USERNAME=travelmaster_user
SPRING_DATASOURCE_PASSWORD=strong-password
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION=86400000
```

#### Booking Service

```bash
SERVER_PORT=8083
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-service:5432/travelmaster_booking
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-service:9092
TRIP_SERVICE_URL=http://trip-service:8082
```

### Application Profiles

#### development (application-dev.yml)
- H2 in-memory database
- Debug logging
- Mock external services

#### docker (application-docker.yml)
- Docker services URLs
- Moderate logging

#### production (application-prod.yml)
- Production database
- Info logging
- Real external services
- Performance optimizations

### Secrets Management

**NEVER commit secrets to Git!**

#### Локально
```bash
# Use .env file (not committed)
cp .env.example .env
vim .env

# Source before running
source .env
java -jar app.jar
```

#### Docker Compose
```yaml
services:
  user-service:
    environment:
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    env_file:
      - .env
```

#### Kubernetes
```bash
# Create secret
kubectl create secret generic database-secret \
  --from-literal=password='strong-password' \
  -n travelmaster

# Use in pod
env:
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: database-secret
        key: password
```

---

## 📊 Мониторинг

### Health Checks

```bash
# Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# All services
curl http://localhost:8080/actuator/health | jq
```

### Metrics

```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Custom metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Grafana Dashboards

1. Open http://localhost:3000
2. Login: admin / admin
3. Import dashboard ID: 12900 (Spring Boot)
4. Datasource: Prometheus

### Zipkin Tracing

1. Open http://localhost:9411
2. Search by service name
3. View trace details

---

## 🔧 Troubleshooting

См. [TROUBLESHOOTING.md](TROUBLESHOOTING.md) для детального руководства.

### Быстрые решения

**Проблема**: Service не стартует
```bash
# Check logs
docker-compose logs service-name
# or
kubectl logs pod-name -n travelmaster
```

**Проблема**: Database connection failed
```bash
# Check PostgreSQL
docker-compose ps postgres
# Test connection
psql -h localhost -U travelmaster_user -d travelmaster_user
```

**Проблема**: Out of memory
```bash
# Increase JVM heap
export JAVA_OPTS="-Xmx2g"
java $JAVA_OPTS -jar app.jar
```

---

## 📚 Дополнительные ресурсы

- [Architecture Guide](ARCHITECTURE.md)
- [API Documentation](API_SPEC.yaml)
- [Security Guide](SECURITY.md)
- [Team Guide](TEAM_GUIDE.md)
- [Troubleshooting](TROUBLESHOOTING.md)

---

## 🆘 Поддержка

- **Issues**: [GitHub Issues](https://github.com/your-org/travelmaster-platform/issues)
- **Documentation**: `/docs`
- **Email**: support@travelmaster.com

---

**Версия**: 1.0  
**Последнее обновление**: 31 октября 2025

