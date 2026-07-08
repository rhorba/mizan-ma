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
