# Test Strategy: Mizan.ma
**Stories Reference**: docs/stories-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: Test Architect

## 1. Risk Assessment
| Component | Impact | Frequency | Complexity | Test Level |
|---|---|---|---|---|
| auth-service (login/register/JWT) | H | H | M | Maximum |
| contracts-service (upload, ownership checks) | H | H | M | Maximum |
| ai-analysis-service (Claude integration) | H | M | M | High |
| user-service (profile) | M | M | L | Standard |
| API Gateway (routing, JWT validation) | H | H | L | High |
| Angular upload/analysis UI | H | H | M | High |
| Admin stats | L | L | L | Minimal |

## 2. Test Pyramid Targets
| Layer | Coverage Target | Tooling |
|---|---|---|
| Unit | ≥ 60% of business logic | JUnit 5 + Mockito (backend), Jasmine/Karma or Jest (Angular) |
| Integration | ≥ 40% of API + DB layer | Spring Boot Test + Testcontainers (Postgres), Angular HttpClientTestingModule |
| E2E | Critical happy paths only | Playwright (with video recording per CLAUDE.md rule 9) |
| **Combined gate** | **≥ 80%** — non-negotiable | CI blocks merge if below (JaCoCo for Java, Istanbul/nyc for Angular) |

## 3. ATDD Acceptance Scenarios (critical paths)
```gherkin
Feature: Contract Upload and Analysis

  Scenario: Successful upload and analysis
    Given a logged-in Individual user with no prior contracts
    When they upload a valid text-based PDF and select French as output language
    Then the contract status becomes "COMPLETE"
    And the analysis view shows a summary and at least one clause flag

  Scenario: Non-extractable PDF rejected gracefully
    Given a logged-in user
    When they upload a scanned/image-only PDF
    Then the system returns a clear error "Couldn't read this PDF"
    And the contract status becomes "FAILED"

  Scenario: Ownership enforcement
    Given two users A and B, where A owns contract X
    When B requests GET /api/v1/contracts/X
    Then the response is 403 Forbidden

  Scenario: Expired JWT rejected
    Given an expired access token
    When any authenticated endpoint is called
    Then the response is 401 Unauthorized
```

## 4. Adversarial Checklist (high-risk components only)
- [ ] Input abuse: oversized PDF (> size cap), non-PDF file renamed to `.pdf`, empty file, PDF with embedded scripts/macros
- [ ] Auth abuse: unauthenticated access to any `/api/v1/*` route, JWT tampering (modified role claim), token replay after logout/revocation
- [ ] Race conditions: concurrent uploads by the same user, double-submit of the same contract
- [ ] Business logic: cross-user contract access attempts, role escalation attempts (Individual calling Admin-only endpoints)
- [ ] Internal boundary: direct calls to ai-analysis-service or backend services bypassing the Gateway (should be rejected — not publicly routable)

## 5. Release Gate Criteria
- [ ] All acceptance scenarios pass
- [ ] Combined unit + integration coverage ≥ 80% (per service, and overall)
- [ ] No critical/high security findings open (Semgrep/Trivy/Gitleaks clean per docs/devops-mizan.md)
- [ ] E2E happy path (upload → analysis → view) passes and is recorded (Playwright video) at each version completion

### Test Strategy Validation Checklist
- [x] Every story maps to at least one acceptance scenario (see docs/stories-mizan.md)
- [x] Coverage gate ≥ 80% confirmed and CI-enforced
- [x] Adversarial review planned for high-risk components (auth, contracts ownership)
- [x] Release gate criteria documented
