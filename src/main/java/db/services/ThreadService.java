package db.services;

import db.models.Forum;
import db.models.Thread;
import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@Service
public class ThreadService {
    private final JdbcTemplate template;

    public ThreadService(JdbcTemplate template) {
        this.template = template;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class.getName());

    public void clearTable() {
        final String dropTable = "DROP TABLE IF EXISTS thread CASCADE";
        template.execute(dropTable);
        /*final String dropUniqueSlug = "DROP INDEX IF EXISTS unique_slug";
        template.execute(dropUniqueSlug);
        final String dropUniqueNickname = "DROP INDEX IF EXISTS unique_nickname";
        template.execute(dropUniqueNickname);*/
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
        /*final String createUniqueSlug = "CREATE UNIQUE INDEX unique_slug ON forum (LOWER(slug))";
        template.execute(createUniqueSlug);
        final String createUniqueNickname = "CREATE UNIQUE INDEX unique_nickname ON \"user\" (LOWER(nickname))";
        template.execute(createUniqueNickname);*/
        LOGGER.info("Table thread created!");
    }

    public Thread create(int userId, Timestamp created, int forumId, String message, String slug, String title) {
        final Thread thread = new Thread(userId, created, forumId, message, slug, title);
        try {
            template.update(new ThreadCreatePst(thread));
        } catch (DuplicateKeyException e) {
            LOGGER.info("Error creating forum - forum already exists!");
            return null;
        }
        LOGGER.info("Forum with slug \"{}\" created", slug);
        return thread;
    }

    public List<Thread> getThreadsBy(int forumId, double limit, Timestamp since, boolean desc) {
        String sort = null;
        sort = (desc) ? "DESC" :  "ASC";
        final String query = "SELECT * FROM thread WHERE forum_id = ? AND created >= ? LIMIT ? ORDER BY created ?";
        final List<Thread> threads = template.query(query, threadMapper, forumId, since, limit, sort);
        return threads;
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
            pst.setTimestamp(2, thread.getCreated());
            pst.setInt(3, thread.getForumId());
            pst.setString(4, thread.getMessage());
            pst.setString(5, thread.getSlug());
            pst.setString(6, thread.getTitle());
            return pst;
        }
    }

    private final RowMapper<Thread> threadMapper = (rs, rowNum) -> {
        final int id = rs.getInt("id");
        final int userId = rs.getInt("user_id");
        final Timestamp created = rs.getTimestamp("created");
        final int forumId = rs.getInt("forum_id");
        final String message = rs.getString("message");
        final String slug = rs.getString("slug");
        final String title = rs.getString("title");
        return new Thread(id, userId, created, forumId, message, slug, title);
    };
}
