# ACTIVITY — Mizan.ma



## MILESTONE — 2026-07-04
Foundation docs (PRD, system design, architecture, security, database, UX, UI, test strategy, DevOps, stories) approved and pushed for Mizan.ma Spring Boot + Angular rebuild.

## PUSH — 2026-07-04
Branch: main | Commit: 10964f3 | Remote: https://github.com/rhorba/mizan-ma.git | Content: foundation docs + scaffolding

## 2026-07-06 — Sprint 1 / Story 1.1 (backend)
Completed: multi-module Maven scaffold — root parent POM (Spring Boot 3.3.4, Java 21, Spring Cloud 2023.0.3), Maven Wrapper (mvnw, Maven 3.9.9), mizan-common (ErrorResponse/ApiError/ErrorDetail DTOs + InternalHeaders constants per ADR-2/ADR-3), gateway (:8080, Spring Cloud Gateway routes to auth/user/contracts), auth-service (:8081), user-service (:8082), contracts-service (:8083) each with layered packages (controller/service/repository/domain/config/exception per ADR-1) + Postgres/JPA/Actuator, ai-analysis-service (:8084, stateless — client/service/controller/config/exception, Anthropic config, no DB per architecture). `./mvnw compile` succeeds for all 6 modules.
Next: Angular workspace scaffold (frontend half of Story 1.1).

## 2026-07-06 — Sprint 1 / Story 1.1 (frontend) — STORY 1.1 COMPLETE
Completed: Angular workspace via `npx @angular/cli@latest new` (Angular 22, project name `mizan-frontend` matching docs/devops-mizan.md dist path), Angular Material added (azure-blue theme, animations enabled), blank shell (`<router-outlet />` only, default CLI welcome page removed), environment.ts/environment.production.ts with apiBaseUrl + fileReplacements wired in angular.json.
Verified: `ng build --configuration development` succeeds, dev server smoke-tested on port 4201 (avoided port 4200 — occupied by an unrelated pre-existing server on this machine) — served MizanFrontend shell with blank <app-root>, confirmed via curl, then cleanly stopped by exact PID.
Story 1.1 acceptance criteria met: backend `./mvnw compile` succeeds across all 6 modules; Angular dev server loads a blank shell page.

## 2026-07-06 — Sprint 1 / Story 1.2 — STORY 1.2 COMPLETE
Completed: `docker compose build --no-cache` for all 5 backend services + frontend succeeded clean (HEALTHCHECK PORT-ARG fix from last session confirmed working). `docker compose up -d` brought up the full 7-container stack; postgres + all 5 Spring Boot services report `(healthy)`, frontend serving 200 on its port, gateway `/actuator/health` returns 200.
Port conflict handling: this machine has atlas-events permanently bound to 5432/8080/4200 (see memory). Rather than editing canonical docker-compose.yml ports (rejected approach per last session), added `docker-compose.override.yml` (git-ignored, uses Compose v2.24+ `!override` merge tag to replace rather than append the ports list) remapping host-side only: postgres 5433, gateway 8085, frontend 4201. Canonical docker-compose.yml is untouched and portable to other machines/CI.
Story 1.2 acceptance criteria met: full local stack starts via `docker compose up -d` with all services healthy.

## 2026-07-06/07 — Sprint 1 / Story 1.3 — STORY 1.3 COMPLETE
Completed: GitHub Actions CI (`.github/workflows/ci.yml`) — backend-lint (Spotless), backend-test (JUnit + Testcontainers-backed JPA smoke tests + JaCoCo 80% line-coverage gate, scaffold classes excluded), frontend-lint (ESLint via @angular-eslint), frontend-test (Vitest + 80% coverage gate via angular.json coverageThresholds), security-scan (Gitleaks secrets, Trivy fs SCA, Semgrep SAST), build-backend/build-frontend (docker build per service + Trivy image scan). Deploy stages intentionally deferred to Story 4.2 (Kubernetes) per docs/stories-mizan.md scope.
Real issues found and fixed: (1) google-java-format (via Spotless) is incompatible with JDK 25's javac internals — switched to Spotless's Eclipse-JDT formatter, which doesn't touch javac internals. (2) The pre-existing `@SpringBootTest` smoke tests in auth/user/contracts-service had never actually been run (Story 1.1 only verified `compile`) and failed on JPA autoconfiguration with no datasource — wired Testcontainers Postgres via `@ServiceConnection` per the DevOps doc's stated testing strategy. (3) JaCoCo 0.8.12 logs (non-fatal) instrumentation warnings against JDK 25 bootstrap classes — cosmetic, confirmed `mvn verify` still exits 0.
Decision (user-confirmed): 80% coverage gate is wired and enforcing now rather than deferred, with Application/package-info classes excluded from the denominator — it starts biting as soon as Story 2.1 adds real business logic.
Full local `./mvnw verify` and `ng lint` / `ng test` pass. Workflow YAML not yet validated by an actual GitHub Actions run — committed locally, not yet pushed (push deferred to sprint end per CLAUDE.md rule 7; will monitor CI per rule 11 once pushed).

## 2026-07-08 — Sprint 1 / Story 2.1 — STORY 2.1 COMPLETE
Completed: auth-service register/login/refresh/logout, backed by a Flyway migration (V1__init_auth_schema.sql: users + refresh_tokens per docs/database-mizan.md). Shared `JwtService` added to mizan-common (ADR-2 — auth-service issues, Gateway will validate the same claims shape in Story 2.2). Password hashing via BCrypt strength 12; refresh tokens are opaque SecureRandom strings, SHA-256-hashed at rest, rotated on every `/refresh` call and revocable via `/logout` (security-mizan.md §3). Registration is restricted to a `RegistrableRole` enum (INDIVIDUAL/BUSINESS only) — the domain `Role` enum includes ADMIN, but that value is unreachable from the public request DTO, closing a self-registration-as-admin privilege-escalation path.
Verified via Testcontainers-backed MockMvc integration tests covering the full register→login→refresh(rotation)→logout→refresh(rejected) lifecycle, plus duplicate-email, weak-password, and wrong-password error paths, all asserting the mizan-common ApiError/ErrorResponse shape. `mvn verify` on mizan-common+auth-service: ~96% instruction / 87% branch coverage, gate passes.
Not yet built (deferred, no story dependency yet): rate limiting / account lockout on login (security-mizan.md STRIDE row "Spoofing" — flagged as a gap, not yet a story).
