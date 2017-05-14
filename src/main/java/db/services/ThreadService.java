package db.services;

import db.models.Thread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@Service
@Transactional
public class ThreadService {
    private final JdbcTemplate template;

    public ThreadService(JdbcTemplate template) {
        this.template = template;
    }

//    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class.getName());

    public void clearTable() {
        final String clearTable = "TRUNCATE TABLE thread CASCADE";
        template.execute(clearTable);
//        LOGGER.info("Table thread was cleared");
    }

    public Thread create(Thread thread) {
        if (thread.getCreated() != null) {
            Timestamp time = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
            thread.setId(template.queryForObject("INSERT INTO thread (user_id, created, forum_id, message, slug, title) VALUES (" +
                            "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), ?, " +
                            "(SELECT id FROM forum WHERE LOWER (slug) = LOWER(?)), ?, ?, ?) RETURNING id", Mappers.idMapper, thread.getAuthor(), time,
                    thread.getForum(), thread.getMessage(), thread.getSlug(), thread.getTitle()));
        } else {
            thread.setId(template.queryForObject("INSERT INTO thread (user_id, forum_id, message, slug, title) VALUES (" +
                            "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), " +
                            "(SELECT id FROM forum WHERE LOWER (slug) = LOWER(?)), ?, ?, ?) RETURNING id", Mappers.idMapper, thread.getAuthor(),
                    thread.getForum(), thread.getMessage(), thread.getSlug(), thread.getTitle()));
        }
        template.update("UPDATE forum SET threads = threads + 1 WHERE slug = ?", thread.getForum());
//        LOGGER.info("Thread with title \"{}\" created", thread.getTitle());
        return thread;
    }

    public Thread update(int id, String message, String title) {
        final String query = "UPDATE thread SET " +
                "message = COALESCE (?, message), " +
                "title = COALESCE (?, title)" +
                "WHERE id = ?";
        final int rows = template.update(query, message, title, id);
        if (rows == 0) {
//            LOGGER.info("Error update thread because thread with such slug does not exist!");
            return null;
        }
        return getThreadById(id);
    }

    public Thread getThreadBySlugOrId(String threadSlugOrId) {
        if (threadSlugOrId.matches("[-+]?\\d*\\.?\\d+")) {
            return getThreadById(Integer.parseInt(threadSlugOrId));
        } else {
            return getThreadBySlug(threadSlugOrId);
        }
    }

    public Thread getThreadBySlug(String slug) {
        try {
            return template.queryForObject("SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, votes FROM thread t " +
                    "JOIN forum f ON (t.forum_id=f.id)" +
                    "JOIN \"user\" u ON (u.id = t.user_id)" +
                    "WHERE LOWER (t.slug) = ?", Mappers.threadMapper, slug.toLowerCase());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Thread getThreadById(int id) {
        try {
            return template.queryForObject("SELECT t.id, nickname, created, f.slug as forum_slug, message, t.slug, t.title, votes FROM thread t " +
                    "JOIN forum f ON (t.forum_id = f.id)" +
                    "JOIN \"user\" u ON (u.id = t.user_id)" +
                    "WHERE t.id = ?", Mappers.threadMapper, id);
        } catch (EmptyResultDataAccessException e) {
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
            params.add(new Timestamp(ZonedDateTime.parse(ZonedDateTime.parse(sinceString).format(DateTimeFormatter.ISO_DATE_TIME)).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli()));
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
}
