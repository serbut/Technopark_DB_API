package db.models;


import java.time.LocalDateTime;

/**
 * Created by sergey on 19.02.17.
 */
public class Post {
    private long id;
    private int authorId;
    private LocalDateTime created;
    private int forumId;
    private boolean isEdited;
    private String message;
    private int parentId;
    private int threadId;

    public Post(int authorId, LocalDateTime created, int forumId, String message, int parentId, int threadId) {
        this.authorId = authorId;
        this.created = created;
        this.forumId = forumId;
        this.isEdited = false;
        this.message = message;
        this.parentId = parentId;
        this.threadId = threadId;
    }

    public long getId() {
        return id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public int getForumId() {
        return  forumId;
    }

    public boolean getIsEdited() {
        return isEdited;
    }

    public String getMessage() {
        return message;
    }

    public int getParentId() {
        return parentId;
    }

    public int getThreadId() {
        return threadId;
    }
}
