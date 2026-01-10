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

CREATE TABLE ratings (
    uuid SERIAL PRIMARY KEY,
    media_id INTEGER NOT NULL REFERENCES media_entries(uuid) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(uuid) ON DELETE CASCADE,
    stars INTEGER NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment TEXT,
    timestamp TIMESTAMP DEFAULT NOW(),
    comment_confirmed BOOLEAN DEFAULT FALSE,
    UNIQUE(media_id, user_id)
);

CREATE TABLE rating_likes (
    rating_id INT REFERENCES ratings(uuid) ON DELETE CASCADE,
    user_id INT REFERENCES users(uuid) ON DELETE CASCADE,
    PRIMARY KEY (rating_id, user_id)
);

CREATE TABLE IF NOT EXISTS user_favorites (
    user_id INT NOT NULL,
    media_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, media_id),
    FOREIGN KEY (user_id) REFERENCES users(uuid) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media_entries(uuid) ON DELETE CASCADE
    );


