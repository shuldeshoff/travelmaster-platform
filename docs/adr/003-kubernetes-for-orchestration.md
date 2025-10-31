# ADR-003: Kubernetes for Container Orchestration

**Status**: Accepted  
**Date**: 2025-10-30  
**Decision Makers**: DevOps Lead, Solution Architect

## Context

TravelMaster Platform состоит из 9 microservices, каждый требует:
- High availability (99.9% uptime)
- Auto-scaling под нагрузкой
- Rolling updates без downtime
- Health monitoring
- Service discovery
- Load balancing

Нам нужна платформа для orchestration containers в production.

## Decision

Мы выбрали **Kubernetes** как primary container orchestration platform для production deployment.

### Deployment Strategy:

- **Local Development**: Docker Compose
- **Staging/Production**: Kubernetes (Yandex Cloud)
- **Package Management**: Helm Charts
- **Infrastructure as Code**: Terraform

## Rationale

### Kubernetes Advantages:

1. **Industry Standard**
   - De facto standard для container orchestration
   - Large community
   - Rich ecosystem

2. **Auto-Scaling**
   - Horizontal Pod Autoscaler (HPA)
   - Cluster Autoscaler
   - Metrics-based scaling

3. **High Availability**
   - Self-healing (pod restarts)
   - Multiple replicas
   - Health checks (liveness/readiness)

4. **Service Discovery & Load Balancing**
   - Built-in DNS
   - Service abstraction
   - Load balancing across pods

5. **Rolling Updates**
   - Zero-downtime deployments
   - Rollback capability
   - Canary deployments

6. **Resource Management**
   - CPU/Memory requests/limits
   - Resource quotas
   - Priority classes

7. **Configuration Management**
   - ConfigMaps для configuration
   - Secrets для credentials
   - Environment-specific configs

8. **Portability**
   - Works on any cloud (AWS, GCP, Azure, Yandex Cloud)
   - On-premises support
   - Hybrid cloud ready

## Alternatives Considered

### 1. Docker Swarm

**Pros**:
- Simpler than Kubernetes
- Native Docker integration
- Easier learning curve

**Cons**:
- Limited features
- Smaller ecosystem
- Less enterprise adoption

**Why rejected**: Недостаточно features для enterprise, меньше community support

### 2. AWS ECS (Elastic Container Service)

**Pros**:
- AWS-native
- Good AWS integration
- Managed service

**Cons**:
- Vendor lock-in (AWS only)
- Limited portability
- Proprietary API

**Why rejected**: Vendor lock-in, мы хотим multi-cloud capability

### 3. Nomad (HashiCorp)

**Pros**:
- Simpler than Kubernetes
- Multi-workload (containers, VMs, etc.)
- Good performance

**Cons**:
- Smaller community
- Less features
- Limited ecosystem

**Why rejected**: Меньше adoption, меньше tools

### 4. Manual VM Management

**Pros**:
- Full control
- No learning curve

**Cons**:
- No auto-scaling
- Manual health checks
- No service discovery
- High operational burden

**Why rejected**: Слишком много manual work, не scalable

## Consequences

### Positive:

- ✅ Production-ready orchestration
- ✅ Auto-scaling capabilities
- ✅ High availability out-of-the-box
- ✅ Rich ecosystem (Helm, Prometheus, etc.)
- ✅ Cloud-agnostic
- ✅ Industry best practices

### Negative:

- ⚠️ Learning curve (complex)
- ⚠️ Operational overhead
- ⚠️ Resource overhead (control plane)
- ⚠️ Debugging can be complex
- ⚠️ Cost (managed Kubernetes clusters)

### Neutral:

- 📊 Requires DevOps expertise
- 📊 Need proper monitoring setup
- 📊 YAML configurations to maintain

## Implementation Details

### Cluster Configuration

```yaml
# Yandex Managed Kubernetes
Node Group:
  - Min replicas: 3
  - Max replicas: 10
  - Instance type: standard-v3 (4 vCPU, 8GB RAM)
  - Auto-scaling enabled
```

### Key Components

1. **Deployments**
   - One per microservice
   - Rolling update strategy
   - Resource limits defined

2. **Services**
   - ClusterIP для internal communication
   - LoadBalancer для Gateway

3. **ConfigMaps**
   - Application configuration
   - Environment variables

4. **Secrets**
   - Database credentials
   - JWT secrets
   - API keys

5. **HPA (Horizontal Pod Autoscaler)**
   - CPU-based: 70% threshold
   - Memory-based: 80% threshold
   - Min 2, Max 10-15 replicas

6. **Ingress**
   - NGINX Ingress Controller
   - TLS termination
   - Path-based routing

7. **StatefulSets**
   - PostgreSQL
   - Redis
   - Kafka/Zookeeper

8. **PersistentVolumes**
   - Database storage
   - Logs (if needed)

### Helm Charts Structure

```
helm/travelmaster/
├── Chart.yaml
├── values.yaml
├── values-staging.yaml
├── values-prod.yaml
└── templates/
    ├── deployments/
    ├── services/
    ├── configmaps/
    ├── secrets/
    ├── hpa/
    └── ingress/
```

### Resource Allocation

| Service | Requests | Limits | Replicas (min-max) |
|---------|----------|--------|-------------------|
| Gateway | 500m, 512Mi | 1, 1Gi | 2-10 |
| User Service | 500m, 512Mi | 1, 1Gi | 2-8 |
| Trip Service | 500m, 512Mi | 1, 1Gi | 2-8 |
| Booking Service | 500m, 512Mi | 1, 1Gi | 3-15 |
| Payment Service | 500m, 512Mi | 1, 1Gi | 2-8 |

### Health Checks

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

## Deployment Process

### Development
```bash
docker-compose up -d
```

### Staging
```bash
helm upgrade --install travelmaster ./helm/travelmaster \
  -n travelmaster-staging \
  -f helm/travelmaster/values-staging.yaml
```

### Production
```bash
# Terraform для infrastructure
cd infra/terraform
terraform apply

# Helm для application
helm upgrade --install travelmaster ./helm/travelmaster \
  -n travelmaster \
  -f helm/travelmaster/values-prod.yaml
```

## Monitoring & Observability

- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **Zipkin**: Distributed tracing
- **Kubernetes Dashboard**: Cluster overview

## Cost Optimization

1. **HPA**: Scale down при low load
2. **Cluster Autoscaler**: Remove unused nodes
3. **Resource requests**: Right-sized
4. **Spot instances**: For non-critical workloads
5. **Scheduled scaling**: Night/weekend scale-down

Estimated Cost (Yandex Cloud):
- Cluster: ~2,000₽/month
- Nodes (3x): ~12,000₽/month
- **Total**: ~14,000₽/month for infrastructure

## Migration Strategy

Phase 1: Docker Compose (Development) ✅  
Phase 2: Kubernetes manifests (Basic) ✅  
Phase 3: Helm charts (Advanced) ✅  
Phase 4: GitOps with ArgoCD (Future)  

## Training & Documentation

- Team training on Kubernetes basics
- Runbooks для common operations
- Disaster recovery procedures
- Incident response playbooks

## References

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Kubernetes Patterns](https://k8spatterns.io/)
- [Production Best Practices](https://kubernetes.io/docs/setup/best-practices/)
- [Yandex Managed Kubernetes](https://cloud.yandex.ru/docs/managed-kubernetes/)

---

**Last Updated**: 2025-10-31

