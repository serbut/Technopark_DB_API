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
                "parent_id INT," +
                "thread_id INT REFERENCES thread(id) NOT NULL)";
        template.execute(createTable);
        LOGGER.info("Table post created!");
    }

    public Post create(Post post) {
        try {
            template.update(new PostCreatePst(post));
            post.setId(template.queryForObject("SELECT currval(pg_get_serial_sequence('post', 'id'))", postIdMapper)); //возможно от этого можно избавиться
        } catch (DuplicateKeyException e) {
            LOGGER.info("Error creating post - post already exists!");
            return null;
        }
        LOGGER.info("Post with something created");
        return post;
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
            pst.setInt(3, post.getThread());
            pst.setString(4, post.getMessage());
            pst.setBoolean(5, post.getIsEdited());
            pst.setInt(6, post.getParentId());
            pst.setInt(7, post.getThread());
            return pst;
        }
    }

    private final RowMapper<Integer> postIdMapper = (rs, rowNum) -> rs.getInt("currval");

}
