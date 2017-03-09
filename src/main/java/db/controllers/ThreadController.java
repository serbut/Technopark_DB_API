package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.models.Thread;
import db.services.ForumService;
import db.services.ThreadService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;


/**
 * Created by sergeybutorin on 27.02.17.
 */
@SuppressWarnings("unchecked")
@RestController
class ThreadController {
    @Autowired
    private ThreadService threadService;

    @Autowired
    private ForumService forumService;

    @RequestMapping(path = "/api/thread", method = RequestMethod.GET)
    public void createTable() {
        threadService.clearTable();
        threadService.createTable();
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
        final Thread thread = threadService.create(new Thread(author, created, forumSlug, message, slug, title));
        return ResponseEntity.status(HttpStatus.CREATED).body(ThreadDataResponse(thread, created));
    }

    @RequestMapping(path = "/api/forum/{forum_slug}/threads", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getThreads(@PathVariable(value="forum_slug") String forumSlug, @RequestParam(name = "limit", required = false, defaultValue = "0") double limit,
                                     @RequestParam(name = "since", required = false) String sinceString,
                                     @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        final Forum forum = forumService.getForumBySlug(forumSlug); // наверное лучше убрать
        if (forum == null) {
            LOGGER.info("Error getting threads - forum with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final List<Thread> threads = threadService.getThreads(forumSlug, limit, sinceString, desc);
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

    private static JSONObject ThreadDataResponse(Thread thread, String created) { //убрать created
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", thread.getAuthor());
        formDetailsJson.put("created", created);
        formDetailsJson.put("forum", thread.getForum());
        formDetailsJson.put("id", thread.getId());
        formDetailsJson.put("message", thread.getMessage());
        formDetailsJson.put("slug", thread.getSlug());
        formDetailsJson.put("title", thread.getTitle());
        return formDetailsJson;
    }

    private String ThreadListResponse(List<Thread> threads) {
        final JSONArray jsonArray = new JSONArray();

        for(Thread t : threads) {
            if (t == null) {
                continue;
            }
            jsonArray.add(ThreadDataResponse(t, t.getCreated()));
        }
        return jsonArray.toString();
    }
}
