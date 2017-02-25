package db.models;

import java.time.LocalDateTime;

/**
 * Created by sergey on 19.02.17.
 */
public class Thread {
    private int id;
    private int authorId;
    private LocalDateTime created;
    private int forumId;
    private String message;
    private String slug;
    private String title;
    private int votes;

    public Thread(int authorId, LocalDateTime created, int forumId, String message, String slug, String title, int votes) {
        this.authorId = authorId;
        this.created = created;
        this.forumId = forumId;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
    }

    public int getId() {
        return id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreated() {
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

    public int getVotes() {
        return votes;
    }

}
