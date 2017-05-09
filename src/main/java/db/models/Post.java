package db.models;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sergey on 19.02.17.
 */
public class Post {
    private int id;
    @JsonProperty("author")
    private String author;
    @JsonProperty("created")
    private String created;
    @JsonProperty("forum")
    private String forum;
    @JsonProperty("isEdited")
    private boolean isEdited;
    @JsonProperty("message")
    private String message;
    @JsonProperty("thread")
    private int threadId;
    @JsonProperty("parent")
    private int parentId = 0;

    @SuppressWarnings("unused")
    private Post() {
    }

    public Post(String author, String created, String forum, String message, boolean isEdited, int parentId, int threadId) {
        this.author = author;
        this.created = created;
        this.isEdited = isEdited;
        this.forum = forum;
        this.message = message;
        this.parentId = parentId;
        this.threadId = threadId;
    }

    public Post(int id, String author, String created, String forum, String message, boolean isEdited, int parentId, int threadId) {
        this.id = id;
        this.author = author;
        this.created = created;
        this.isEdited = isEdited;
        this.forum = forum;
        this.message = message;
        this.parentId = parentId;
        this.threadId = threadId;
    }

    public int getId() {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }
}
