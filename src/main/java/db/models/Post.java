package db.models;


/**
 * Created by sergey on 19.02.17.
 */
public class Post {
    private long id;
    private final int userId;
    private final String created;
    private final int forumId;
    private final boolean isEdited;
    private final String message;
    private final int parentId;
    private final int threadId;

    public Post(int userId, String created, int forumId, String message, boolean isEdited, int threadId) {
        this.userId = userId;
        this.created = created;
        this.forumId = forumId;
        this.isEdited = isEdited;
        this.message = message;
        this.parentId = 0;
        this.threadId = threadId;
    }

    public Post(int userId, String created, int forumId, String message, int parentId, int threadId) {
        this.userId = userId;
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

    public int getUserId() {
        return userId;
    }

    public String getCreated() {
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
