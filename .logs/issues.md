# ISSUES — Mizan.ma



## 2026-07-06 — Story 1.2 healthcheck bug (found + fixed, not yet re-verified in full stack)
Issue: docker/backend.Dockerfile's HEALTHCHECK used `http://localhost:${PORT}/...` but ARG values aren't visible to HEALTHCHECK's shell-form CMD at container runtime (only at build time) — all 5 backend containers ran fine (app logs showed clean startup) but Docker reported them "unhealthy" with `wget: bad port ''`.
Fix applied: added `ENV PORT=${PORT}` in the final stage (docker/backend.Dockerfile) so the port is a real runtime env var; HEALTHCHECK CMD changed to `$PORT` (no braces, redundant with ENV). Also bumped start-period 30s->60s (auth-service took ~58s to start cold in-container).
Verified: isolated no-cache single-container test (auth-service) confirmed the fix — health flips to "healthy" ~20-25s after start.
NOT yet verified: the fix has not been rebuilt into the actual compose stack images yet (a `docker compose build --no-cache` for the other 4 services was still running when the session ended and was stopped cleanly, not completed). Local images `mizan-ma-{auth,user,contracts,ai-analysis}-service` and `mizan-ma-gateway` still contain the OLD broken healthcheck as of session end.
RESOLVED 2026-07-07: rebuilt full stack, all 6 services report `(healthy)`. See Story 1.2 completion in activity.md.

## 2026-07-08 — Known gap: no rate limiting / account lockout on auth-service login (Story 2.1)
security-mizan.md STRIDE row "Spoofing" lists "Rate limiting, bcrypt password hashing, account lockout after N failed attempts" as the mitigation for auth-service login; only bcrypt hashing was in Story 2.1's acceptance criteria and got built. Rate limiting/lockout has no story yet in docs/stories-mizan.md.
Not a blocker for Story 2.1 (its acceptance criteria didn't call for it), but should be scheduled — either its own story or folded into Story 2.2/3.x — before this goes anywhere near production traffic.

## 2026-07-08 — First real CI run: 4 bugs found and fixed
Sprint 1's first-ever push (Stories 1.1-2.2) triggered `.github/workflows/ci.yml` for the first time — it had never actually run before. Found and fixed, in order:
1. `mvnw` was committed from Windows without the executable bit; Linux runners got `Permission denied`. Fixed: `git update-index --chmod=+x mvnw`.
2. `aquasecurity/trivy-action@0.24.0` doesn't exist (tags are `vX.Y.Z`, no bare `0.24.0` release) — the action failed to resolve, failing the whole security-scan/build-image jobs before they could even start. Fixed: pinned to `@v0.36.0`.
3. Real CRITICAL CVE (CVE-2025-14813) in `org.bouncycastle:bcprov-jdk18on:1.78`, pulled in transitively via `spring-security-rsa` (itself pulled in by `spring-cloud-starter`, only present in gateway). Fixed: pinned `bcprov-jdk18on` to `1.80.2` in the root pom's `dependencyManagement` (applies reactor-wide).
4. 4 more CRITICAL CVEs (CVE-2025-24813 + 3 from 2026) in `tomcat-embed-core:10.1.30`, the embedded Tomcat version Spring Boot 3.3.4 defaults to — present in every service using `spring-boot-starter-web` (auth/user/contracts/ai-analysis, not gateway which uses reactive Netty). Fixed: pinned the whole `tomcat-embed-*` matched set to `10.1.57` in the root pom.
All 4 fixes verified locally before each push: full `./mvnw verify` reactor pass, and all 6 Docker images (5 backend + frontend) rescanned clean with `docker run aquasec/trivy:latest image` matching the CI job's exact severity/exit-code config, before relying on GitHub Actions to confirm.
