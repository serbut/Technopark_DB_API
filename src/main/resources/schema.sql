DROP TABLE IF EXISTS vote CASCADE;
DROP TABLE IF EXISTS post CASCADE;
DROP TABLE IF EXISTS thread CASCADE;
DROP TABLE IF EXISTS forum CASCADE;
DROP INDEX IF EXISTS unique_email;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS users_forum CASCADE;

DROP INDEX IF EXISTS unique_slug_thread;
DROP INDEX IF EXISTS unique_slug_forum;
DROP INDEX IF EXISTS unique_nickname;
DROP INDEX IF EXISTS idx_forum_user;
DROP INDEX IF EXISTS idx_thread_user;
DROP INDEX IF EXISTS idx_thread_forum;
DROP INDEX IF EXISTS idx_post_user;
DROP INDEX IF EXISTS idx_post_forum_id;
DROP INDEX IF EXISTS idx_uf_forum;
DROP INDEX IF EXISTS idx_uf_user;
DROP INDEX IF EXISTS idx_post_created;
DROP INDEX IF EXISTS idx_thread_created;

DROP TRIGGER IF EXISTS post_insert_trigger ON post;
DROP TRIGGER IF EXISTS thread_insert_trigger ON thread;


CREATE TABLE IF NOT EXISTS "user" (
                id SERIAL NOT NULL PRIMARY KEY,
                about TEXT,
                nickname VARCHAR(30) NOT NULL,
                fullname VARCHAR(100),
                email VARCHAR(50) NOT NULL);

CREATE UNIQUE INDEX IF NOT EXISTS unique_email ON "user" (LOWER(email));
CREATE UNIQUE INDEX IF NOT EXISTS unique_nickname ON "user" (LOWER(nickname COLLATE "ucs_basic"));

CREATE TABLE IF NOT EXISTS forum (
                id SERIAL NOT NULL PRIMARY KEY,
                slug VARCHAR(100),
                title VARCHAR(100) NOT NULL,
                posts INT NOT NULL DEFAULT 0,
                threads INT NOT NULL DEFAULT 0,
                user_id INT REFERENCES "user"(id) NOT NULL);

CREATE INDEX IF NOT EXISTS idx_forum_user ON forum(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS unique_slug_forum ON forum (LOWER(slug));

CREATE TABLE IF NOT EXISTS thread (
                id SERIAL NOT NULL PRIMARY KEY,
                user_id INT REFERENCES "user"(id) NOT NULL,
                created TIMESTAMP NOT NULL,
                forum_id INT REFERENCES forum(id) NOT NULL,
                message TEXT,
                slug VARCHAR(100),
                title VARCHAR(100) NOT NULL,
                votes INT NOT NULL DEFAULT 0);

CREATE INDEX IF NOT EXISTS idx_thread_user ON thread(user_id);
CREATE INDEX IF NOT EXISTS idx_thread_forum ON thread(forum_id);
CREATE INDEX IF NOT EXISTS idx_thread_created ON thread(created);
CREATE UNIQUE INDEX IF NOT EXISTS unique_slug_thread ON thread (LOWER(slug));

CREATE TABLE IF NOT EXISTS post (
                id SERIAL NOT NULL PRIMARY KEY,
                user_id INT REFERENCES "user"(id) NOT NULL,
                created TIMESTAMP NOT NULL,
                forum_id INT REFERENCES forum(id) NOT NULL,
                isEdited BOOLEAN DEFAULT FALSE,
                message TEXT,
                parent_id INT,
                thread_id INT REFERENCES thread(id) NOT NULL,
                path INT ARRAY);

CREATE INDEX IF NOT EXISTS idx_post_user ON post(user_id);
CREATE INDEX IF NOT EXISTS idx_post_forum_id ON post(forum_id);
CREATE INDEX IF NOT EXISTS idx_post_created ON post(created);
CREATE INDEX IF NOT EXISTS idx_post_path ON post(path);

CREATE TABLE IF NOT EXISTS vote (
                id SERIAL NOT NULL PRIMARY KEY,
                user_id INT REFERENCES "user"(id) NOT NULL,
                voice SMALLINT,
                thread_id INT REFERENCES thread(id) NOT NULL,
                UNIQUE (user_id, thread_id));

CREATE TABLE IF NOT EXISTS users_forum (
  user_id INT REFERENCES "user"(id) NOT NULL,
  forum_id INT REFERENCES forum(id) NOT NULL);

CREATE INDEX IF NOT EXISTS idx_uf_user ON users_forum (user_id);
CREATE INDEX IF NOT EXISTS idx_uf_forum ON users_forum (forum_id);

CREATE OR REPLACE FUNCTION users_forum_add() RETURNS TRIGGER AS '
  BEGIN
    INSERT INTO users_forum (user_id, forum_id) VALUES (NEW.user_id, NEW.forum_id);
    RETURN NEW;
  END;
' LANGUAGE plpgsql;


CREATE TRIGGER post_insert_trigger AFTER INSERT ON post
FOR EACH ROW EXECUTE PROCEDURE users_forum_add();

CREATE TRIGGER thread_insert_trigger AFTER INSERT ON thread
FOR EACH ROW EXECUTE PROCEDURE users_forum_add();