package db.controllers;

import db.models.Forum;
import db.models.Post;
import db.models.Thread;
import db.models.User;
import db.responses.PostDetailResponse;
import db.services.ForumService;
import db.services.PostService;
import db.services.ThreadService;
import db.services.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * Created by sergey on 05.03.17.
 */
@SuppressWarnings("unchecked")
@RestController
@RequestMapping(path = "/api/post")
class PostController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class.getName());

    private ThreadService threadService;
    private UserService userService;
    private ForumService forumService;
    private PostService postService;

    @Autowired
    PostController(UserService userService, ForumService forumService, ThreadService threadService, PostService postService) {
        this. userService = userService;
        this.forumService = forumService;
        this.threadService = threadService;
        this.postService = postService;
    }

    @RequestMapping(path = "/{id}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getPostDetails(@PathVariable(value="id") int postId,
                                         @RequestParam(name = "related", required = false) ArrayList<String> related) {
        final Post post = postService.getPostById(postId);
        if (post == null) {
            LOGGER.info("Post with such id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        User author = null;
        Forum forum = null;
        Thread thread = null;
        if (related != null) {
            if (related.contains("user")) {
                author = userService.getUserByNickname(post.getAuthor());
            }
            if (related.contains("forum")) {
                forum = forumService.getForumBySlug(post.getForum());
            }
            if (related.contains("thread")) {
                thread = threadService.getThreadById(post.getThreadId());
            }
        }
        return ResponseEntity.ok(new PostDetailResponse(author, forum, thread, post));
    }

    @RequestMapping(path = "/{id}/details", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity updatePostDetails(@PathVariable(value="id") int postId, @RequestBody Post body) {
        final String message = body.getMessage();
        Post post = postService.getPostById(postId);
        if (post == null) {
            LOGGER.info("Post with such id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        if (message != null && !message.equals(post.getMessage())) {
            post = postService.update(postId, message);
        }
        return ResponseEntity.ok(post);
    }
}
