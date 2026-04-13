-- Reset data and seed an admin for integration tests (password: password123)
SET REFERENTIAL_INTEGRITY FALSE;
DELETE FROM cable_path_point;
DELETE FROM cable_model;
DELETE FROM pedal_model;
DELETE FROM board_model;
DELETE FROM user_model;
SET REFERENTIAL_INTEGRITY TRUE;
INSERT INTO user_model (id, username, email, password, role) VALUES
('a0000001-0000-4000-8000-000000000001', 'testadmin', 'admin@test.com',
 '$2b$10$0XmU8Jwli8SCvouFYb6TGOHcFbAV6AMxhBNKlGG/1qwhxEu1.YQIG', 'ADMIN');
