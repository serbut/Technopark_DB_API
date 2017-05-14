package db.controllers;

import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import db.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeybutorin on 20.02.17.
 */

@RestController
@RequestMapping(path = "/api/user")
class UserController {
    private UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

//    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

    @RequestMapping(path = "/{nickname}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
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
                if (user != null) {
                    duplicates.add(user);
                }
                dupEmail = user.getEmail();
            }
            catch (NullPointerException e) {
//                LOGGER.info("There is no user with such nickname");
            }
            if (dupEmail != null && !(email.toLowerCase()).equals(dupEmail.toLowerCase()) || dupEmail == null) { //если email найденного пользователя совпадает, то нового искать не надо
                try {
                    user = userService.getUserByEmail(email);
                    if (user != null) {
                        duplicates.add(user);
                    }
                } catch (NullPointerException e) {
//                    LOGGER.info("There is no user with such email");
                }
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicates);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getUser(@PathVariable(value="nickname") String nickname) {
        final User user = userService.getUserByNickname(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity updateUser(@PathVariable(value="nickname") String nickname, @RequestBody User body) {
        final String about = body.getAbout();
        final String email = body.getEmail();
        final String fullname = body.getFullname();
        final User user;
        try {
             user = userService.update(about, email, fullname, nickname);
        }
        catch (DuplicateKeyException e) {
//            LOGGER.info("Error updating user - duplicate values exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("");
        }
        if (user == null) {
//            LOGGER.info("Error updating user - user doesn't exists!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
