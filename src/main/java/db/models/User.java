package db.models;

/**
 * Created by sergey on 19.02.17.
 */
public class User {
    private int id;
    private String about;
    private String email;
    private String fullname;
    private String nickname; //добавить ограничения: Данное поле допускает только латиницу, цифры и знак подчеркивания. Сравнение имени регистронезависимо.

    public User(String about, String email, String fullname, String nickname) {
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
    }

    public User(int id, String about, String email, String fullname, String nickname) {
        this.id = id;
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
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

    public int getId() {
        return id;
    }
}
