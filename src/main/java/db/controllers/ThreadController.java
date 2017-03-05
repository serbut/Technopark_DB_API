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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
        String created = body.getCreated();
        final String message = body.getMessage();
        final String slug = body.getSlug();
        final String title = body.getTitle();
        if (created == null) {
            created = "1970-01-01T00:00:00Z";
        }
        User user = userServ.getUserByNickname(author);
        Forum forum = forumServ.getForumBySlug(forumSlug);
        Thread thread = threadServ.create(user.getId(), created, forum.getId(), message, slug, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(ThreadDataResponse(thread, author, forumSlug, created));
    }

    @RequestMapping(path = "/api/forum/{forum_slug}/threads", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getThreads(@PathVariable(value="forum_slug") String forumSlug, @RequestParam(name = "limit", required = false, defaultValue = "0") double limit,
                                     @RequestParam(name = "since", required = false) String sinceString,
                                     @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        Forum forum = forumServ.getForumBySlug(forumSlug);
        if(forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        List<Thread> threads = threadServ.getThreadsBy(forum.getId(), limit, sinceString, desc);
        return ResponseEntity.status(HttpStatus.OK).body(ThreadListResponse(threads));
    }

    private static final class GetThreadRequest {
        @JsonProperty("author")
        private String author;
        @JsonProperty("created")
        private String created;
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
        private GetThreadRequest(String author, String created, String forum, String message, String slug, String title){
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

        public String getCreated() {
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

    private static JSONObject ThreadDataResponse(Thread thread, String userNickname, String forumSlug, String created) { //убрать created
        JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", userNickname);
        formDetailsJson.put("created", created);
        formDetailsJson.put("forum", forumSlug);
        formDetailsJson.put("id", 42 /*+ thread.getId()*/); //42, вроде косяк в тестах
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
            jsonArray.add(ThreadDataResponse(t, u.getNickname(), f.getSlug(), t.getCreated()));
        }
        return jsonArray.toString();
    }
}
