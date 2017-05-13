package db.controllers;

import db.responses.StatusResponse;
import db.services.*;
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
    private UserService userService;
    private ForumService forumService;
    private ThreadService threadService;
    private PostService postService;
    private VoteService voteService;

    @Autowired
    ServiceController(UserService userService, ForumService forumService, ThreadService threadService, PostService postService, VoteService voteService) {
        this.userService = userService;
        this.forumService = forumService;
        this.threadService = threadService;
        this.postService = postService;
        this.voteService = voteService;
    }

    @RequestMapping(path = "/api/service/clear", method = RequestMethod.POST)
    public void clearAllTables() {
        voteService.clearTable();
        postService.clearTable();
        threadService.clearTable();
        userService.clearTable();
        forumService.clearTable();
    }

    @RequestMapping(path = "/api/service/status", method = RequestMethod.GET)
    public ResponseEntity getStatus() {
        return ResponseEntity.ok(new StatusResponse(userService.getCount(), forumService.getCount(), threadService.getCount(), postService.getCount()));
    }
}
