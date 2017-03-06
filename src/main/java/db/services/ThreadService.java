package db.services;

import db.models.Thread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@Service
public class ThreadService {
    private final JdbcTemplate template;

    private ThreadService(JdbcTemplate template) {
        this.template = template;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class.getName());

    public void clearTable() {
        final String dropTable = "DROP TABLE IF EXISTS thread CASCADE";
        template.execute(dropTable);
        LOGGER.info("Table thread was dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS  thread (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "user_id INT REFERENCES \"user\"(id)," +
                "created TIMESTAMP," +
                "forum_id INT REFERENCES forum(id)," +
                "message TEXT," +
                "slug VARCHAR(100)," +
                "title VARCHAR(100) NOT NULL UNIQUE)";
        template.execute(createTable);
        LOGGER.info("Table thread created!");
    }

    public Thread create(int userId, String created, int forumId, String message, String slug, String title) {
        final Thread thread = new Thread(userId, created, forumId, message, slug, title);
        try {
            template.update(new ThreadCreatePst(thread));
        } catch (DuplicateKeyException e) {
            LOGGER.info("Error creating thread - thread already exists!");
            return null;
        }
        LOGGER.info("Thread with title \"{}\" created", title);
        return thread;
    }

    public Thread getThreadBySlug(String slug) {
        try {
            return template.queryForObject("SELECT * FROM thread WHERE LOWER (slug) = ?", threadMapper, slug.toLowerCase());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Thread getThreadById(int id) {
        try {
            return template.queryForObject("SELECT * FROM thread WHERE id = ?", threadMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Thread> getThreads(int forumId, double limit, String sinceString, boolean desc) {
        final ArrayList<Object> params = new ArrayList<>();
        params.add(forumId);
        String sort, createdSign, sinceCreated = "";
        if (desc) {
            sort = "DESC";
            createdSign = "<=";
        } else {
            sort = "ASC";
            createdSign = ">=";
        }
        if (sinceString != null) {
            sinceCreated = "? AND created " + createdSign;
            Timestamp since = Timestamp.valueOf(LocalDateTime.parse(sinceString, DateTimeFormatter.ISO_DATE_TIME));
            params.add(since);
        }
        final String query = "SELECT * FROM thread WHERE forum_id = " + sinceCreated + "? ORDER BY created " + sort + " LIMIT ?";
        params.add(limit);
        return template.query(query, threadMapper, params.toArray());
    }

    private static class ThreadCreatePst implements PreparedStatementCreator {
        private final Thread thread;

        ThreadCreatePst(Thread thread) {
            this.thread = thread;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO thread (user_id, created, forum_id, message, slug, title) VALUES (?, ?, ?, ?, ?, ?)";
            final PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, thread.getUserId());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME)));
            pst.setInt(3, thread.getForumId());
            pst.setString(4, thread.getMessage());
            pst.setString(5, thread.getSlug());
            pst.setString(6, thread.getTitle());
            return pst;
        }
    }

    private final RowMapper<Thread> threadMapper = (rs, rowNum) -> {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        final int id = rs.getInt("id");
        final int userId = rs.getInt("user_id");
        final Timestamp created = rs.getTimestamp("created");
        final int forumId = rs.getInt("forum_id");
        final String message = rs.getString("message");
        final String slug = rs.getString("slug");
        final String title = rs.getString("title");
        return new Thread(id, userId, dateFormat.format(created), forumId, message, slug, title);
    };
}
