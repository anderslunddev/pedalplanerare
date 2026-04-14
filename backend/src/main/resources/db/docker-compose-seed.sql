-- Idempotent seed for local Docker Compose (profile: docker).
-- dockeradmin / password123 — BCrypt same as integration tests.
INSERT INTO user_model (id, username, email, password, "role")
VALUES ('b0000001-0000-4000-8000-000000000002', 'dockeradmin', 'dockeradmin@local.dev',
        '$2b$10$0XmU8Jwli8SCvouFYb6TGOHcFbAV6AMxhBNKlGG/1qwhxEu1.YQIG', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- anders / pass4321 — local dev convenience (BCrypt strength 10).
INSERT INTO user_model (id, username, email, password, "role")
VALUES ('b0000001-0000-4000-8000-000000000003', 'anders', 'anders@local.dev',
        '$2b$10$h94nf.Bh9/i4yg6OLBXGDuWH54kF8zGpo5l61fNDon2yNEmimg6iW', 'USER')
ON CONFLICT (username) DO NOTHING;
