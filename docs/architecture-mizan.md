# Architecture: Mizan.ma
**PRD Reference**: docs/prd-mizan.md
**System Design Reference**: docs/system-design-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: Software Architect

## 1. Overview
Mizan.ma backend is split into four Spring Boot 3.x (Java 21) microservices — `auth-service`, `user-service`, `contracts-service`, `ai-analysis-service` — fronted by a Spring Cloud Gateway. Each service follows a layered structure (controller → service → repository) with clear package boundaries. The Angular frontend is a single SPA consuming the gateway's REST API.

## 2. Architecture Decision Records

### ADR-1: Layered package structure per service
- **Context**: Need consistent internal structure across 4 independently-built services.
- **Decision**: Each service uses `controller` (REST endpoints, DTOs), `service` (business logic), `repository` (Spring Data JPA), `domain` (entities), `config` (security/beans), `exception` (global exception handling).
- **Alternatives**: Hexagonal/ports-and-adapters per service (rejected for v1 — adds indirection not yet justified for CRUD-heavy services).
- **Consequences**: Simple, familiar Spring Boot structure; easy onboarding; may need refactor to hexagonal only if business logic complexity grows significantly.

### ADR-2: Shared DTO/contract library
- **Context**: Services need to agree on request/response shapes (e.g., JWT claims structure, error response format) without duplicating code.
- **Decision**: A small shared Maven module (`mizan-common`) holds cross-cutting DTOs, error response format, and JWT utility classes. Published to a local/private Maven repo or included as a Git submodule/multi-module Maven build.
- **Alternatives**: Full code duplication per service (rejected — drifts over time); shared full domain library (rejected — would recouple services).
- **Consequences**: One shared dependency to version; keep it deliberately thin (contracts/utilities only, no business logic).

### ADR-3: API Gateway owns JWT validation
- **Context**: Every request needs auth verification; don't want to duplicate JWT parsing in every service.
- **Decision**: Spring Cloud Gateway validates the JWT signature/expiry and forwards user identity via trusted internal headers (e.g., `X-User-Id`, `X-User-Role`) to downstream services. Downstream services trust these headers only because the network is not publicly exposed.
- **Alternatives**: Each service validates JWT independently (rejected — duplication; acceptable fallback if gateway becomes a bottleneck).
- **Consequences**: Services must not be exposed directly to the public internet — internal network isolation (Docker/K8s network policy) is a hard requirement, not optional.

## 3. System Design
```
[Angular SPA] → [API Gateway :8080] → [auth-service :8081]    → [auth_db]
                                     → [user-service :8082]    → [user_db]
                                     → [contracts-service :8083] → [contracts_db]
                                                                 → [Cloudflare R2]
                                                                 → [ai-analysis-service :8084] → [Claude API]
```

## 4. Data Model (summary — full schema in docs/database-mizan.md)
```
User (auth_db) ──1:1──> UserProfile (user_db, linked by user_id)
User ──1:N──> Contract (contracts_db, linked by user_id)
Contract ──1:1──> AnalysisResult (contracts_db)
AnalysisResult ──1:N──> ClauseFlag (contracts_db)
```

## 5. API Design

### auth-service (`/api/v1/auth`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /api/v1/auth/register | Register (Individual/Business) | Public |
| POST | /api/v1/auth/login | Login, returns JWT | Public |
| POST | /api/v1/auth/refresh | Refresh JWT | Refresh token |
| POST | /api/v1/auth/logout | Invalidate refresh token | Required |

### user-service (`/api/v1/users`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | /api/v1/users/me | Get own profile | Required |
| PUT | /api/v1/users/me | Update profile (name, business info, language pref) | Required |
| GET | /api/v1/users | List users (admin) | Admin |

### contracts-service (`/api/v1/contracts`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /api/v1/contracts | Upload PDF, triggers analysis | Required |
| GET | /api/v1/contracts | List own contracts (history) | Required |
| GET | /api/v1/contracts/:id | Get contract + analysis result | Owner |
| DELETE | /api/v1/contracts/:id | Delete contract + analysis | Owner |
| GET | /api/v1/contracts/stats | Aggregate usage/flag stats | Admin |

### ai-analysis-service (`/internal/v1/analyze`) — internal only, not gateway-routed
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /internal/v1/analyze | Analyze extracted PDF text, return clauses/flags | Internal service token |

## 6. Security Considerations
(Full detail in docs/security-mizan.md)
- Authentication: Spring Security + JWT, issued by auth-service, validated at Gateway.
- Authorization: Role-based (INDIVIDUAL, BUSINESS, ADMIN) via JWT claim, enforced with `@PreAuthorize` in each service.
- Data protection: PDFs encrypted at rest in R2 (SSE), Postgres connections over TLS, JWT secret + DB credentials via env vars/K8s secrets — never hardcoded.
- Key risks: internal-network trust boundary (Gateway → services) must be enforced by network policy, not just convention.

## 7. Infrastructure
- Hosting: Docker Compose (local/dev), Kubernetes (staging/prod) — see docs/devops-mizan.md.
- Database: PostgreSQL 16, one logical DB per service.
- CI/CD: GitHub Actions — lint, test, coverage gate, security scan, build, deploy.
- Monitoring: Structured JSON logs to stdout, collected by the container runtime; Actuator health/metrics endpoints per service.

## 8. Technical Risks
| Risk | Mitigation | Owner |
|---|---|---|
| Internal services accidentally exposed publicly | Enforce via K8s NetworkPolicy + Compose internal network, verified in DevSecOps review | DevOps/DevSecOps |
| Shared `mizan-common` library becomes a coupling point | Keep it strictly to DTOs/utilities, review any addition | Software Architect |
| Gateway becomes single point of failure | Run ≥2 replicas in K8s; add health checks | DevOps |
| Cross-service data consistency (e.g., user deleted but contracts remain) | Soft-delete + scheduled cleanup job, documented in backlog | Backend Dev |
