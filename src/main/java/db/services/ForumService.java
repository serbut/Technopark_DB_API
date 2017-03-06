package db.services;

import db.models.Forum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by sergey on 26.02.17.
 */
@Service
public class ForumService {
    private final JdbcTemplate template;
    private ForumService(JdbcTemplate template) {
        this.template = template;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumService.class.getName());

    public void clearTable() {
        final String dropTable = "DROP TABLE IF EXISTS forum CASCADE";
        template.execute(dropTable);
        final String dropUniqueSlug = "DROP INDEX IF EXISTS unique_slug";
        template.execute(dropUniqueSlug);
        /*final String dropUniqueNickname = "DROP INDEX IF EXISTS unique_nickname";
        template.execute(dropUniqueNickname);*/
        LOGGER.info("Table forum dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS  forum (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "slug VARCHAR(100)," +
                "title VARCHAR(100) NOT NULL UNIQUE," +
                "user_id INT REFERENCES \"user\"(id))";
        template.execute(createTable);
        final String createUniqueSlug = "CREATE UNIQUE INDEX unique_slug ON forum (LOWER(slug))";
        template.execute(createUniqueSlug);
        /*final String createUniqueNickname = "CREATE UNIQUE INDEX unique_nickname ON \"user\" (LOWER(nickname))";
        template.execute(createUniqueNickname);*/
        LOGGER.info("Table forum created!");
    }

    public Forum create(String slug, String title, int userId) {
        final Forum forum = new Forum(slug, title, userId);
        try {
            template.update(new ForumPst(forum));
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating forum - forum already exists!");
            return null;
        }
        LOGGER.info("Forum with slug \"{}\" created", slug);
        return forum;
    }

    private static class ForumPst implements PreparedStatementCreator {
        private final Forum forum;
        ForumPst(Forum forum) {
            this.forum = forum;
        }
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO forum (slug, title, user_id) VALUES (?, ?, ?)";
            final PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, forum.getSlug());
            pst.setString(2, forum.getTitle());
            pst.setInt(3, forum.getUserId());
            return pst;
        }
    }

    public Forum getForumBySlug(String slug) {
        try {
            return template.queryForObject("SELECT * FROM forum WHERE LOWER (slug) = ?", forumMapper, slug.toLowerCase());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Forum getForumById(int id) {
        try {
            return template.queryForObject("SELECT * FROM forum WHERE id = ?", forumMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private final RowMapper<Forum> forumMapper = (rs, rowNum) -> {
        final int id = rs.getInt("id");
        final String slug = rs.getString("slug");
        final String title = rs.getString("title");
        final int userId = rs.getInt("user_id");
        return new Forum(id, slug, title, userId);
    };
}
