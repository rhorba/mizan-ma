# Stories: Mizan.ma
**PRD**: docs/prd-mizan.md
**Architecture**: docs/architecture-mizan.md

## Epic 1: Foundation & Scaffolding
Stand up the multi-service skeleton, shared library, gateway, and CI so every later story has somewhere to land.

### Story 1.1: Repo & multi-module Maven scaffold + Angular workspace
**Priority**: Must | **Size**: M | **Specialist**: Tech Lead / Backend Dev / Frontend Dev

**Description**: As a developer, I want the 4 Spring Boot services + gateway + `mizan-common` module + Angular workspace scaffolded, so that feature work has a consistent base.

**Acceptance Criteria**:
```gherkin
Given a fresh clone of the repo
When I run the backend build and `ng serve`
Then all services start locally and the Angular dev server loads a blank shell page
```
**Technical Notes**: Uses topology from docs/architecture-mizan.md §3. Java 21, Spring Boot 3.x, Angular latest + Material.
**Dependencies**: None.

---

### Story 1.2: Docker Compose local environment
**Priority**: Must | **Size**: M | **Specialist**: DevOps

**Description**: As a developer, I want `docker compose up` to run Postgres + all services + gateway + frontend, so that I can develop against a full stack locally.

**Acceptance Criteria**:
```gherkin
Given docker-compose.yml is configured
When I run `docker compose up`
Then all containers report healthy and the frontend is reachable on localhost
```
**Technical Notes**: Per docs/devops-mizan.md §5. Uses `.env` populated from `.env.example`.
**Dependencies**: 1.1

---

### Story 1.3: CI pipeline (lint, test, coverage gate, security scan, build)
**Priority**: Must | **Size**: M | **Specialist**: DevOps/DevSecOps

**Acceptance Criteria**:
```gherkin
Given a pull request is opened
When CI runs
Then lint, tests, coverage gate (≥80%), and security scans all execute and block merge on failure
```
**Dependencies**: 1.1

---

## Epic 2: Auth & User Management

### Story 2.1: Register & login (auth-service)
**Priority**: Must | **Size**: M | **Specialist**: Backend Dev

**Description**: As a user, I want to register as Individual or Business and log in, so that I get a JWT to access the app.

**Acceptance Criteria**:
```gherkin
Scenario: Successful registration
  Given valid email/password/role
  When POST /api/v1/auth/register
  Then a user is created and 201 is returned

Scenario: Login issues JWT
  Given a registered user
  When POST /api/v1/auth/login with correct credentials
  Then a JWT access token and refresh token are returned
```
**Technical Notes**: Uses auth_db schema from docs/database-mizan.md. Security requirements from docs/security-mizan.md §3.
**Dependencies**: 1.1, 1.2

---

### Story 2.2: JWT validation at Gateway + role-based routing
**Priority**: Must | **Size**: S | **Specialist**: Backend Dev

**Acceptance Criteria**:
```gherkin
Scenario: Expired token rejected
  Given an expired JWT
  When any authenticated request is made
  Then 401 Unauthorized is returned
```
**Technical Notes**: Implements ADR-3 (Gateway owns JWT validation, forwards trusted headers).
**Dependencies**: 2.1

---

### Story 2.3: User profile (user-service)
**Priority**: Should | **Size**: S | **Specialist**: Backend Dev / Frontend Dev

**Acceptance Criteria**:
```gherkin
Scenario: Update language preference
  Given a logged-in user
  When PUT /api/v1/users/me with preferred_lang=ary
  Then the profile is updated and reflected on next GET
```
**Dependencies**: 2.1

---

## Epic 3: Contract Upload & Analysis

### Story 3.1: PDF upload + storage (contracts-service + R2)
**Priority**: Must | **Size**: M | **Specialist**: Backend Dev

**Acceptance Criteria**:
```gherkin
Scenario: Upload valid PDF
  Given a logged-in user and a valid text-based PDF under the size cap
  When POST /api/v1/contracts with the file
  Then the file is stored in R2, a contract row is created with status PENDING, and analysis is triggered
```
**Technical Notes**: File validation per docs/security-mizan.md §6 (mime-type + magic bytes + size cap).
**Dependencies**: 2.1, 1.2

---

### Story 3.2: AI analysis integration (ai-analysis-service + Claude API)
**Priority**: Must | **Size**: L | **Specialist**: Backend Dev

**Acceptance Criteria**:
```gherkin
Scenario: Successful analysis
  Given extracted PDF text and a target language
  When contracts-service calls ai-analysis-service
  Then a summary, clause flags (with risk levels), and suggested corrections are returned

Scenario: Non-extractable PDF
  Given a scanned/image-only PDF
  When text extraction is attempted
  Then contract status becomes FAILED with a clear error message
```
**Technical Notes**: Implements SDR-2 (synchronous call). Persists via analysis_results/clause_flags schema.
**Dependencies**: 3.1

---

### Story 3.3: Contract history & analysis view (Angular)
**Priority**: Must | **Size**: M | **Specialist**: Frontend Dev

**Acceptance Criteria**:
```gherkin
Scenario: View history
  Given a logged-in user with contracts
  When they visit the dashboard
  Then contracts are listed with status badges, most recent first

Scenario: View analysis
  Given a COMPLETE contract
  When the user opens it
  Then the summary and clause flags render per docs/ux-mizan.md wireframes
```
**Dependencies**: 3.1, 3.2

---

### Story 3.4: Ownership enforcement & contract deletion
**Priority**: Must | **Size**: S | **Specialist**: Backend Dev

**Acceptance Criteria**:
```gherkin
Scenario: Cross-user access denied
  Given user B requests user A's contract
  When GET /api/v1/contracts/:id
  Then 403 Forbidden is returned
```
**Dependencies**: 3.1

---

## Epic 4: Admin & Polish

### Story 4.1: Admin usage/flag stats
**Priority**: Could | **Size**: S | **Specialist**: Backend Dev / Frontend Dev

**Acceptance Criteria**:
```gherkin
Scenario: Admin views stats
  Given an ADMIN user
  When GET /api/v1/contracts/stats
  Then aggregate counts by status and risk level are returned and rendered
```
**Dependencies**: 3.1, 3.2

---

### Story 4.2: Kubernetes manifests + staging deploy
**Priority**: Must | **Size**: M | **Specialist**: DevOps

**Acceptance Criteria**:
```gherkin
Scenario: Deploy to staging
  Given manifests for all services + gateway + frontend + NetworkPolicy
  When applied to the staging cluster
  Then only gateway and frontend are externally reachable; app is functional end-to-end
```
**Dependencies**: 1.3, all Epic 2/3 stories

---

## Sprint Allocation
| Sprint | Stories | Estimated Effort |
|---|---|---|
| Sprint 1 | 1.1, 1.2, 1.3, 2.1, 2.2 | ~4-5 days |
| Sprint 2 | 2.3, 3.1, 3.2, 3.4 | ~5-6 days |
| Sprint 3 | 3.3, 4.1, 4.2 (+ E2E/video recording, coverage hardening) | ~4-5 days |
