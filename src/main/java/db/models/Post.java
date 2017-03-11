package db.models;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sergey on 19.02.17.
 */
public class Post {
    private long id;
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
    private int thread;
    @JsonProperty("parent")
    private int parentId = 0;

    @SuppressWarnings("unused")
    private Post() {
    }

    public Post(String author, String created, String message, boolean isEdited, int parentId, int thread) {
        this.author = author;
        this.created = created;
        this.isEdited = isEdited;
        this.message = message;
        this.parentId = parentId;
        this.thread = thread;
    }

    public Post(String author, String created, String message, boolean isEdited, int thread) {
        this.author = author;
        this.created = created;
        this.isEdited = isEdited;
        this.message = message;
        this.thread = thread;
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

    public int getThread() {
        return thread;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setId(long id) {
        this.id = id;
    }
}
