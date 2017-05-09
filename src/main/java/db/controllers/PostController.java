package db.controllers;

import db.models.Post;
import db.models.Thread;
import db.services.ForumService;
import db.services.PostService;
import db.services.ThreadService;
import db.services.UserService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
        JSONObject author = null;
        JSONObject forum = null;
        JSONObject thread = null;
        final Post post = postService.getPostById(postId);
        if (post == null) {
            LOGGER.info("Post with such id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        if (related != null) {
            if (related.contains("user")) {
                author = UserController.userDataResponse(userService.getUserByNickname(post.getAuthor()));
            }
            if (related.contains("forum")) {
                forum = ForumController.forumDataResponse(forumService.getForumBySlug(post.getForum()));
            }
            if (related.contains("thread")) {
                thread = ThreadController.threadDataResponse(threadService.getThreadById(post.getThreadId()));
            }
        }
        return ResponseEntity.ok(postDetailResponse(author, forum, postDataResponse(post), thread));
    }

    @RequestMapping(path = "/{id}/details", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity updatePostDetails(@PathVariable(value="id") int postId, @RequestBody Post body) {
        final String message = body.getMessage();
        Post post;
        post = postService.getPostById(postId);
        if (post == null) {
            LOGGER.info("Post with such id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        if (message != null && !message.equals(post.getMessage())) {
            post = postService.update(postId, message);
        }
        return ResponseEntity.ok(postDataResponse(post));
    }

    private static JSONObject postDataResponse(Post post) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", post.getAuthor());
        formDetailsJson.put("created", post.getCreated());
        formDetailsJson.put("forum", post.getForum());
        formDetailsJson.put("id", post.getId());
        formDetailsJson.put("message", post.getMessage());
        formDetailsJson.put("parent", post.getParentId());
        formDetailsJson.put("isEdited", post.getIsEdited());
        formDetailsJson.put("thread", post.getThreadId());
        return formDetailsJson;
    }

    static JSONObject sortResponse(JSONArray result, String marker) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("marker", marker);
        formDetailsJson.put("posts", result);
        return formDetailsJson;
    }

    static JSONObject postDetailResponse(JSONObject author, JSONObject forum ,JSONObject post, JSONObject thread) {
        final JSONObject postDetailsJson = new JSONObject();
        postDetailsJson.put("author", author);
        postDetailsJson.put("forum", forum);
        postDetailsJson.put("post", post);
        postDetailsJson.put("thread", thread);
        return postDetailsJson;
    }

    static JSONArray postListResponse(List<Post> posts) {
        final JSONArray jsonArray = new JSONArray();
        for(Post p : posts) {
            if (p == null) {
                continue;
            }
            jsonArray.add(postDataResponse(p));
        }
        return jsonArray;
    }

}
