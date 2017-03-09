package db.models;

/**
 * Created by sergey on 19.02.17.
 */
public class Thread {
    private int id;
    private final String author;
    private final String created;
    private final String forum;
    private final String message;
    private final String slug;
    private final String title;

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

    public void setId(int id) {
        this.id = id;
    }
}
