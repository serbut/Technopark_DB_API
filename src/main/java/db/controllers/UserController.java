package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import db.services.UserService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeybutorin on 20.02.17.
 */

@SuppressWarnings("unchecked")
@RestController
class UserController {
    @Autowired
    private UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

    @RequestMapping(path = "/api/user/{nickname}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createUser(@PathVariable(value="nickname") String nickname, @RequestBody User body) {
        final String about = body.getAbout();
        final String email = body.getEmail();
        final String fullname = body.getFullname();
        User user = userService.create(about, email, fullname, nickname);
        if (user == null) {
            final List<User> duplicates = new ArrayList<>();
            String dupEmail = null;
            try {
                user = userService.getUserByNickname(nickname);
                duplicates.add(user);
                dupEmail = user.getEmail();
            }
            catch (NullPointerException e) {
                LOGGER.info("There is no user with such nickname");
            }
            if (dupEmail != null && !(email.toLowerCase()).equals(dupEmail.toLowerCase()) || dupEmail == null) { //если email найденного пользователя совпадает, то нового искать не надо
                try {
                    duplicates.add(userService.getUserByEmail(email));
                } catch (NullPointerException e) {
                    LOGGER.info("There is no user with such email");
                }
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(userListResponse(duplicates));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userDataResponse(user));
    }

    @RequestMapping(path = "/api/user/{nickname}/profile", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getUser(@PathVariable(value="nickname") String nickname) {
        final User user = userService.getUserByNickname(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(userDataResponse(user));
    }

    @RequestMapping(path = "/api/user/{nickname}/profile", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity updateUser(@PathVariable(value="nickname") String nickname, @RequestBody User body) {
        final String about = body.getAbout();
        final String email = body.getEmail();
        final String fullname = body.getFullname();
        final User user;
        try {
             user = userService.update(about, email, fullname, nickname);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error updating user - duplicate values exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("");
        }
        if (user == null) {
            LOGGER.info("Error updating user - user doesn't exists!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(userDataResponse(user));
    }

    static JSONObject userDataResponse(User user) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("about", user.getAbout());
        formDetailsJson.put("email", user.getEmail());
        formDetailsJson.put("fullname", user.getFullname());
        formDetailsJson.put("nickname", user.getNickname());
        return formDetailsJson;
    }

    static String userListResponse(List<User> users) {
        final JSONArray jsonArray = new JSONArray();

        for(User u : users) {
            if (u == null) {
                continue;
            }
            jsonArray.add(userDataResponse(u));
        }
        return jsonArray.toString();
    }
}
