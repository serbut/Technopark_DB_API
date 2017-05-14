package db.services;

import db.models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by sergey on 05.03.17.
 */
@Service
@Transactional
public class PostService {
    private final JdbcTemplate template;

    public PostService(JdbcTemplate template) {
        this.template = template;
    }

//    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class.getName());

    public void clearTable() {
        final String clearTable = "TRUNCATE TABLE post CASCADE";
        template.execute(clearTable);
//        LOGGER.info("Table post was cleared");
    }

    public List<Post> create(List<Post> posts) {
        final String query = "INSERT INTO post (id, user_id, created, forum_id, message, isEdited, parent_id, thread_id, path) VALUES (?, " +
                "(SELECT id FROM \"user\" WHERE nickname = ?), ?, " +
                "(SELECT f.id FROM forum f JOIN thread t ON (t.forum_id = f.id AND t.id = ?)), " +
                "?, ?, ?, ?, array_append((SELECT path FROM post WHERE id = ?), ?))";
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try(Connection conn = template.getDataSource().getConnection();
            PreparedStatement pst = conn.prepareStatement(query, Statement.NO_GENERATED_KEYS)) {
            for (Post post : posts) {
                post.setId(template.queryForObject("SELECT nextval(pg_get_serial_sequence('post', 'id'))", Mappers.nextIdMapper));
                post.setCreated(dateFormat.format(created));
                pst.setInt(1, post.getId());
                pst.setString(2, post.getAuthor());
                pst.setTimestamp(3, created);
                pst.setInt(4, post.getThreadId());
                pst.setString(5, post.getMessage());
                pst.setBoolean(6, post.getIsEdited());
                pst.setInt(7, post.getParentId());
                pst.setInt(8, post.getThreadId());
                pst.setInt(9, post.getParentId());
                pst.setInt(10, post.getId());
                pst.addBatch();
//                LOGGER.info("Post with id \"{}\" created", post.getId());
            }
            pst.executeBatch();
            template.update("UPDATE forum SET posts = posts + ? WHERE slug = ?", posts.size(), posts.get(0).getForum());

        } catch (SQLException e) {
//            LOGGER.info("Error creating post" + e.getMessage());
            return null;
        }
        return posts;
    }

    public List<Post> getPostsFlat(int threadId, int limit, int offset, boolean desc) {
        final String query = "SELECT p.id, nickname, p.created, f.slug, isEdited, p.message, parent_id, thread_id FROM post p " +
                "JOIN forum f ON (p.forum_id = f.id)" +
                "JOIN \"user\" u ON (u.id = p.user_id)" +
                "WHERE p.thread_id = ? " +
                "ORDER BY created " + (desc ? "DESC" : "ASC") + ", p.id " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
        return template.query(query, Mappers.postMapper, threadId, limit, offset);
    }

    public List<Post> getPostsTree(int threadId, int limit, int offset, boolean desc) {
        final String query = "SELECT p.id, nickname, p.created, f.slug, isEdited, p.message, parent_id, thread_id FROM post p " +
                "JOIN \"user\" u ON (u.id = p.user_id) " +
                "JOIN forum f ON (p.forum_id = f.id) " +
                "WHERE p.thread_id = ?  " +
                "ORDER BY p.path " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
        return template.query(query, Mappers.postMapper, threadId, limit, offset);
    }
    public List<Post> getPostsParentsTree(int threadId, boolean desc, List<Integer> parentIds) {
        final List<Post> result = new ArrayList<>();
        for (Integer id : parentIds) {
            final String query = "SELECT p.id, nickname, p.created, f.slug, isEdited, p.message, parent_id, thread_id FROM post p " +
                    "JOIN \"user\" u ON (u.id = p.user_id) " +
                    "JOIN forum f ON (p.forum_id = f.id) " +
                    "WHERE p.path[1] = ? AND p.thread_id = ?  " +
                    "ORDER BY path " + (desc ? "DESC" : "ASC") + ", p.id " + (desc ? "DESC" : "ASC");
            result.addAll(template.query(query, Mappers.postMapper, id, threadId));
        }
        return result;
    }

    public List<Integer> getParents(int threadId, int limit, int offset, boolean desc) {
        final String parentsQuery = "SELECT p.id FROM post p " +
                "WHERE parent_id = 0 AND p.thread_id = ? " +
                "ORDER BY p.id " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
        return template.query(parentsQuery, Mappers.idMapper, threadId, limit, offset);
    }

    public Post getPostById(int id) {
        try {
            return template.queryForObject("SELECT p.id, nickname, created, f.slug as slug, p.message, thread_id, isEdited, parent_id FROM post p " +
                    "JOIN forum f ON (p.forum_id = f.id) " +
                    "JOIN \"user\" u ON (u.id = p.user_id) " +
                    "WHERE (p.id) = ?", Mappers.postMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Post update(int id, String message) {
        final String query = "UPDATE post SET " +
                "message = ?, " +
                "isEdited = true " +
                "WHERE id = ?";
        final int rows = template.update(query, message, id);
        if (rows == 0) {
//            LOGGER.info("Error update thread profile because post with such id does not exist!");
            return null;
        }
        return getPostById(id);
    }

    public int getCount() {
        return template.queryForObject("SELECT COUNT(*) FROM post", Mappers.countMapper);
    }
}
