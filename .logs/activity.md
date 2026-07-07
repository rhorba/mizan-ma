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
