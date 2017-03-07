package db.services;

import db.models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        final String dropTable = "DROP TABLE IF EXISTS post CASCADE";
        template.execute(dropTable);
        LOGGER.info("Table post was dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS  post (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "user_id INT REFERENCES \"user\"(id) NOT NULL ," +
                "created TIMESTAMP," +
                "forum_id INT REFERENCES forum(id) NOT NULL ," +
                "isEdited BOOLEAN DEFAULT FALSE," +
                "message TEXT," +
                "parent_id INT REFERENCES post(id) DEFAULT 0," +
                "thread_id INT REFERENCES thread(id) NOT NULL)";
        template.execute(createTable);
        LOGGER.info("Table post created!");
    }

    public Post create(int userId, String created, int forumId, String message, boolean isEdited, int threadId) {
        final Post post = new Post(userId, created, forumId, message, isEdited, threadId);
        try {
            template.update(new PostCreatePst(post));
        } catch (DuplicateKeyException e) {
            LOGGER.info("Error creating post - post already exists!");
            return null;
        }
        LOGGER.info("Post with created");
        return post;
    }

    private static class PostCreatePst implements PreparedStatementCreator {
        private final Post post;

        PostCreatePst(Post post) {
            this.post = post;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO post (user_id, created, forum_id, message, isEdited, thread_id) VALUES (?, ?, ?, ?, ?, ?)";
            final PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, post.getUserId());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME)));
            pst.setInt(3, post.getForumId());
            pst.setString(4, post.getMessage());
            pst.setBoolean(5, post.getIsEdited());
            pst.setInt(6, post.getThreadId());
            return pst;
        }
    }
}
