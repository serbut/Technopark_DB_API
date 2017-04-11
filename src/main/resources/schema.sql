DROP TABLE IF EXISTS vote CASCADE;
DROP TABLE IF EXISTS post CASCADE;
DROP TABLE IF EXISTS thread CASCADE;
DROP INDEX IF EXISTS unique_slug_forum;
DROP TABLE IF EXISTS forum CASCADE;
DROP INDEX IF EXISTS unique_email;
DROP INDEX IF EXISTS unique_nickname;
DROP TABLE IF EXISTS "user" CASCADE;


CREATE TABLE IF NOT EXISTS  "user" (
                id SERIAL NOT NULL PRIMARY KEY,
                about TEXT,
                nickname VARCHAR(30) NOT NULL UNIQUE,
                fullname VARCHAR(100),
                email VARCHAR(50) NOT NULL UNIQUE);

CREATE UNIQUE INDEX unique_email ON "user" (LOWER(email));
CREATE UNIQUE INDEX unique_nickname ON "user" (LOWER(nickname));

CREATE TABLE IF NOT EXISTS  forum (
                id SERIAL NOT NULL PRIMARY KEY,
                slug VARCHAR(100),
                title VARCHAR(100) NOT NULL ,
                user_id INT REFERENCES "user"(id) NOT NULL);

CREATE UNIQUE INDEX unique_slug_forum ON forum (LOWER(slug));

CREATE TABLE IF NOT EXISTS thread (
                id SERIAL NOT NULL PRIMARY KEY,
                user_id INT REFERENCES "user"(id) NOT NULL,
                created TIMESTAMP DEFAULT NOW(),
                forum_id INT REFERENCES forum(id) NOT NULL,
                message TEXT,
                slug VARCHAR(100),
                title VARCHAR(100) NOT NULL);

CREATE UNIQUE INDEX unique_slug_thread ON thread (LOWER(slug));

CREATE TABLE IF NOT EXISTS post (
                id SERIAL NOT NULL PRIMARY KEY,
                user_id INT REFERENCES "user"(id) NOT NULL ,
                created TIMESTAMP,
                forum_id INT REFERENCES forum(id) NOT NULL ,
                isEdited BOOLEAN DEFAULT FALSE,
                message TEXT,
                parent_id INT,
                thread_id INT REFERENCES thread(id) NOT NULL);

CREATE TABLE IF NOT EXISTS  vote (
                id SERIAL NOT NULL PRIMARY KEY,
                user_id INT REFERENCES "user"(id) NOT NULL,
                voice SMALLINT,
                thread_id INT REFERENCES thread(id) NOT NULL,
                UNIQUE (user_id, thread_id));