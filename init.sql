-- Tabelle für Benutzer
CREATE TABLE user (
    uuid SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);



-- Tabelle für Medien
CREATE TABLE IF NOT EXISTS media_entry (
    uuid SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    media_type VARCHAR(20),
    release_year INT,
    genres TEXT[],
    age_restriction INT,
    creator_id INT REFERENCES users(id)
);

-- Tabelle für Bewertungen
CREATE TABLE IF NOT EXISTS rating (
    uuid SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    media_id INT REFERENCES media_entries(id),
    stars INT CHECK (stars BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
