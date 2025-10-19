-- Tabelle für Benutzer
CREATE TABLE users (
    uuid SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Tabelle für Medien
CREATE TABLE IF NOT EXISTS media_entries (
    uuid SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    media_type VARCHAR(20) NOT NULL,
    release_year INT,
    genres TEXT[],
    age_restriction INT,
    creator_id INT REFERENCES users(uuid)
);
