-- Drop tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS MovieGenre CASCADE;
DROP TABLE IF EXISTS MovieCharacter CASCADE;
DROP TABLE IF EXISTS Movie CASCADE;
DROP TABLE IF EXISTS Person CASCADE;
DROP TABLE IF EXISTS Genre CASCADE;

-- Drop sequences if they exist
DROP SEQUENCE IF EXISTS person_seq;
DROP SEQUENCE IF EXISTS movie_seq;
DROP SEQUENCE IF EXISTS genre_seq;
DROP SEQUENCE IF EXISTS moviechar_seq;

-- Create sequences for ID generation
CREATE SEQUENCE person_seq START 1;
CREATE SEQUENCE movie_seq START 1;
CREATE SEQUENCE genre_seq START 1;
CREATE SEQUENCE moviechar_seq START 1;

-- Create Person table
CREATE TABLE Person (
    PersonID BIGINT PRIMARY KEY DEFAULT nextval('person_seq'),
    Name VARCHAR(255) NOT NULL
);

-- Create Movie table
CREATE TABLE Movie (
    MovieID BIGINT PRIMARY KEY DEFAULT nextval('movie_seq'),
    Title VARCHAR(255) NOT NULL,
    Year INT NOT NULL,
    Type CHAR(1) NOT NULL
);

-- Create Genre table
CREATE TABLE Genre (
    GenreID BIGINT PRIMARY KEY DEFAULT nextval('genre_seq'),
    Genre VARCHAR(100) NOT NULL
);

-- Create MovieCharacter table (1:N relationship from Movie and Person)
CREATE TABLE MovieCharacter (
    MovCharID BIGINT PRIMARY KEY DEFAULT nextval('moviechar_seq'),
    MovieID BIGINT NOT NULL,
    PlayerID BIGINT NOT NULL,
    Character VARCHAR(255) NOT NULL,
    Alias VARCHAR(255),
    Position INT,
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE CASCADE,
    FOREIGN KEY (PlayerID) REFERENCES Person(PersonID) ON DELETE CASCADE
);

-- Create MovieGenre table (N:M relationship between Movie and Genre)
CREATE TABLE MovieGenre (
    MovieID BIGINT NOT NULL,
    GenreID BIGINT NOT NULL,
    PRIMARY KEY (MovieID, GenreID),
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE CASCADE,
    FOREIGN KEY (GenreID) REFERENCES Genre(GenreID) ON DELETE CASCADE
);