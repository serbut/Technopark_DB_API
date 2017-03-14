package db.services;

import db.models.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by sergeybutorin on 09.03.17.
 */

@Service
public class VoteService {
    private final JdbcTemplate template;

    private VoteService(JdbcTemplate template) {
        this.template = template;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteService.class.getName());

    public void clearTable() {
        final String clearTable = "TRUNCATE TABLE vote CASCADE";
        template.execute(clearTable);
        LOGGER.info("Table vote was dropped");
    }

    public void deleteTable() {
        final String dropTableVotes = "DROP TABLE IF EXISTS vote CASCADE";
        template.execute(dropTableVotes);
        LOGGER.info("Table vote was dropped");
    }

    public void createTable() {
        final String createTableVotes = "CREATE TABLE IF NOT EXISTS  vote (" +
                "id SERIAL NOT NULL PRIMARY KEY," +
                "user_id INT REFERENCES \"user\"(id) NOT NULL," +
                "voice SMALLINT," +
                "thread_id INT REFERENCES thread(id) NOT NULL," +
                "UNIQUE (user_id, thread_id)\n)";
        template.execute(createTableVotes);
        LOGGER.info("Table vote was created!");
    }

    public void addVote(Vote vote) {
        try {
            final String query = "INSERT INTO vote (user_id, voice, thread_id) VALUES (" +
                    "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), ?, ?)";
            template.update(query, vote.getAuthor(), vote.getVoice(), vote.getThreadId());
            LOGGER.info("Vote added");
        } catch (DuplicateKeyException e) {
            final String query = "UPDATE vote SET voice = ? " +
                    "WHERE user_id IN (SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)) AND thread_id = ?";
            template.update(query, vote.getVoice(), vote.getAuthor(), vote.getThreadId());
            LOGGER.info("Vote updated");
        }
    }

    public int getVotesForThread(int id) {
        return template.queryForObject("SELECT SUM(voice) as votes FROM vote " +
                "WHERE (thread_id) = ?", voteMapper, id);
    }

    private final RowMapper<Integer> voteMapper = (rs, rowNum) -> rs.getInt("votes");
}
