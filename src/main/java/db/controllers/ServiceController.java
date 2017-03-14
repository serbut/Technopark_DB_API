package db.controllers;

import db.models.Post;
import db.services.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(path = "/api/service/clear", method = RequestMethod.POST)
    public void clearAllTables() {
        voteService.clearTable();
        postService.clearTable();
        threadService.clearTable();
        userService.clearTable();
        forumService.clearTable();
    }
    @RequestMapping(path = "/api/service/create", method = RequestMethod.GET)
    public void createAllTables() {
        voteService.deleteTable();
        postService.deleteTable();
        threadService.deleteTable();
        userService.deleteTable();
        forumService.deleteTable();
        userService.createTable();
        forumService.createTable();
        threadService.createTable();
        postService.createTable();
        voteService.createTable();
    }

    @RequestMapping(path = "/api/service/status", method = RequestMethod.GET)
    public ResponseEntity getStatus() {
        return ResponseEntity.ok(statusResponse(forumService.getCount(), postService.getCount(), threadService.getCount(), userService.getCount()));
    }

    private static JSONObject statusResponse(int forum, int post, int thread, int user) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("forum", forum);
        formDetailsJson.put("post", post);
        formDetailsJson.put("thread", thread);
        formDetailsJson.put("user", user);
        return formDetailsJson;
    }
}
