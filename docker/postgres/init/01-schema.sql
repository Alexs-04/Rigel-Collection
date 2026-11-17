-- Runs once, when the database volume is first created.
-- The database and its owner come from POSTGRES_DB / POSTGRES_USER; the application only
-- needs the schema configured in spring.jpa.properties.hibernate.default_schema.
CREATE SCHEMA IF NOT EXISTS rigel;
