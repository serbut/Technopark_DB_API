package db.services;

import db.models.Forum;
import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
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
    public ForumService(JdbcTemplate template) {
        this.template = template;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumService.class.getName());

    @Autowired
    private UserService userServ;

    public void clearTable() {
        final String dropTable = "DROP TABLE IF EXISTS forum CASCADE";
        template.execute(dropTable);
        /*final String dropUniqueEmail = "DROP INDEX IF EXISTS unique_email";
        template.execute(dropUniqueEmail);
        final String dropUniqueNickname = "DROP INDEX IF EXISTS unique_nickname";
        template.execute(dropUniqueNickname);*/
        LOGGER.info("Table forum was dropped");
    }

    public void createTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS  forum (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "slug VARCHAR(100)," +
                "title VARCHAR(100) NOT NULL UNIQUE," +
                "user_id INT REFERENCES \"user\"(id))";
        template.execute(createTable);
        /*final String createUniqueEmail = "CREATE UNIQUE INDEX unique_email ON \"user\" (LOWER(email))";
        template.execute(createUniqueEmail);
        final String createUniqueNickname = "CREATE UNIQUE INDEX unique_nickname ON \"user\" (LOWER(nickname))";
        template.execute(createUniqueNickname);*/
        LOGGER.info("Table forum created!");
    }

    public Forum create(String slug, String title, String user_nickname) {
        final User user = userServ.getUserByNickname(user_nickname);
        final Forum forum = new Forum(slug, title, user.getId());
        try {
            template.update(new ForumPst(forum));
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating forum - forum already exists!");
            return null;
        }
        LOGGER.info("Forum with slug created", slug);
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
}
