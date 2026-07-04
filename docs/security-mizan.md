# Security Baseline: Mizan.ma
**Architecture Reference**: docs/architecture-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: Security Engineer

## 1. Threat Model (5-Minute)
- **What are we building?** A web app where users upload legal contracts (potentially sensitive business/financial documents) for AI-driven analysis.
- **Who would attack it?** Opportunistic attackers (credential stuffing, scraping other users' contracts), competitors (data scraping), possibly malicious insiders (admin abuse).
- **Worst outcome?** Leak of a business's confidential contract to another user/competitor; account takeover exposing contract history.

## 2. STRIDE Analysis (top risks only)
| Threat | Component | Mitigation | Status |
|---|---|---|---|
| Spoofing | auth-service login | Rate limiting, bcrypt password hashing, account lockout after N failed attempts | TODO |
| Tampering | JWT | Signed with strong secret (HS256 min, prefer RS256 if key rotation needed), short expiry + refresh flow | TODO |
| Repudiation | contracts-service | Audit log of upload/delete actions with user_id + timestamp | TODO |
| Info Disclosure | contracts-service, R2 storage | Owner-only access checks on every contract read; R2 bucket private, signed URLs only | TODO |
| DoS | ai-analysis-service | PDF size/page cap, rate limit uploads per user | TODO |
| Elevation of Privilege | Gateway → internal services | Internal-only network policy; services must reject requests without trusted internal header/service token | TODO |

## 3. Authentication Strategy
- **Type**: JWT (stateless), issued by auth-service.
- **MFA**: Not required for v1 — justify: MVP scale, low-sensitivity relative to financial/health data; revisit if Business tier grows.
- **Password policy**: Minimum 10 characters, bcrypt (strength 12), no composition rules that encourage weak patterns.
- **Session management**: Access token 1 hour expiry (`JWT_EXPIRATION_MS`), refresh token 7-14 days, refresh tokens stored hashed in auth_db and revocable on logout.

## 4. Authorization Model
- **Pattern**: RBAC — simple roles, no per-resource ABAC needed at this scale.
- **Roles defined**: `INDIVIDUAL`, `BUSINESS`, `ADMIN`.
- **Resource-level checks**: Yes, per-object — contracts-service must verify `contract.user_id == authenticated user_id` (or admin) before returning/deleting a contract. This is the single most important authorization check in the system.

## 5. Data Protection
- **PII fields**: user email, name, business registration info (user_db); contract PDF content, extracted clause text (contracts_db + R2).
- **Encryption at rest**: R2 server-side encryption (default); Postgres volume encryption via hosting provider/K8s storage class.
- **Encryption in transit**: HTTPS enforced end-to-end (Gateway TLS termination), internal service calls over cluster-internal network (TLS optional at MVP scale, revisit for compliance-driven needs).
- **Secrets management**: All secrets (JWT_SECRET, DB passwords, Claude API key, R2 keys) via env vars locally, Kubernetes Secrets in cluster — never committed to git, never in `.env` (only `.env.example` with placeholders).

## 6. Security Requirements for Dev Team
- [ ] All inputs validated server-side (Bean Validation `@Valid` on all DTOs)
- [ ] Output encoded for context (Angular's default HTML sanitization not to be bypassed via `innerHTML`/`bypassSecurityTrust*` without review)
- [ ] No secrets in code, logs, or error messages (scrub PDF content and tokens from log output)
- [ ] HTTPS only in staging/prod, HSTS header set at Gateway
- [ ] Dependencies scanned in CI (Trivy for containers, OWASP Dependency-Check or Snyk for Maven/npm deps)
- [ ] File upload validation: enforce PDF mime-type + magic-byte check, size cap, reject executable/script content disguised as PDF
