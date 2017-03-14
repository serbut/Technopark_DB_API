package db.services;

import db.models.Forum;
import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergey on 26.02.17.
 */
@Service
public final class ForumService {
    private final JdbcTemplate template;
    private ForumService(JdbcTemplate template) {
        this.template = template;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumService.class.getName());

    public void clearTable() {
        final String dropTable = "DROP TABLE IF EXISTS forum CASCADE";
        template.execute(dropTable);
        final String dropUniqueSlug = "DROP INDEX IF EXISTS unique_slug_forum";
        template.execute(dropUniqueSlug);
        LOGGER.info("Table forum dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS  forum (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "slug VARCHAR(100)," +
                "title VARCHAR(100) NOT NULL ," +
                "user_id INT REFERENCES \"user\"(id) NOT NULL)";
        template.execute(createTable);
        final String createUniqueSlug = "CREATE UNIQUE INDEX unique_slug_forum ON forum (LOWER(slug))";
        template.execute(createUniqueSlug);
        LOGGER.info("Table forum created!");
    }

    public Forum create(Forum forum) {
        template.update(new ForumPst(forum));
        LOGGER.info("Forum with slug \"{}\" created", forum.getSlug());
        return forum;
    }

    private static class ForumPst implements PreparedStatementCreator {
        private final Forum forum;
        ForumPst(Forum forum) {
            this.forum = forum;
        }
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO forum (slug, title, user_id) VALUES (?, ?, (" +
                    "SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)))";
            final PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, forum.getSlug());
            pst.setString(2, forum.getTitle());
            pst.setString(3, forum.getUser());
            return pst;
        }
    }

    public Forum getForumBySlug(String slug) {
        try {
            return template.queryForObject("WITH forum AS (SELECT f.id AS id, f.slug AS slug, f.title AS title, nickname, p.id AS pid, t.id AS tid FROM forum f " + //переписать эту дичь
                            "JOIN \"user\" u ON (u.id = f.user_id) LEFT JOIN post p ON (p.forum_id = f.id) " +                                                          //но работает!
                            "LEFT JOIN thread t ON (t.forum_id = f.id) WHERE LOWER (f.slug) = LOWER (?))" +
                            "SELECT DISTINCT f.id, f.slug, f.title, f.nickname, p.count AS posts, t.count AS threads FROM forum f " +
                            "CROSS JOIN (SELECT COUNT (pid) AS \"count\" FROM forum f GROUP BY f.id, f.slug, f.title, f.nickname, f.tid) AS p " +
                            "CROSS JOIN (SELECT COUNT (tid) AS \"count\" FROM forum f GROUP BY f.id, f.slug, f.title, f.nickname, f.pid) AS t ", forumMapper, slug);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private final RowMapper<Forum> forumMapper = (rs, rowNum) -> {
        final int id = rs.getInt("id");
        final String slug = rs.getString("slug");
        final String title = rs.getString("title");
        final String userNickname = rs.getString("nickname");
        final int posts = rs.getInt("posts");
        final int threads = rs.getInt("threads");
        return new Forum(id, slug, title, userNickname, posts, threads);
    };
}
