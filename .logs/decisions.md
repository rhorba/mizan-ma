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

## 2026-07-15 — AI analysis E2E recording uses placeholder Anthropic key
`.env`'s ANTHROPIC_API_KEY is a placeholder (17 chars, not a real key). User chose to record the upload flow as-is rather than pause for a real key: the E2E video will show upload succeeding and the contract landing in FAILED status via the real upstream-error handling path, not a successful analysis. This is genuine app behavior (the error path), not a stub.

## 2026-07-15 — Sprint 4 scope: Register page
Sprint 3 fully shipped, no Sprint 4 defined in stories doc. Audited the app against docs/prd-mizan.md's functional requirements to find real gaps rather than guessing. Found: no Register/Sign-up UI exists at all (auth.service.ts has no register() method, app.routes.ts has no /register route) — Story 3.3 only ever built Login. This is a P0 gap against FR-1 (no one can create an account through the web app). User confirmed Sprint 4 starts with Register page only (not bundling the also-identified accessibility audit gap).

## 2026-07-17 — Sprint 5 SMTP credentials deferred
User chose Gmail SMTP as the provider and was given exact .env setup steps (App Password generation, SMTP_* values), but then said to skip it for now before providing the real App Password. LOG_VERIFICATION_LINKS=true dev fallback remains in place; no code or config changed. Decision: revisit real SMTP delivery later — not blocking other work.

## 2026-07-17 — ai-analysis-service default model updated to claude-sonnet-5
User chose claude-sonnet-5 over claude-opus-4-8 for contract analysis (near-Opus quality at Sonnet cost, intro pricing through 2026-08-31) over keeping the prior claude-sonnet-4-6 default. Updated: ai-analysis-service/src/main/resources/application.yml, .env, .env.example, k8s/staging/01-configmap.yaml, README.md stack line. Test fixtures (AnalyzeControllerIntegrationTest mock response, test application.yml) intentionally left on claude-sonnet-4-6 — they're isolated mock stand-ins, not coupled to the real config. ai-analysis-service tests re-run clean (exit 0) after the change. Real ANTHROPIC_API_KEY still not provided — deferred, same as SMTP credentials.
