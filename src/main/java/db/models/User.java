package db.models;

/**
 * Created by sergey on 19.02.17.
 */
public class User {
    private int id;
    private String about;
    private String email;
    private String fullname;
    private String nickname;

    public User(String about, String email, String fullname, String nickname) {
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
    }

    public int getId() {
        return id;
    }

    public String getAbout() {
        return about;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }
}
