package db.services;

import db.models.Thread;
import db.models.Vote;
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
public final class ThreadService {
    private final JdbcTemplate template;

    private ThreadService(JdbcTemplate template) {
        this.template = template;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class.getName());

    public void clearTable() {
        final String dropTable = "DROP TABLE IF EXISTS thread CASCADE";
        template.execute(dropTable);
        final String dropTableVotes = "DROP TABLE IF EXISTS vote CASCADE";
        template.execute(dropTableVotes);
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
                "title VARCHAR(100) NOT NULL UNIQUE)";
        template.execute(createTableThreads);
        final String createUniqueSlug = "CREATE UNIQUE INDEX unique_slug_thread ON thread (LOWER(slug))";
        template.execute(createUniqueSlug);
        LOGGER.info("Table thread created!");
    }

    public Thread create(Thread thread) {
        template.update(new ThreadCreatePst(thread));
        thread.setId(template.queryForObject("SELECT currval(pg_get_serial_sequence('thread', 'id'))", threadIdMapper)); //возможно от этого можно избавиться
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
            return template.queryForObject("SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, SUM (v.voice) as votes FROM thread t " +
                    "JOIN forum f ON (t.forum_id=f.id)" +
                    "JOIN \"user\" u ON (u.id = t.user_id)" +
                    "LEFT JOIN vote v ON (v.thread_id = t.id)" +
                    "WHERE LOWER (t.slug) = ?" +
                    "GROUP BY t.id, nickname, created, f.slug, message, t.slug, t.title", threadMapper, slug.toLowerCase());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Thread getThreadById(int id) {
        try {
            return template.queryForObject("SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, SUM (v.voice) as votes FROM thread t " +
                    "JOIN forum f ON (t.forum_id = f.id)" +
                    "JOIN \"user\" u ON (u.id = t.user_id)" +
                    "LEFT JOIN vote v ON (v.thread_id = t.id)" +
                    "WHERE (t.id) = ?" +
                    "GROUP BY t.id, nickname, created, f.slug, message, t.slug, t.title", threadMapper, id);
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
        String sinceCreated = ""; //переписать на StringBuilder
        if (desc) {
            sort = "DESC";
            createdSign = "<=";
        } else {
            sort = "ASC";
            createdSign = ">=";
        }
        if (sinceString != null) {
            sinceCreated = "WHERE created " + createdSign + " ?";
            final Timestamp since = Timestamp.valueOf(LocalDateTime.parse(sinceString, DateTimeFormatter.ISO_DATE_TIME));
            params.add(since);
        }
        final String query = "SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, SUM (v.voice) as votes FROM thread t " +
                "JOIN forum f ON (t.forum_id = f.id AND f.slug = ?)" +
                "LEFT JOIN vote v ON (v.thread_id = t.id)" +
                "JOIN \"user\" u ON (u.id = t.user_id)" + sinceCreated +
                "GROUP BY t.id, nickname, created, f.slug, message, t.slug, t.title" +
                " ORDER BY created " + sort + " LIMIT ?";
        params.add(limit);
        return template.query(query, threadMapper, params.toArray());
    }

    private static class ThreadCreatePst implements PreparedStatementCreator {
        private final Thread thread;

        ThreadCreatePst(Thread thread) {
            this.thread = thread;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO thread (user_id, created, forum_id, message, slug, title) VALUES (" +
                    "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), ?, " +
                    "(SELECT id FROM forum WHERE LOWER (slug) = LOWER(?)), ?, ?, ?)";
            final PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, thread.getAuthor());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME)));
            pst.setString(3, thread.getForum());
            pst.setString(4, thread.getMessage());
            pst.setString(5, thread.getSlug());
            pst.setString(6, thread.getTitle());
            return pst;
        }
    }

    private final RowMapper<Integer> threadIdMapper = (rs, rowNum) -> rs.getInt("currval");

    private final RowMapper<Thread> threadMapper = (rs, rowNum) -> {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        final int id = rs.getInt("id");
        final String author = rs.getString("nickname");
        final Timestamp created = rs.getTimestamp("created");
        final String forum = rs.getString("forum_slug");
        final String message = rs.getString("message");
        final String slug = rs.getString("slug");
        final String title = rs.getString("title");
        final int votes = rs.getInt("votes");
        return new Thread(id, author, dateFormat.format(created), forum, message, slug, title, votes);
    };
}
