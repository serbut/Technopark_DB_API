package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.models.Thread;
import db.models.User;
import db.services.ForumService;
import db.services.ThreadService;
import db.services.UserService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@RestController
public class ThreadController {
    @Autowired
    private ThreadService threadServ;

    @Autowired
    private UserService userServ;

    @Autowired
    private ForumService forumServ;

    @RequestMapping(path = "/api/thread", method = RequestMethod.GET)
    public void createTable() {
        threadServ.clearTable();
        threadServ.createTable();
    }

    @RequestMapping(path = "/api/forum/{forum_slug}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createThread(@PathVariable(value="forum_slug") String forumSlug, @RequestBody GetThreadRequest body) {
        final String author = body.getAuthor();
        final Timestamp created = body.getCreated();
        final String message = body.getMessage();
        final String slug = body.getSlug();
        final String title = body.getTitle();
        final int userId;
        User user = userServ.getUserByNickname(author);
        Forum forum = forumServ.getForumBySlug(forumSlug);
        Thread thread = threadServ.create(user.getId(), created, forum.getId(), message, slug, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(ThreadDataResponse(thread, author, forumSlug));
    }

    @RequestMapping(path = "/api/forum/{forum_slug}/threads", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getThreads(@PathVariable(value="forum_slug") String forumSlug, @RequestParam(name = "limit", required = false) double limit,
                                    @RequestParam(name = "since", required = false) Timestamp since, @RequestParam(name = "desc", required = false) boolean desc) {
        Forum forum = forumServ.getForumBySlug(forumSlug);
        if(limit = null) {

        }
        List<Thread> threads = threadServ.getThreadsBy(forum.getId(), limit, since, desc);
        return ResponseEntity.status(HttpStatus.OK).body(ThreadListResponse(threads));
    }

    private static final class GetThreadRequest {
        @JsonProperty("author")
        private String author;
        @JsonProperty("created")
        private Timestamp created;
        @JsonProperty("forum")
        private String forum;
        @JsonProperty("message")
        private String message;
        @JsonProperty("slug")
        private String slug;
        @JsonProperty("title")
        private String title;


        @SuppressWarnings("unused")
        private GetThreadRequest() {
        }

        @SuppressWarnings("unused")
        private GetThreadRequest(String author, Timestamp created, String forum, String message, String slug, String title){
            this.author = author;
            this.created = created;
            this.forum = forum;
            this.message = message;
            this.slug = slug;
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public Timestamp getCreated() {
            return created;
        }

        public  String getForum() {
            return forum;
        }

        public String getMessage() {
            return message;
        }

        public String getSlug() {
            return slug;
        }

        public String getTitle() {
            return title;
        }
    }

    private static JSONObject ThreadDataResponse(Thread thread, String userNickname, String forumSlug) {
        Instant date = thread.getCreated().toInstant();
        JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", userNickname);
        formDetailsJson.put("created", thread.getCreated().toInstant().toString());
        formDetailsJson.put("forum", forumSlug);
        formDetailsJson.put("message", thread.getMessage());
        formDetailsJson.put("slug", thread.getSlug());
        formDetailsJson.put("title", thread.getTitle());
        return formDetailsJson;
    }

    private String ThreadListResponse(List<Thread> threads) {
        JSONArray jsonArray = new JSONArray();

        for(Thread t : threads) {
            if (t == null) {
                continue;
            }
            Forum f = forumServ.getForumById(t.getForumId()); //тут все совсем плохо
            User u = userServ.getUserById(t.getUserId());//тут все совсем плохо
            jsonArray.add(ThreadDataResponse(t, u.getNickname(), f.getSlug()));
        }
        return jsonArray.toString();
    }
}
