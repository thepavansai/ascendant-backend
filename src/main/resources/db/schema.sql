-- =============================================================
-- Ascendant Initiative — PostgreSQL Schema
-- Version: 1.0 | Run order: 01-schema.sql (before data.sql)
-- =============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================
-- ENUM types
-- =============================================================
DO $$ BEGIN
    CREATE TYPE user_role        AS ENUM ('CHILD', 'PARENT', 'ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE identity_type    AS ENUM ('STRATEGIST', 'BUILDER', 'ANALYST', 'CREATOR');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE mission_type     AS ENUM ('FACTUAL', 'ANALYTICAL', 'OPEN_ENDED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE eval_status      AS ENUM ('PENDING', 'DONE', 'FAILED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =============================================================
-- TABLE: users
-- =============================================================
CREATE TABLE IF NOT EXISTS users (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT         NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'CHILD',
    requested_parent_email VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role  ON users(role);

-- =============================================================
-- TABLE: player_profiles
-- =============================================================
CREATE TABLE IF NOT EXISTS player_profiles (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    identity_type VARCHAR(20)  NOT NULL DEFAULT 'ANALYST',
    xp            INTEGER      NOT NULL DEFAULT 0,
    level         INTEGER      NOT NULL DEFAULT 1,
    intellect     FLOAT        NOT NULL DEFAULT 0.0,
    judgment      FLOAT        NOT NULL DEFAULT 0.0,
    awareness     FLOAT        NOT NULL DEFAULT 0.0,
    clarity       FLOAT        NOT NULL DEFAULT 0.0,
    streak_days   INTEGER      NOT NULL DEFAULT 0,
    last_active   TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_player_profiles_user_id ON player_profiles(user_id);

-- =============================================================
-- TABLE: missions
-- =============================================================
CREATE TABLE IF NOT EXISTS missions (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(200) NOT NULL,
    narrative        TEXT         NOT NULL,
    difficulty_level INTEGER      NOT NULL CHECK (difficulty_level BETWEEN 1 AND 5),
    mission_type     VARCHAR(20)  NOT NULL DEFAULT 'ANALYTICAL',
    rule_weight      FLOAT        NOT NULL DEFAULT 0.3,
    ai_weight        FLOAT        NOT NULL DEFAULT 0.7,
    attribute_weights JSONB,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_missions_active     ON missions(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_missions_difficulty ON missions(difficulty_level);
CREATE INDEX IF NOT EXISTS idx_missions_type       ON missions(mission_type);

-- =============================================================
-- TABLE: scenarios
-- =============================================================
CREATE TABLE IF NOT EXISTS scenarios (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id    UUID         NOT NULL UNIQUE REFERENCES missions(id) ON DELETE CASCADE,
    context       TEXT         NOT NULL,
    choices       JSONB,
    open_response BOOLEAN      NOT NULL DEFAULT TRUE,
    order_index   INTEGER      NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_scenarios_mission_id ON scenarios(mission_id);

-- =============================================================
-- TABLE: responses
-- =============================================================
CREATE TABLE IF NOT EXISTS responses (
    id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mission_id      UUID      NOT NULL REFERENCES missions(id),
    scenario_id     UUID      NOT NULL REFERENCES scenarios(id),
    answer_text     TEXT      NOT NULL,
    selected_choice VARCHAR(10),
    word_count      INTEGER,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_responses_user_id    ON responses(user_id);
CREATE INDEX IF NOT EXISTS idx_responses_mission_id ON responses(mission_id);
CREATE INDEX IF NOT EXISTS idx_responses_user_mission ON responses(user_id, mission_id);

-- =============================================================
-- TABLE: evaluations
-- =============================================================
CREATE TABLE IF NOT EXISTS evaluations (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    response_id     UUID         NOT NULL UNIQUE REFERENCES responses(id) ON DELETE CASCADE,
    rule_score      FLOAT,
    ai_score        FLOAT,
    final_score     FLOAT,
    intellect_score FLOAT,
    judgment_score  FLOAT,
    awareness_score FLOAT,
    clarity_score   FLOAT,
    feedback_text   TEXT,
    ai_tokens_used  INTEGER      DEFAULT 0,
    eval_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_evaluations_response_id ON evaluations(response_id);
CREATE INDEX IF NOT EXISTS idx_evaluations_status      ON evaluations(eval_status);

-- =============================================================
-- TABLE: progression_logs
-- =============================================================
CREATE TABLE IF NOT EXISTS progression_logs (
    id           UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mission_id   UUID      NOT NULL REFERENCES missions(id),
    response_id  UUID      REFERENCES responses(id),
    xp_earned    INTEGER   NOT NULL DEFAULT 0,
    final_score  FLOAT,
    level_before INTEGER   NOT NULL DEFAULT 1,
    level_after  INTEGER   NOT NULL DEFAULT 1,
    leveled_up   BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_progression_user_id   ON progression_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_progression_created   ON progression_logs(user_id, created_at DESC);

-- =============================================================
-- TABLE: parent_child_links
-- =============================================================
CREATE TABLE IF NOT EXISTS parent_child_links (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id  UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    child_id   UUID      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    approved   BOOLEAN   NOT NULL DEFAULT FALSE,
    linked_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(parent_id, child_id)
);

CREATE INDEX IF NOT EXISTS idx_parent_child_parent   ON parent_child_links(parent_id, approved);
CREATE INDEX IF NOT EXISTS idx_parent_child_child    ON parent_child_links(child_id);

-- =============================================================
-- TABLE: ai_cost_log
-- =============================================================
CREATE TABLE IF NOT EXISTS ai_cost_log (
    id           UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tokens_used  INTEGER   NOT NULL DEFAULT 0,
    model        VARCHAR(60),
    call_date    DATE      NOT NULL DEFAULT CURRENT_DATE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_cost_user_date ON ai_cost_log(user_id, call_date);
