CREATE TABLE user_profiles (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL UNIQUE,
  display_name    VARCHAR(255) NOT NULL,
  business_name   VARCHAR(255),
  preferred_lang  VARCHAR(10) NOT NULL DEFAULT 'fr' CHECK (preferred_lang IN ('ar','fr','ary')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_user_id ON user_profiles(user_id);
