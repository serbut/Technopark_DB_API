package db.models;

/**
 * Created by sergey on 19.02.17.
 */
public class Thread {
    private int id;
    private final int userId;
    private final String created;
    private final int forumId;
    private final String message;
    private final String slug;
    private final String title;

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
