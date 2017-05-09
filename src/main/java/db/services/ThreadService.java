package db.services;

import db.models.Thread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@Service
public final class ThreadService {
    private final JdbcTemplate template;

    private ThreadService(JdbcTemplate template) {
        this.template = template;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class.getName());

    public void clearTable() {
        final String clearTable = "TRUNCATE TABLE thread CASCADE";
        template.execute(clearTable);
        LOGGER.info("Table thread was cleared");
    }

    public void deleteTable() {
        final String dropTable = "DROP TABLE IF EXISTS thread CASCADE";
        template.execute(dropTable);
        LOGGER.info("Table thread was dropped");
    }

    public void createTable() {
        final String createTableThreads = "CREATE TABLE IF NOT EXISTS  thread (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "user_id INT REFERENCES \"user\"(id) NOT NULL," +
                "created TIMESTAMP," +
                "forum_id INT REFERENCES forum(id) NOT NULL," +
                "message TEXT," +
                "slug VARCHAR(100)," +
                "title VARCHAR(100) NOT NULL)";
        template.execute(createTableThreads);
        final String createUniqueSlug = "CREATE UNIQUE INDEX unique_slug_thread ON thread (LOWER(slug))";
        template.execute(createUniqueSlug);
        LOGGER.info("Table thread created!");
    }

    public Thread create(Thread thread) {
        Timestamp time = null;
        if (thread.getCreated() != null) {
            time = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME).minusHours(3));
        }
        thread.setId(template.queryForObject("INSERT INTO thread (user_id, created, forum_id, message, slug, title) VALUES (" +
                "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), ?, " +
                        "(SELECT id FROM forum WHERE LOWER (slug) = LOWER(?)), ?, ?, ?) RETURNING id", Mappers.idMapper, thread.getAuthor(), time,
                thread.getForum(), thread.getMessage(), thread.getSlug(), thread.getTitle()));
        template.update("UPDATE forum SET threads = threads + 1 WHERE slug = ?", thread.getForum());
        LOGGER.info("Thread with title \"{}\" created", thread.getTitle());
        return thread;
    }

    public Thread update(String slug, String message, String title) {
        final String query = "UPDATE thread SET " +
                "message = COALESCE (?, message), " +
                "title = COALESCE (?, title)" +
                "WHERE LOWER (slug) = LOWER (?)";
        final int rows = template.update(query, message, title, slug);
        if (rows == 0) {
            LOGGER.info("Error update thread profile because thread with such slug does not exist!");
            return null;
        }
        return getThreadBySlug(slug);
    }

    public Thread getThreadBySlug(String slug) {
        try {
            return template.queryForObject("SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, votes FROM thread t " +
                    "JOIN forum f ON (t.forum_id=f.id)" +
                    "JOIN \"user\" u ON (u.id = t.user_id)" +
                    "WHERE LOWER (t.slug) = ?", Mappers.threadMapper, slug.toLowerCase());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Thread getThreadById(int id) {
        try {
            return template.queryForObject("SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, votes FROM thread t " +
                    "JOIN forum f ON (t.forum_id = f.id)" +
                    "JOIN \"user\" u ON (u.id = t.user_id)" +
                    "WHERE (t.id) = ?", Mappers.threadMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Thread> getThreads(String forumSlug, int limit, String sinceString, boolean desc) {
        final ArrayList<Object> params = new ArrayList<>();
        params.add(forumSlug);
        final String sort;
        final String createdSign;
        if (desc) {
            sort = "DESC";
            createdSign = "<=";
        } else {
            sort = "ASC";
            createdSign = ">=";
        }
        String sinceCreated = " ";
        if (sinceString != null) {
            sinceCreated = "WHERE created " + createdSign + " ? ";
            params.add(Timestamp.valueOf(LocalDateTime.parse(sinceString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }
        final String query = "SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, votes FROM thread t " +
                "JOIN forum f ON (t.forum_id = f.id AND LOWER(f.slug) = LOWER(?))" +
                "JOIN \"user\" u ON (u.id = t.user_id) " + sinceCreated +
                " ORDER BY created " + sort + " LIMIT ?";
        params.add(limit);
        return template.query(query, Mappers.threadMapper, params.toArray());
    }

    public int getCount() {
        return template.queryForObject("SELECT COUNT(*) FROM thread", Mappers.countMapper);
    }

    private static class ThreadCreatePst implements PreparedStatementCreator {
        private final Thread thread;

        ThreadCreatePst(Thread thread) {
            this.thread = thread;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO thread (user_id, created, forum_id, message, slug, title) VALUES (" +
                    "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), ?, " +
                    "(SELECT id FROM forum WHERE LOWER (slug) = LOWER(?)), ?, ?, ?)";
            final PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, thread.getAuthor());
            if (thread.getCreated() != null) {
                pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME).minusHours(3)));
            } else {
                pst.setTimestamp(2, null);
            }
            pst.setString(3, thread.getForum());
            pst.setString(4, thread.getMessage());
            pst.setString(5, thread.getSlug());
            pst.setString(6, thread.getTitle());
            return pst;
        }
    }
}
