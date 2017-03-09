package db.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sergey on 19.02.17.
 */
public class Thread {
    private int id;
    @JsonProperty("author")
    private String author;
    @JsonProperty("created")
    private String created;
    @JsonProperty("forum")
    private String forum;
    @JsonProperty("message")
    private String message;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("title")
    private String title;
    private int votes;

    @SuppressWarnings("unused")
    private Thread() {
    }

    public Thread(String author, String created, String forum, String message, String slug, String title) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.message = message;
        this.slug = slug;
        this.title = title;
    }

    public Thread(int id, String author, String created, String forum, String message, String slug, String title) {
        this.id = id;
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.message = message;
        this.slug = slug;
        this.title = title;
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
        return forum;
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

    public int getVotes() {
        return votes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
