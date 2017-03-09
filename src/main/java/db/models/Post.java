package db.models;


/**
 * Created by sergey on 19.02.17.
 */
public class Post {
    private long id;
    private final String author;
    private final String created;
    private String forum;
    private final boolean isEdited;
    private final String message;
    private int parentId = 0;
    private final int threadId;

    public Post(String author, String created, String forum, String message, boolean isEdited, int threadId) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.isEdited = isEdited;
        this.message = message;
        this.parentId = 0;
        this.threadId = threadId;
    }

    public Post(String author, String created, String forum, String message, int parentId, int threadId) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.isEdited = false;
        this.message = message;
        this.parentId = parentId;
        this.threadId = threadId;
    }

    public Post(String author, String created, String message, boolean isEdited, int threadId) {
        this.author = author;
        this.created = created;
        this.isEdited = isEdited;
        this.message = message;
        this.threadId = threadId;
    }

    public long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreated() {
        return created;
    }

    public String getForum() {
        return  forum;
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

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setId(long id) {
        this.id = id;
    }
}
