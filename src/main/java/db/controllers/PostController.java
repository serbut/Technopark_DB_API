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

    @RequestMapping(path = "/api/thread/{thread}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPost(@PathVariable(value="thread") String threadSlugOrId, @RequestBody List<GetPostRequest> body) {
        int threadId = -1;
        Thread t;
        try {
            threadId = Integer.parseInt(threadSlugOrId);
            t = threadService.getThreadById(threadId);
        } catch(NumberFormatException e) {
            t = threadService.getThreadBySlug(threadSlugOrId);
        }
        List<Post> posts = new ArrayList<>();
        for(GetPostRequest postBody: body) {
            final String author = postBody.getAuthor();
            String created = postBody.getCreated();
            final String message = postBody.getMessage();
            final boolean isEdited = postBody.getIsEdited();
            if (created == null) {
                LocalDateTime a = LocalDateTime.now();
                created = a.toString() + "+03:00"; // получить актуальное время
            }
            final Post post = postService.create(new Post(author, created, message, isEdited, t.getId()));
            post.setForum(t.getForum());
            posts.add(post);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(PostListResponse(posts));
    }

    private static final class GetPostRequest {
        @JsonProperty("author")
        private String author;
        @JsonProperty("created")
        private String created;
        @JsonProperty("forum")
        private String forum;
        @JsonProperty("isEdited")
        private boolean isEdited;
        @JsonProperty("message")
        private String message;
        @JsonProperty("thread")
        private int thread;

        @SuppressWarnings("unused")
        private GetPostRequest() {
        }

        @SuppressWarnings("unused")
        private GetPostRequest(String author, String created, String forum, String message, int thread, boolean isEdited){
            this.author = author;
            this.created = created;
            this.forum = forum;
            this.message = message;
            this.thread = thread;
            this.isEdited = isEdited;
        }

        public String getAuthor() {
            return author;
        }

        public String getCreated() {
            return created;
        }

        public  String getForum() {
            return forum;
        }

        public String getMessage() {
            return message;
        }

        public int getThread() {
            return thread;
        }

        public boolean getIsEdited() {
            return isEdited;
        }
    }

    private static JSONObject PostDataResponse(Post post, String created) { //убрать created
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", post.getAuthor());
        formDetailsJson.put("created", post.getCreated());
        formDetailsJson.put("forum", post.getForum());
        formDetailsJson.put("id", post.getId());
        formDetailsJson.put("message", post.getMessage());
        formDetailsJson.put("isEdited", post.getIsEdited());
        formDetailsJson.put("thread", post.getThreadId());
        return formDetailsJson;
    }

    private String PostListResponse(List<Post> posts) {
        final JSONArray jsonArray = new JSONArray();

        for(Post p : posts) {
            if (p == null) {
                continue;
            }
            jsonArray.add(PostDataResponse(p, p.getCreated()));
        }
        return jsonArray.toString();
    }

}
