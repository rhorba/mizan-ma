# DECISIONS — Mizan.ma



## DECISIONS — 2026-07-04
- Backend: Java 21 LTS + Spring Boot 3.x
- Frontend: Angular (latest) + Angular Material
- Auth: Spring Security + JWT (stateless)
- Deployment: Docker Compose + Kubernetes manifests (both, per explicit user choice overriding YAGNI default)
- Retained from prior stack: PostgreSQL (via Spring Data JPA), Cloudflare R2 (PDF storage), Anthropic Claude API for document analysis
- README.md stack section to be rewritten to reflect this pivot
