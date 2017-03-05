package db.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Created by sergey on 19.02.17.
 */
public class Thread {
    private int id;
    private int userId;
    private String created;
    private int forumId;
    private String message;
    private String slug;
    private String title;

    public Thread(int userId, String created, int forumId, String message, String slug, String title) {
        this.userId = userId;
        this.created = created;
        this.forumId = forumId;
        this.message = message;
        this.slug = slug;
        this.title = title;
    }

    public Thread(int id, int userId, String created, int forumId, String message, String slug, String title) {
        this.id = id;
        this.userId = userId;
        this.created = created;
        this.forumId = forumId;
        this.message = message;
        this.slug = slug;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getCreated() {
        return created;
    }

    public int getForumId() {
        return forumId;
    }

    public String getMessage() {
        return message;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }
}
