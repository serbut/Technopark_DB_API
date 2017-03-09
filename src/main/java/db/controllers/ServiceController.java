package db.controllers;

import db.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@RestController
class ServiceController {
    @Autowired
    private PostService postService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private UserService userService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private VoteService voteService;

    @RequestMapping(path = "/api/service/clear", method = RequestMethod.GET)
    public void deleteAllTables() {
        voteService.clearTable();
        postService.clearTable();
        threadService.clearTable();
        userService.clearTable();
        forumService.clearTable();
    }
    @RequestMapping(path = "/api/service/create", method = RequestMethod.GET)
    public void createAllTables() {
        voteService.clearTable();
        postService.clearTable();
        threadService.clearTable();
        userService.clearTable();
        forumService.clearTable();
        userService.createTable();
        forumService.createTable();
        threadService.createTable();
        postService.createTable();
        voteService.createTable();
    }
}
