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
        Thread t;
        try {
            t = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            t = threadService.getThreadBySlug(threadSlugOrId);
        }
        List<Post> posts = new ArrayList<>();
        for(Post postBody: body) {
            final String author = postBody.getAuthor();
            String created = postBody.getCreated();
            final String message = postBody.getMessage();
            final boolean isEdited = postBody.getIsEdited();
            final int parentId = postBody.getParentId();
            if (created == null) {
                LocalDateTime a = LocalDateTime.now();
                created = a.toString() + "+03:00"; // получить актуальное время
            }
            final Post post = postService.create(new Post(author, created, message, isEdited, parentId, t.getId()));
            post.setForum(t.getForum());
            posts.add(post);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(PostListResponse(posts));
    }

    @RequestMapping(path = "/api/forum/{thread_slug_or_id}/posts", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getPosts(@PathVariable(value="thread_slug_or_id") String forumSlug,
                                   @RequestParam(name = "limit", required = false, defaultValue = "0") double limit,
                                   @RequestParam(name = "marker", required = false) String marker,  //маркер может быть получен из какого-либо предыдущего запроса
                                   @RequestParam(name = "sort", required = false, defaultValue = "flat") String sort,
                                   @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {


    }

    private static JSONObject PostDataResponse(Post post) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", post.getAuthor());
        formDetailsJson.put("created", post.getCreated());
        formDetailsJson.put("forum", post.getForum());
        formDetailsJson.put("id", post.getId());
        formDetailsJson.put("message", post.getMessage());
        formDetailsJson.put("parent", post.getParentId());
        formDetailsJson.put("isEdited", post.getIsEdited());
        formDetailsJson.put("thread", post.getThread());
        return formDetailsJson;
    }

    private String PostListResponse(List<Post> posts) {
        final JSONArray jsonArray = new JSONArray();

        for(Post p : posts) {
            if (p == null) {
                continue;
            }
            jsonArray.add(PostDataResponse(p));
        }
        return jsonArray.toString();
    }

}
