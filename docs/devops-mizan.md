# DevOps Foundation: Mizan.ma
**Architecture**: docs/architecture-mizan.md
**Security**: docs/security-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: DevOps/DevSecOps

## 1. Environment Strategy
| Environment | Purpose | Deploy Trigger |
|---|---|---|
| local | Development (Docker Compose) | Manual (`docker compose up`) |
| staging | QA / Preview (Kubernetes) | Auto on merge to `main` |
| production | Live users (Kubernetes) | Manual approval gate after staging soak |

## 2. CI Pipeline (GitHub Actions)
```yaml
stages:
  - lint            # Checkstyle/Spotless (Java), ESLint (Angular)
  - test            # JUnit + Testcontainers (per service), Jasmine/Jest (Angular) — fail if coverage < 80%
  - security-scan   # Semgrep (SAST), Trivy (SCA + container scan), Gitleaks (secrets)
  - build           # Maven build per service → Docker image; Angular build → Docker image (nginx)
  - deploy-staging  # kubectl apply / helm upgrade, auto on main merge
  - deploy-prod     # manual approval gate, same manifests promoted
```
CI monitoring is mandatory (CLAUDE.md rule 11): every push is watched; if CI goes RED, stop other work, diagnose, fix, re-push, repeat until GREEN before any SHIP phase.

## 3. Infrastructure
- **Hosting**: Kubernetes cluster (staging/prod) — provider TBD by user (e.g., a managed K8s offering); Docker Compose for local dev.
- **Compute**: Containers — 4 backend services + Gateway + Angular (served via nginx) + Postgres (3 logical DBs).
- **Database**: Self-hosted Postgres 16 container/StatefulSet at MVP scale; migrate to managed Postgres if operational burden grows.
- **Secrets**: Kubernetes Secrets (staging/prod), `.env` (local, gitignored, never committed) — values sourced from `.env.example` placeholders.
- **Monitoring**: Spring Boot Actuator (`/actuator/health`, `/actuator/metrics`) per service; container stdout logs collected by the cluster's log driver. No dedicated APM/tracing tool at MVP scale (YAGNI) — revisit if debugging cross-service issues becomes painful.

## 4. Security Scanning Gates
| Scanner | Scan Type | Fail Threshold |
|---|---|---|
| Semgrep | SAST — code vulnerabilities | Critical findings |
| Trivy | SCA — dependency + container image CVEs | Critical CVEs |
| Gitleaks | Secrets detection | Any secrets found |

## 5. Docker Setup

### Backend service (example: contracts-service)
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/contracts-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Angular frontend
```dockerfile
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

FROM nginx:alpine
COPY --from=build /app/dist/mizan-frontend /usr/share/nginx/html
EXPOSE 80
```

### docker-compose.yml (local dev) — services
- `postgres` (single instance, 3 DBs created via init script)
- `auth-service`, `user-service`, `contracts-service`, `ai-analysis-service`
- `gateway`
- `frontend` (nginx-served Angular build, or `ng serve` for hot-reload dev mode)

### Kubernetes manifests (staging/prod) — per service
- `Deployment` (image, resource requests/limits, readiness/liveness probes hitting `/actuator/health`)
- `Service` (ClusterIP, internal only — except `gateway` and `frontend`, exposed via `Ingress`)
- `ConfigMap` (non-secret config)
- `Secret` (DB credentials, JWT secret, API keys — populated from CI/CD secret store, never committed)
- `Ingress` (routes public traffic to `gateway` and `frontend` only)
- `NetworkPolicy` (deny direct external access to auth/user/contracts/ai-analysis services — Gateway is the only public entry point, per ADR-3)

## 6. Monitoring Baseline
| Signal | Tool | Alert Threshold |
|---|---|---|
| Logs | Container stdout → cluster log aggregation | Error rate spike (manual review at MVP scale) |
| Metrics | Actuator `/metrics` | Latency p99 > 30s on analysis endpoint |
| Uptime | K8s liveness/readiness probes | Pod restart loop / probe failures |

### DevOps Validation Checklist
- [x] All 3 environments defined with deploy triggers
- [x] CI pipeline covers lint + test (coverage gate) + security scan + build + deploy
- [x] Coverage gate configured (< 80% fails CI)
- [x] Secrets management strategy confirmed (K8s Secrets + `.env.example`, no hardcoded secrets)
- [x] Monitoring baseline defined with alert thresholds
- [x] NetworkPolicy enforces internal-only services (required by ADR-3 trust boundary)
