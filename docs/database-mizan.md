# Database Design: Mizan.ma
**Architecture Reference**: docs/architecture-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: DBA

## 1. Database Selection
- **Engine**: PostgreSQL 16 (carried over from prior stack decision, pairs well with Spring Data JPA).
- **Rationale**: Relational data (users, contracts, structured analysis results), strong JSONB support for flexible clause-flag payloads, mature Spring ecosystem support.
- **Hosting**: Self-hosted container (Docker Compose locally, StatefulSet or managed Postgres in K8s/staging).
- **Topology**: One logical database per service — `auth_db`, `user_db`, `contracts_db` — matching ADR/SDR-3. Can live on one Postgres instance at MVP scale (separate DBs, not just schemas, to keep the door open to physical separation later).

## 2. Entity-Relationship Model

```
auth_db:
  User ──1:N──> RefreshToken

user_db:
  UserProfile (user_id FK-by-reference to auth_db User.id — no cross-DB FK, enforced in app layer)

contracts_db:
  Contract (user_id FK-by-reference to auth_db User.id) ──1:1──> AnalysisResult
  AnalysisResult ──1:N──> ClauseFlag
```

Cross-service "foreign keys" (e.g., `contracts.user_id` referencing a user in `auth_db`) are logical only — enforced at the application layer, not a DB-level constraint, since services own separate databases.

## 3. Schema Design

### auth_db
```sql
CREATE TABLE users (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(20) NOT NULL CHECK (role IN ('INDIVIDUAL','BUSINESS','ADMIN')),
  is_active     BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  VARCHAR(255) NOT NULL,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked     BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### user_db
```sql
CREATE TABLE user_profiles (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL UNIQUE, -- references auth_db.users.id logically
  display_name    VARCHAR(255) NOT NULL,
  business_name   VARCHAR(255),
  preferred_lang  VARCHAR(10) NOT NULL DEFAULT 'fr' CHECK (preferred_lang IN ('ar','fr','ary')), -- ary = Darija
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### contracts_db
```sql
CREATE TABLE contracts (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        UUID NOT NULL, -- references auth_db.users.id logically
  file_name      VARCHAR(255) NOT NULL,
  r2_object_key  VARCHAR(512) NOT NULL,
  page_count     INT,
  status         VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ANALYZING','COMPLETE','FAILED')),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE analysis_results (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  contract_id   UUID NOT NULL UNIQUE REFERENCES contracts(id) ON DELETE CASCADE,
  language      VARCHAR(10) NOT NULL,
  summary       TEXT NOT NULL,
  raw_response  JSONB, -- full Claude response for audit/debug
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE clause_flags (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  analysis_result_id  UUID NOT NULL REFERENCES analysis_results(id) ON DELETE CASCADE,
  clause_text         TEXT NOT NULL,
  risk_level          VARCHAR(10) NOT NULL CHECK (risk_level IN ('LOW','MEDIUM','HIGH')),
  explanation         TEXT NOT NULL,
  suggested_correction TEXT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## 4. Index Strategy
| Table | Index Name | Columns | Query Pattern |
|---|---|---|---|
| users | idx_users_email | email | Login lookup |
| refresh_tokens | idx_refresh_user_id | user_id | Refresh/revoke by user |
| user_profiles | idx_profiles_user_id | user_id | Profile lookup by auth user id |
| contracts | idx_contracts_user_id | user_id | Contract history listing |
| contracts | idx_contracts_status | status | Admin stats / stuck-job queries |
| analysis_results | idx_analysis_contract_id | contract_id | Already unique, covers lookup |
| clause_flags | idx_flags_analysis_id | analysis_result_id | Load all flags for a result |
| clause_flags | idx_flags_risk_level | risk_level | Admin trend stats |

## 5. Migration Plan
| Migration File | Description | Reversible |
|---|---|---|
| V1__init_auth_schema.sql | users, refresh_tokens tables | Yes |
| V1__init_user_schema.sql | user_profiles table | Yes |
| V1__init_contracts_schema.sql | contracts, analysis_results, clause_flags tables | Yes |

Managed via Flyway (Spring Boot default integration), one migration set per service's own DB.

## 6. Access Patterns
| Use Case | Query Pattern | Index Coverage |
|---|---|---|
| Login | SELECT by email | idx_users_email |
| Contract history for user | SELECT by user_id ORDER BY created_at DESC | idx_contracts_user_id |
| View analysis for a contract | SELECT by contract_id (join analysis_results + clause_flags) | idx_analysis_contract_id, idx_flags_analysis_id |
| Admin flagged-content trends | SELECT COUNT by risk_level | idx_flags_risk_level |

## 7. Sensitive Data
- Columns requiring encryption/special handling: `users.password_hash` (bcrypt, never plaintext), `contracts.r2_object_key` (not sensitive itself, but the R2 object it points to is private/signed-URL only), `analysis_results.raw_response` (may contain full contract text — treat as confidential, restrict access to owner + admin).
- Row-level security: Not using Postgres RLS at MVP scale — ownership checks enforced in the service layer (contracts-service). Revisit RLS if audit/compliance requires defense-in-depth at the DB layer.
