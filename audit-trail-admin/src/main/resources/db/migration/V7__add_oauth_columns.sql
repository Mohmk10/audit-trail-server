-- Add OAuth2 columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_id VARCHAR(255);

-- Make password_hash nullable for OAuth users
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- Add index for OAuth lookups
CREATE INDEX IF NOT EXISTS idx_users_oauth ON users(oauth_provider, oauth_id);
