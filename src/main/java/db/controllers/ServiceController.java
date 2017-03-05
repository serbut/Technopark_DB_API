package db.controllers;

import db.services.ForumService;
import db.services.ThreadService;
import db.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@RestController
public class ServiceController {
    @Autowired
    private ThreadService threadServ;

    @Autowired
    private UserService userServ;

    @Autowired
    private ForumService forumServ;

    @RequestMapping(path = "/api/service/clear", method = RequestMethod.GET)
    public void deleteAllTables() {
        threadServ.clearTable();
        userServ.clearTable();
        forumServ.clearTable();
    }
    @RequestMapping(path = "/api/service/create", method = RequestMethod.GET)
    public void createAllTables() {
        threadServ.clearTable();
        userServ.clearTable();
        forumServ.clearTable();
        userServ.createTable();
        forumServ.createTable();
        threadServ.createTable();
    }
}
