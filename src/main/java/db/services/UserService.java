package db.services;

import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sergey on 25.02.17.
 */

@Service
@Transactional
public class UserService {
    private final JdbcTemplate template;
    public UserService(JdbcTemplate template) {
        this.template = template;
    }
//    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class.getName());

    public void clearTable() {
        final String clearTable = "TRUNCATE TABLE \"user\" CASCADE";
        template.execute(clearTable);
//        LOGGER.info("Table user was cleared");
    }

    public User create(String about, String email, String fullname, String nickname) {
        final User user = new User(about, email, fullname, nickname);
        try {
            final String query = "INSERT INTO \"user\" (about, nickname, fullname, email) VALUES (?, ?, ?, ?)";
            template.update(query, about, nickname, fullname, email);
        }
        catch (DuplicateKeyException e) {
//            LOGGER.info("Error creating user - user already exists!");
            return null;
        }
//        LOGGER.info("User with nickname \"{}\" and email \"{}\" created", nickname, email);
        return user;
    }

    public User update(String about, String email, String fullname, String nickname) {
        final String query = "UPDATE \"user\" SET " +
                "about = COALESCE (?, about), " +
                "email = COALESCE (?, email), " +
                "fullname = COALESCE (?, fullname)" +
                "WHERE LOWER (nickname COLLATE \"ucs_basic\") = LOWER (? COLLATE \"ucs_basic\")";
        final int rows = template.update(query, about, email, fullname, nickname);
        if (rows == 0) {
//            LOGGER.info("Error update user profile because user with such nickname does not exist!");
            return null;
        }
        return getUserByNickname(nickname);
    }

    public User getUserByNickname(String nickname) {
        try {
            return template.queryForObject("SELECT * FROM \"user\" WHERE LOWER (nickname COLLATE \"ucs_basic\") = (? COLLATE \"ucs_basic\")", Mappers.userMapper, nickname.toLowerCase());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User getUserByEmail(String email) {
        try {
            return template.queryForObject("SELECT * FROM \"user\" WHERE LOWER (email) = ?", Mappers.userMapper, email.toLowerCase());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int getCount() {
        return template.queryForObject("SELECT COUNT(*) FROM \"user\"", Mappers.countMapper);
    }

    public List<User> getUsersForum (int forumId, int limit, String since, boolean desc) {
        final ArrayList<Object> params = new ArrayList<>();
        params.add(forumId);
        final String sort;
        final String createdSign;
        String sinceCreated = "";
        if (desc) {
            sort = "DESC";
            createdSign = "<";
        } else {
            sort = "ASC";
            createdSign = ">";
        }
        if (since != null) {
            sinceCreated = "AND LOWER (nickname COLLATE \"ucs_basic\") " + createdSign + " LOWER (? COLLATE \"ucs_basic\") ";
            params.add(since);
        }

        final String query = "SELECT id, about, nickname COLLATE \"ucs_basic\", fullname, email FROM \"user\" " +
                "WHERE id IN (SELECT user_id FROM users_forum WHERE forum_id = ?) " +
                sinceCreated +
                " ORDER BY LOWER (nickname COLLATE \"ucs_basic\") " + sort + " LIMIT ?";

        params.add(limit);
        return template.query(query, Mappers.userMapper, params.toArray());
    }
}
