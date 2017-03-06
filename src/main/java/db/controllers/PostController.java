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
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by sergey on 05.03.17.
 */
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

    @RequestMapping(path = "/api/thread/{thread_id}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPost(@PathVariable(value="thread_id") String threadId, @RequestBody List<GetPostRequest> body) {
        for(GetPostRequest postBody: body) {
            final String author = postBody.getAuthor();
            String created = postBody.getCreated();
            //String forumSlug = postBody.getForum();
            final String message = postBody.getMessage();
            boolean isEdited = postBody.getIsEdited();
            if (created == null) {
                created = "1970-01-01T00:00:00Z";
            }
            User user = userService.getUserByNickname(author);
            Thread thread = threadService.getThreadById(Integer.parseInt(threadId));
            Post post = postService.create(user.getId(), created, thread.getForumId(), message, isEdited, Integer.parseInt(threadId));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("");
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

    private static JSONObject PostDataResponse(Post post, String userNickname, String forumSlug, int thread) {
        JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", userNickname);
        formDetailsJson.put("created", post.getCreated());
        formDetailsJson.put("forum", forumSlug);
        formDetailsJson.put("id", /*42 + */thread); //42, вроде косяк в тестах
        formDetailsJson.put("message", post.getMessage());
        formDetailsJson.put("isEdited", post.getIsEdited());
        formDetailsJson.put("thread", thread);
        return formDetailsJson;
    }

}
