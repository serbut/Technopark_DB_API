package db.services;

import db.models.Forum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by sergey on 26.02.17.
 */
@Service
@Transactional
public class ForumService {
    private final JdbcTemplate template;
    public ForumService(JdbcTemplate template) {
        this.template = template;
    }

    public void clearTable() {
        final String clearTable = "TRUNCATE TABLE forum CASCADE";
        template.execute(clearTable);
    }

    public Forum create(Forum forum) {
        template.update("INSERT INTO forum (slug, title, user_id) VALUES (?, ?," +
                "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)))", forum.getSlug(), forum.getTitle(), forum.getUser());
        return forum;
    }

    public int getCount() {
        return template.queryForObject("SELECT COUNT(*) FROM forum", Mappers.countMapper);
    }

    public Forum getForumBySlug(String slug) {
        try {
            return template.queryForObject("SELECT * FROM forum f JOIN \"user\" u ON (u.id = f.user_id) WHERE LOWER (f.slug) = LOWER (?)", Mappers.forumMapper, slug);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
