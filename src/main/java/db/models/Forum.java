package db.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sergey on 19.02.17.
 */
public class Forum {
    private int id;
    private String slug;
    private String title;
    private String user;
    private int posts;
    private int threads;

    @SuppressWarnings("unused")
    private Forum() {
    }

    public Forum(String slug, String title, String user) {
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public Forum(int id, String slug, String title, String user, int posts, int threads) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.user = user;
        this.posts = posts;
        this.threads = threads;
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

    public int getPosts() {
        return posts;
    }

    public int getThreads() {
        return threads;
    }
}
