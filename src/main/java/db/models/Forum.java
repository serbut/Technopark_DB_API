package db.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sergey on 19.02.17.
 */
public class Forum {
    private int id;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("title")
    private String title;
    @JsonProperty("user")
    private String user;

    @SuppressWarnings("unused")
    private Forum() {
    }

    public Forum(String slug, String title, String user) {
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public Forum(int id, String slug, String title, String user) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }
}
