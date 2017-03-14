package db.services;

import db.models.Post;
import db.models.Thread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


/**
 * Created by sergey on 05.03.17.
 */
@Service
public final class PostService {
    private final JdbcTemplate template;

    private PostService(JdbcTemplate template) {
        this.template = template;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class.getName());

    public void clearTable() {
        final String clearTable = "TRUNCATE TABLE post CASCADE";
        template.execute(clearTable);
        LOGGER.info("Table post was cleared");
    }

    public void deleteTable() {
        final String dropTable = "DROP TABLE IF EXISTS post CASCADE";
        template.execute(dropTable);
        LOGGER.info("Table post was dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS post (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "user_id INT REFERENCES \"user\"(id) NOT NULL ," +
                "created TIMESTAMP," +
                "forum_id INT REFERENCES forum(id) NOT NULL ," +
                "isEdited BOOLEAN DEFAULT FALSE," +
                "message TEXT," +
                "parent_id INT," +
                "thread_id INT REFERENCES thread(id) NOT NULL)";
        template.execute(createTable);
        LOGGER.info("Table post created!");
    }

    public Post create(Post post) {
        try {
            template.update(new PostCreatePst(post));
            post.setId(template.queryForObject("SELECT currval(pg_get_serial_sequence('post', 'id'))", postCurrentIdMapper)); //возможно от этого можно избавиться
        } catch (DuplicateKeyException e) {
            LOGGER.info("Error creating post - post already exists!");
            return null;
        }
        LOGGER.info("Post with id \"{}\" created", post.getId());
        return post;
    }

    public List<Post> getPostsFlat(String threadSlug, int limit, int offset, boolean desc) {
        String query = "SELECT p.id, nickname, p.created, f.slug, isEdited, p.message, parent_id, thread_id FROM post p " +
                "JOIN thread t ON (p.thread_id = t.id AND t.slug = ?)" +
                "JOIN forum f ON (t.forum_id = f.id)" +
                "JOIN \"user\" u ON (u.id = p.user_id)" +
                "ORDER BY created " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
        return template.query(query, postMapper, threadSlug, limit, offset);
    }

    public List<Post> getPostsTree(String threadSlug, int limit, int offset, boolean desc) {
        String query = "WITH RECURSIVE tree (id, user_id, created, forum_id, isEdited, message, parent_id, thread_id, posts) AS ( " +
                "SELECT id, user_id, created, forum_id, isEdited, message, parent_id, thread_id, array[id] FROM post WHERE parent_id = 0 " +
                "UNION ALL " +
                "SELECT p.id, p.user_id, p.created, p.forum_id, p.isEdited, p.message, p.parent_id, p.thread_id, array_append(posts, p.id) FROM post p " +
                "JOIN tree ON tree.id = p.parent_id) " +
                "SELECT tr.id, nickname, tr.created, f.slug, isEdited, tr.message, tr.parent_id, tr.thread_id, array_to_string(posts, ' ') AS posts FROM tree tr " +
                "JOIN thread t ON (tr.thread_id = t.id AND t.slug = ?) " +
                "JOIN forum f ON (t.forum_id = f.id) " +
                "JOIN \"user\" u ON (u.id = tr.user_id) " +
                "ORDER BY posts " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
        return template.query(query, postMapper, threadSlug, limit, offset);
    }
    public List<Post> getPostsParentsTree(String threadSlug, boolean desc, List<Integer> parentIds) {
        String query = null;
        List<Post> result = new ArrayList<>();
        for (Integer id : parentIds) {
            query = "WITH RECURSIVE tree (id, user_id, created, forum_id, isEdited, message, parent_id, thread_id, posts) AS ( " +
                    "SELECT id, user_id, created, forum_id, isEdited, message, parent_id, thread_id, array[id] FROM post WHERE id = ? " +
                    "UNION ALL " +
                    "SELECT p.id, p.user_id, p.created, p.forum_id, p.isEdited, p.message, p.parent_id, p.thread_id, array_append(posts, p.id) FROM post p " +
                    "JOIN tree ON tree.id = p.parent_id) " +
                    "SELECT tr.id, nickname, tr.created, f.slug, isEdited, tr.message, tr.parent_id, tr.thread_id, array_to_string(posts, ' ') AS posts FROM tree tr " +
                    "JOIN thread t ON (tr.thread_id = t.id AND t.slug = ?) " +
                    "JOIN forum f ON (t.forum_id = f.id) " +
                    "JOIN \"user\" u ON (u.id = tr.user_id) " +
                    "ORDER BY posts " + (desc ? "DESC" : "ASC");
            result.addAll(template.query(query, postMapper, id, threadSlug));
        }
        return result;
    }

    public List<Integer> getParents(String threadSlug, int limit, int offset, boolean desc) {
        String parentsQuery = "SELECT p.id FROM post p " +
                "JOIN thread t ON (t.id = p.thread_id) " +
                "WHERE parent_id = 0 AND t.slug = ? " +
                "ORDER BY p.id " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
        return template.query(parentsQuery, postIdMapper, threadSlug, limit, offset);
    }

    public Post getPostById(int id) {
        try {
            return template.queryForObject("SELECT p.id, nickname, created, f.slug as slug, p.message, thread_id, isEdited, parent_id FROM post p " +
                    "JOIN forum f ON (p.forum_id = f.id) " +
                    "JOIN \"user\" u ON (u.id = p.user_id) " +
                    "WHERE (p.id) = ?", postMapper, id);
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
            LOGGER.info("Error update thread profile because post with such id does not exist!");
            return null;
        }
        return getPostById(id);
    }

    public int getCount() {
        return template.queryForObject("SELECT COUNT(*) FROM post", countMapper);
    }

    private static class PostCreatePst implements PreparedStatementCreator {
        private final Post post;

        PostCreatePst(Post post) {
            this.post = post;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO post (user_id, created, forum_id, message, isEdited, parent_id, thread_id) VALUES (" +
                    "(SELECT id FROM \"user\" WHERE nickname = ?), ?, " +
                    "(SELECT f.id FROM forum f JOIN thread t ON (t.forum_id = f.id AND t.id = ?)), " +
                    "?, ?, ?, ?)";
            final PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, post.getAuthor());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME)));
            pst.setInt(3, post.getThreadId());
            pst.setString(4, post.getMessage());
            pst.setBoolean(5, post.getIsEdited());
            pst.setInt(6, post.getParentId());
            pst.setInt(7, post.getThreadId());
            return pst;
        }
    }

    private final RowMapper<Integer> postIdMapper = (rs, rowNum) -> rs.getInt("id"); //вынести

    private final RowMapper<Integer> postCurrentIdMapper = (rs, rowNum) -> rs.getInt("currval"); //вынести

    private final RowMapper<Integer> countMapper = (rs, rowNum) -> rs.getInt("count"); //вынести

    private final RowMapper<Post> postMapper = (rs, rowNum) -> {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        final int id = rs.getInt("id");
        final String author = rs.getString("nickname");
        final Timestamp created = rs.getTimestamp("created");
        final String forum = rs.getString("slug");
        final boolean isEdited = rs.getBoolean("isEdited");
        final String message = rs.getString("message");
        final int parentId = rs.getInt("parent_id");
        final int threadId = rs.getInt("thread_id");
        return new Post(id, author, dateFormat.format(created), forum, message, isEdited, parentId, threadId);
    };

}
