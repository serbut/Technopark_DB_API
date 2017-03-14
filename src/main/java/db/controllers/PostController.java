package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.models.Post;
import db.models.Thread;
import db.models.User;
import db.services.ForumService;
import db.services.PostService;
import db.services.ThreadService;
import db.services.UserService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * Created by sergey on 05.03.17.
 */
@SuppressWarnings("unchecked")
@RestController
class PostController {
    @Autowired
    private ThreadService threadService;

    @Autowired
    private UserService userService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private PostService postService;

    @RequestMapping(path = "/api/thread/{thread_slug_or_id}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPost(@PathVariable(value="thread_slug_or_id") String threadSlugOrId, @RequestBody List<Post> body) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Error creating posts - thread with such slug/id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        List<Post> posts = new ArrayList<>();
        for(Post postBody: body) {
            String author = postBody.getAuthor();
            String created = postBody.getCreated();
            final String message = postBody.getMessage();
            final boolean isEdited = postBody.getIsEdited();
            final int parentId = postBody.getParentId();
            try { // этот блок вынести
                author = userService.getUserByNickname(author).getNickname();//убрать это
            } catch (NullPointerException e) {
                LOGGER.info("Error creating post - user not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
            }
            if (created == null) {
                LocalDateTime a = LocalDateTime.now();
                created = a.toString() + "+03:00";
            }
            final Post post = postService.create(new Post(author, created, message, isEdited, parentId, thread.getId()));
            post.setForum(thread.getForum());
            posts.add(post);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(postListResponse(posts).toJSONString());
    }

    @RequestMapping(path = "/api/thread/{thread_slug_or_id}/posts", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getPosts(@PathVariable(value="thread_slug_or_id") String threadSlugOrId,
                                   @RequestParam(name = "limit", required = false, defaultValue = "0") int limit,
                                   @RequestParam(name = "marker", required = false, defaultValue = "0") String marker,
                                   @RequestParam(name = "sort", required = false, defaultValue = "flat") String sort,
                                   @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Error getting posts - thread with such slug/id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        int markerInt = Integer.parseInt(marker);
        List<Post> posts = null;
        switch (sort) {
            case "flat":
                posts = postService.getPostsFlat(thread.getSlug(), limit, markerInt, desc);
                if(!posts.isEmpty()){
                    markerInt += posts.size();
                }
                break;
            case "tree":
                posts = postService.getPostsTree(thread.getSlug(), limit, markerInt, desc);
                if(!posts.isEmpty()){
                    markerInt += posts.size();
                }
                break;
            case "parent_tree":
                List<Integer> parentIds = postService.getParents(thread.getSlug(), limit, markerInt, desc);
                if(!parentIds.isEmpty()){
                    markerInt += parentIds.size();
                }
                posts = postService.getPostsParentsTree(thread.getSlug(), desc, parentIds);
                break;
        }
        return ResponseEntity.ok(sortResponse(postListResponse(posts), String.valueOf(markerInt)));
    }

    @RequestMapping(path = "/api/post/{id}/details", method = RequestMethod.GET, produces = "application/json")
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

    @RequestMapping(path = "/api/post/{id}/details", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
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

    private static JSONObject sortResponse(JSONArray result, String marker) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("marker", marker);
        formDetailsJson.put("posts", result);
        return formDetailsJson;
    }

    private static JSONObject postDetailResponse(JSONObject author, JSONObject forum ,JSONObject post, JSONObject thread) {
        final JSONObject postDetailsJson = new JSONObject();
        postDetailsJson.put("author", author);
        postDetailsJson.put("forum", forum);
        postDetailsJson.put("post", post);
        postDetailsJson.put("thread", thread);
        return postDetailsJson;
    }

    private JSONArray postListResponse(List<Post> posts) {
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
