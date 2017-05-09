package db.services;

import db.models.Forum;
import db.models.Post;
import db.models.Thread;
import db.models.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by sergey on 15.03.17.
 */
class Mappers {

    static final RowMapper<Integer> idMapper = (rs, rowNum) -> rs.getInt("id");

    static final RowMapper<Integer> currentIdMapper = (rs, rowNum) -> rs.getInt("currval");

    static final RowMapper<Integer> nextIdMapper = (rs, rowNum) -> rs.getInt("nextval");

    static final RowMapper<Integer> countMapper = (rs, rowNum) -> rs.getInt("count");

    static final RowMapper<User> userMapper = (rs, rowNum) -> {
        final int id = rs.getInt("id");
        final String about = rs.getString("about");
        final String nickname = rs.getString("nickname");
        final String fullname = rs.getString("fullname");
        final String email = rs.getString("email");
        return new User(id, about, email,fullname, nickname);
    };

    static final RowMapper<Forum> forumMapper = (rs, rowNum) -> {
        final int id = rs.getInt("id");
        final String slug = rs.getString("slug");
        final String title = rs.getString("title");
        final String userNickname = rs.getString("nickname");
        final int posts = rs.getInt("posts");
        final int threads = rs.getInt("threads");
        return new Forum(id, slug, title, userNickname, posts, threads);
    };

    static final RowMapper<Thread> threadMapper = (rs, rowNum) -> {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        final int id = rs.getInt("id");
        final String author = rs.getString("nickname");
        final Timestamp created = rs.getTimestamp("created");
        final String forum = rs.getString("forum_slug");
        final String message = rs.getString("message");
        final String slug = rs.getString("slug");
        final String title = rs.getString("title");
        final int votes = rs.getInt("votes");
        return new Thread(id, author, dateFormat.format(created), forum, message, slug, title, votes);
    };

    static final RowMapper<Post> postMapper = (rs, rowNum) -> {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

    static final RowMapper<Integer> voteMapper = (rs, rowNum) -> rs.getInt("votes");

}
