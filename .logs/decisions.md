# DECISIONS — Mizan.ma



## DECISIONS — 2026-07-04
- Backend: Java 21 LTS + Spring Boot 3.x
- Frontend: Angular (latest) + Angular Material
- Auth: Spring Security + JWT (stateless)
- Deployment: Docker Compose + Kubernetes manifests (both, per explicit user choice overriding YAGNI default)
- Retained from prior stack: PostgreSQL (via Spring Data JPA), Cloudflare R2 (PDF storage), Anthropic Claude API for document analysis
- README.md stack section to be rewritten to reflect this pivot

## 2026-07-06 — Sprint 1 kickoff
Decision: Proceed with Sprint 1 execution (Stories 1.1, 1.2, 1.3, 2.1, 2.2) in order, per docs/stories-mizan.md. No plan changes.
Toolchain check: Java 25 (Temurin) present, no global Maven -> will generate Maven Wrapper (mvnw) per service. Node 22.23.1 + npm 10.2.3 present; global Angular CLI is 17.3.17 (outdated, flagged Node 22 unsupported) -> will scaffold via `npx @angular/cli@latest new` instead of global ng. Internet access confirmed (Maven Central + npm registry reachable).

## 2026-07-15 — Sprint 3 close-out scope
User chose to complete both remaining Sprint 3 items now (not defer): E2E/video recording (CLAUDE.md rule 9) and coverage hardening beyond the bare 80% CI gate, before treating Sprint 3 as fully closed and discussing what's next (no Sprint 4 defined in stories doc).
