-- SQL script to create the database, user, and schema for the Rigel application in PostgreSQL(psql).
CREATE DATABASE rigel_db;
\c rigel_db;
CREATE USER rigel_root WITH PASSWORD 'root';
GRANT ALL PRIVILEGES ON DATABASE rigel_db TO rigel_root;
CREATE SCHEMA rigel AUTHORIZATION rigel_root;
GRANT ALL PRIVILEGES ON SCHEMA rigel TO rigel_root;