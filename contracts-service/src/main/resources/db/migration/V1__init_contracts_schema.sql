CREATE TABLE contracts (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        UUID NOT NULL,
  file_name      VARCHAR(255) NOT NULL,
  r2_object_key  VARCHAR(512) NOT NULL,
  page_count     INT,
  status         VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ANALYZING','COMPLETE','FAILED')),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contracts_user_id ON contracts(user_id);
CREATE INDEX idx_contracts_status ON contracts(status);

CREATE TABLE analysis_results (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  contract_id   UUID NOT NULL UNIQUE REFERENCES contracts(id) ON DELETE CASCADE,
  language      VARCHAR(10) NOT NULL,
  summary       TEXT NOT NULL,
  raw_response  JSONB,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE clause_flags (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  analysis_result_id   UUID NOT NULL REFERENCES analysis_results(id) ON DELETE CASCADE,
  clause_text          TEXT NOT NULL,
  risk_level           VARCHAR(10) NOT NULL CHECK (risk_level IN ('LOW','MEDIUM','HIGH')),
  explanation          TEXT NOT NULL,
  suggested_correction TEXT,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_flags_analysis_id ON clause_flags(analysis_result_id);
CREATE INDEX idx_flags_risk_level ON clause_flags(risk_level);
