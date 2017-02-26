package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.User;
import db.services.UserService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * Created by sergeybutorin on 20.02.17.
 */

@RestController
public class UserController {
    private final UserService userServ;

    public UserController(UserService userServ) {
        this.userServ = userServ;
    }

    @RequestMapping(path = "/api/user", method = RequestMethod.GET)
    public void createTable() {
        userServ.clearTable();
        userServ.createTable();
    }

    @RequestMapping(path = "/api/user/{nickname}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createUser(@PathVariable(value="nickname") String nickname, @RequestBody GetUserRequest body) {
        String about = body.getAbout();
        String email = body.getEmail();
        String fullname = body.getFullname();
        if (StringUtils.isEmpty(email)
                || StringUtils.isEmpty(nickname)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong parameters");
        }
        User user = userServ.create(about, email, fullname, nickname);
        if (user == null) {
            final List<User> duplicates = new ArrayList<User>();
            String dupEmail = null;
            try {
                user = userServ.getUserByNickname(nickname);
                duplicates.add(user);
                dupEmail = user.getEmail();
            }
            catch (NullPointerException e) {
                LOGGER.info("There is no user with such nickname");
            }
            if (dupEmail != null && !(email.toLowerCase()).equals(dupEmail.toLowerCase()) || dupEmail == null) { //если email найденного пользователя совпадает, то нового искать не надо
                try {
                    duplicates.add(userServ.getUserByEmail(email));
                } catch (NullPointerException e) {
                    LOGGER.info("There is no user with such email");
                }
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(UserListResponse(duplicates));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDataResponse(user));
    }

    @RequestMapping(path = "/api/user/{nickname}/profile", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getUser(@PathVariable(value="nickname") String nickname) {
        final User user = userServ.getUserByNickname(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(UserDataResponse(user));
    }

    @RequestMapping(path = "/api/user/{nickname}/profile", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity updateUser(@PathVariable(value="nickname") String nickname, @RequestBody GetUserRequest body) {
        String about = body.getAbout();
        String email = body.getEmail();
        String fullname = body.getFullname();
        if (email != null) { //тут дичь какая-то
            final User checkEmail = userServ.getUserByEmail(email);
            if (checkEmail != null && !checkEmail.getNickname().equals(nickname)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("");
            }
        }
        User currentUser = userServ.getUserByNickname(nickname);
        if (email == null) {
            email = currentUser.getEmail();
        }
        if (about == null){
            about = currentUser.getAbout();
        }
        if (fullname == null) {
            fullname = currentUser.getFullname();
        }
        final User user = userServ.updateUser(about, email, fullname, nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(UserDataResponse(user));
    }

    private static final class GetUserRequest {
        @JsonProperty("about")
        private String about;
        @JsonProperty("email")
        private String email;
        @JsonProperty("fullname")
        private String fullname;
        @JsonProperty("nickname")
        private String nickname;

        @SuppressWarnings("unused")
        private GetUserRequest() {
        }

        @SuppressWarnings("unused")
        private GetUserRequest(String about, String email, String fullname, String nickname) {
            this.about = about;
            this.email = email;
            this.fullname = fullname;
            this.nickname = nickname;
        }

        public String getAbout() {
            return about;
        }

        public String getEmail() {
            return email;
        }

        public String getFullname() {
            return fullname;
        }

        public String getNickname() {
            return nickname;
        }
    }

    private static JSONObject UserDataResponse(User user) {
        JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("about", user.getAbout());
        formDetailsJson.put("email", user.getEmail());
        formDetailsJson.put("fullname", user.getFullname());
        formDetailsJson.put("nickname", user.getNickname());
        return formDetailsJson;
    }

    private static String UserListResponse(List<User> users) {
        JSONArray jsonArray = new JSONArray();

        for(User u : users) {
            if (u == null) {
                continue;
            }
            jsonArray.add(UserDataResponse(u));
        }
        return jsonArray.toString();
    }
}
