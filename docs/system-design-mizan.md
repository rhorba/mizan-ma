# System Design: Mizan.ma
**PRD Reference**: docs/prd-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: System Designer

## 1. Non-Functional Requirements
| Attribute | Target | Notes |
|---|---|---|
| Availability | Best-effort (~99% informal target) | No formal SLA at MVP scale |
| Latency (p99) | < 30s for analysis endpoint, < 500ms for CRUD endpoints | Analysis is bound by Claude API round-trip |
| Throughput | ~10-50 concurrent analyses at launch | Scale up via replica count, not re-architecture |
| Data Volume | < 5 GB/month PDFs at launch | Cloudflare R2 scales independently of app |
| Retention | Contracts + analyses retained indefinitely unless user deletes | User-initiated deletion required (FR gap noted for backlog) |
| Recovery (RTO) | < 4 hours | Manual redeploy acceptable at this scale |
| Recovery (RPO) | < 24 hours | Daily DB backups sufficient for MVP |

## 2. Component Topology

```
[Angular SPA (browser)]
        │ HTTPS
        ▼
[API Gateway — Spring Cloud Gateway]  ←── JWT validation, routing, CORS, rate limiting
        │
   ┌────┼─────────────┬──────────────────┐
   ▼    ▼              ▼                  ▼
[auth-service] [user-service]   [contracts-service]
   │                 │                    │
[auth_db]        [user_db]         [contracts_db]
 (Postgres)      (Postgres)          (Postgres)
                                          │
                                          ├──► [Cloudflare R2] (PDF blob storage)
                                          │
                                          ▼
                              [ai-analysis-service] (stateless)
                                          │
                                          ▼
                              [Anthropic Claude API] (external)

Observability: each service → structured logs → (stdout, collected by Docker/K8s log driver)
```

Each service is an independently deployable Spring Boot 3.x application (Java 21). The API Gateway is the single public entry point; internal services are not exposed outside the Docker/Kubernetes network.

## 3. Integration Patterns
| Integration | Pattern | Reason |
|---|---|---|
| Angular → API Gateway | REST/HTTPS + JWT bearer | Standard SPA-to-backend pattern |
| Gateway → services | REST (internal network) | Simple, no need for gRPC at this scale |
| contracts-service → ai-analysis-service | REST, synchronous call (v1) | PDF analysis latency (~seconds) is acceptable synchronously; revisit if p99 exceeds 30s |
| contracts-service → Cloudflare R2 | S3-compatible SDK (AWS SDK v2) | R2 is S3 API-compatible |
| ai-analysis-service → Claude API | HTTPS REST via Anthropic SDK | Direct integration, no intermediary needed |
| Service → service auth | Internal JWT (service-to-service token) or trusted network + shared secret header | Avoid over-engineering with mTLS at MVP scale |

## 4. Scalability Strategy
- Scaling approach: Horizontal — each microservice scales independently via replica count (Docker Compose `--scale` locally, K8s `replicas` / HPA in cluster).
- Cache strategy: None at MVP. Revisit (Redis) only if read-heavy endpoints (e.g., contract history listing) show measured latency issues.
- Queue strategy: None at MVP — analysis calls are synchronous. Add a queue (e.g., RabbitMQ/SQS) only if analysis volume or Claude API latency makes synchronous calls unreliable (see SDR-2).

## 5. System Design Decision Records

### SDR-1: Microservices split (auth, user, contracts, ai-analysis)
- **NFR Driver**: User explicitly requested microservices architecture for independent scaling/deployability, overriding the modular-monolith YAGNI default.
- **Decision**: Four services — auth-service, user-service, contracts-service, ai-analysis-service — each with its own deployable unit; ai-analysis-service is stateless (no DB).
- **Alternatives**: Modular monolith (rejected — user explicitly chose microservices); full event-driven mesh with service discovery (Eureka/Consul) (rejected for now — K8s/Compose DNS is sufficient at this scale).
- **Re-evaluate when**: If service-to-service calls become a bottleneck, or team size grows enough to justify a service mesh.

### SDR-2: Synchronous analysis call (contracts-service → ai-analysis-service)
- **NFR Driver**: NFR-1 (30s latency target) is achievable synchronously for typical contract sizes.
- **Decision**: contracts-service calls ai-analysis-service synchronously and blocks until the Claude API responds.
- **Alternatives**: Async job queue with polling/WebSocket status updates (rejected for v1 — adds infra complexity not yet justified).
- **Re-evaluate when**: p99 latency exceeds 30s regularly, or contract page-count limits need to be relaxed.

### SDR-3: Database-per-service
- **NFR Driver**: Microservices data ownership — avoid cross-service coupling on shared tables.
- **Decision**: auth_db, user_db, contracts_db as separate PostgreSQL databases (can share one Postgres instance at MVP scale via separate schemas/DBs, split onto separate instances later if load requires it).
- **Alternatives**: Single shared database (rejected — defeats the purpose of choosing microservices; would recreate monolith coupling).
- **Re-evaluate when**: Never for MVP — this is the baseline for the chosen architecture.

### SDR-4: Kubernetes alongside Docker Compose
- **NFR Driver**: User explicitly requested both Docker Compose and Kubernetes manifests from day one, overriding the Compose-only YAGNI default.
- **Decision**: Docker Compose for local dev; Kubernetes manifests (Deployments, Services, ConfigMaps, Secrets) for staging/production from the start.
- **Alternatives**: Compose-only until scale demands K8s (rejected — explicit user choice).
- **Re-evaluate when**: N/A — already the chosen baseline.
