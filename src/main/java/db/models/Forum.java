package db.models;

/**
 * Created by sergey on 19.02.17.
 */
public class Forum {
    private int id;
    private final String slug;
    private final String title;
    private final String user;

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
