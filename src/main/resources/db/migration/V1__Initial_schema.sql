-- Initial schema migration for Utabridge Server
-- Creates tables for users, refresh tokens, source terms, and translations

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    picture_url VARCHAR(2048),
    provider_id VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    last_active_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider_id ON users(provider_id);

-- User preferences table (element collection from User entity)
CREATE TABLE user_preferences (
    user_id BIGINT NOT NULL,
    preference_key VARCHAR(100) NOT NULL,
    preference_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, preference_key),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    hashed_token VARCHAR(512) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hashed_token ON refresh_tokens(hashed_token);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- Source terms table
CREATE TABLE source_terms (
    id BIGSERIAL PRIMARY KEY,
    original_text VARCHAR(1000) NOT NULL UNIQUE,
    language_code VARCHAR(10) NOT NULL,
    romanized_text VARCHAR(1000) NOT NULL
);

CREATE INDEX idx_source_terms_original_text ON source_terms(original_text);
CREATE INDEX idx_source_terms_language_code ON source_terms(language_code);

-- Source term tags table (element collection from SourceTerm entity)
CREATE TABLE source_term_tags (
    source_term_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (source_term_id, tag),
    FOREIGN KEY (source_term_id) REFERENCES source_terms(id) ON DELETE CASCADE
);

CREATE INDEX idx_source_term_tags_source_term_id ON source_term_tags(source_term_id);
CREATE INDEX idx_source_term_tags_tag ON source_term_tags(tag);

-- Translations table
CREATE TABLE translations (
    id BIGSERIAL PRIMARY KEY,
    language_code VARCHAR(10) NOT NULL,
    translated_text VARCHAR(1000) NOT NULL,
    source_term_id BIGINT NOT NULL,
    FOREIGN KEY (source_term_id) REFERENCES source_terms(id) ON DELETE CASCADE
);

CREATE INDEX idx_translations_source_term_id ON translations(source_term_id);
CREATE INDEX idx_translations_language_code ON translations(language_code);
CREATE UNIQUE INDEX idx_translations_source_term_language ON translations(source_term_id, language_code);
