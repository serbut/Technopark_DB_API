package db.models;

/**
 * Created by sergey on 19.02.17.
 */
public class Forum {
    private int id;
    private String slug;
    private String title;
    private int userId;

    public Forum(String slug, String title, int userId) {
        this.slug = slug;
        this.title = title;
        this.userId = userId;
    }

    public Forum(int id, String slug, String title, int userId) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.userId = userId;
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

    public int getUserId() {
        return userId;
    }
}
