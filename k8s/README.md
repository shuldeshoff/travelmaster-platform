# Kubernetes Deployment

Инструкции по развёртыванию TravelMaster Platform в Kubernetes.

## Структура

```
k8s/
├── base/                      # Базовые манифесты
│   ├── deployments/          # Deployment манифесты
│   ├── services/             # Service манифесты
│   ├── configmaps/           # ConfigMap манифесты
│   ├── secrets/              # Secret манифесты (не в git)
│   └── ingress/              # Ingress манифесты
└── overlays/                 # Кастомизации для окружений
    ├── staging/              # Staging окружение
    └── production/           # Production окружение
```

## Предварительные требования

- Kubernetes cluster (1.24+)
- kubectl установлен и настроен
- Helm 3.x установлен
- Ingress Controller (nginx-ingress)
- Cert-manager для SSL сертификатов

## Развёртывание с kubectl

### 1. Создание namespace

```bash
kubectl create namespace travelmaster
```

### 2. Создание secrets

```bash
kubectl create secret generic postgres-secret \
  --from-literal=password=your_password \
  -n travelmaster

kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=your_username \
  --docker-password=your_token \
  -n travelmaster
```

### 3. Применение манифестов

```bash
kubectl apply -f k8s/base/deployments/ -n travelmaster
kubectl apply -f k8s/base/services/ -n travelmaster
kubectl apply -f k8s/base/ingress/ -n travelmaster
```

## Развёртывание с Helm

Helm предоставляет более гибкий и управляемый способ деплоя.

### Staging

```bash
helm upgrade --install travelmaster ./helm/travelmaster \
  --namespace travelmaster-staging \
  --create-namespace \
  --values ./helm/travelmaster/values-staging.yaml
```

### Production

```bash
helm upgrade --install travelmaster ./helm/travelmaster \
  --namespace travelmaster-production \
  --create-namespace \
  --values ./helm/travelmaster/values-prod.yaml
```

## Проверка статуса

```bash
# Проверка pods
kubectl get pods -n travelmaster

# Проверка services
kubectl get svc -n travelmaster

# Проверка ingress
kubectl get ingress -n travelmaster

# Логи конкретного сервиса
kubectl logs -f deployment/gateway-service -n travelmaster
```

## Масштабирование

```bash
# Ручное масштабирование
kubectl scale deployment/booking-service --replicas=5 -n travelmaster

# Автоматическое масштабирование (HPA)
kubectl autoscale deployment/booking-service \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n travelmaster
```

## Обновление

```bash
# Обновление образа
kubectl set image deployment/user-service \
  user=ghcr.io/shuldeshoff/travelmaster-platform/user-service:v1.1.0 \
  -n travelmaster

# Откат к предыдущей версии
kubectl rollout undo deployment/user-service -n travelmaster

# История развёртываний
kubectl rollout history deployment/user-service -n travelmaster
```

## Мониторинг

```bash
# Метрики узлов
kubectl top nodes

# Метрики pods
kubectl top pods -n travelmaster

# События
kubectl get events -n travelmaster --sort-by='.lastTimestamp'
```

## Troubleshooting

```bash
# Описание pod для диагностики
kubectl describe pod <pod-name> -n travelmaster

# Логи с предыдущего запуска
kubectl logs <pod-name> --previous -n travelmaster

# Интерактивная shell в контейнере
kubectl exec -it <pod-name> -n travelmaster -- /bin/sh

# Port forwarding для локального доступа
kubectl port-forward svc/gateway-service 8080:8080 -n travelmaster
```

## Удаление

```bash
# С Helm
helm uninstall travelmaster -n travelmaster

# С kubectl
kubectl delete namespace travelmaster
```

