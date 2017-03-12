package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.models.Thread;
import db.models.Vote;
import db.services.ForumService;
import db.services.ThreadService;
import db.services.UserService;
import db.services.VoteService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
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

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/api/thread", method = RequestMethod.GET)
    public void createTable() {
        threadService.clearTable();
        threadService.createTable();
    }

    @RequestMapping(path = "/api/forum/{forum_slug}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createThread(@PathVariable(value="forum_slug") String forumSlug, @RequestBody Thread body) {
        String author = body.getAuthor();
        String created = body.getCreated();
        final String message = body.getMessage();
        final String slug = body.getSlug();
        final String title = body.getTitle();
        if (created == null) {
            created = "1970-01-01T00:00:00Z";
        }
        Thread thread = null;
        try {
            author = userService.getUserByNickname(author).getNickname();//убрать это
            forumSlug = forumService.getForumBySlug(forumSlug).getSlug(); //и это!
            thread = threadService.create(new Thread(author, created, forumSlug, message, slug, title));
        } catch (NullPointerException e) { //убрать это
            LOGGER.info("Error creating thread - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        } catch (DuplicateKeyException e) {
            try {
                thread = threadService.getThreadBySlug(slug);
            }
            catch (NullPointerException ex) {
                LOGGER.info("There is no thread with such slug");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ThreadDataResponse(thread));
        }
        catch (DataAccessException e) {
            LOGGER.info("Error creating thread - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ThreadDataResponse(thread));
    }

    @RequestMapping(path = "/api/thread/{thread_slug_or_id}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getSingleThread(@PathVariable(value="thread_slug_or_id") String threadSlugOrId) {
        Thread t;
        try {
            t = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            t = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (t == null) {
            LOGGER.info("Thread with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ThreadDataResponse(t));
    }

    @RequestMapping(path = "/api/forum/{forum_slug}/threads", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getThreads(@PathVariable(value="forum_slug") String forumSlug,
                                     @RequestParam(name = "limit", required = false, defaultValue = "0") int limit,
                                     @RequestParam(name = "since", required = false) String sinceString,
                                     @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        final Forum forum = forumService.getForumBySlug(forumSlug); // наверное лучше убрать
        if (forum == null) {
            LOGGER.info("Error getting threads - forum with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final List<Thread> threads = threadService.getThreads(forumSlug, limit, sinceString, desc);
        return ResponseEntity.status(HttpStatus.OK).body(ThreadListResponse(threads).toJSONString());
    }

    static JSONObject ThreadDataResponse(Thread thread) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", thread.getAuthor());
        formDetailsJson.put("created", thread.getCreated());
        formDetailsJson.put("forum", thread.getForum());
        formDetailsJson.put("id", thread.getId());
        formDetailsJson.put("message", thread.getMessage());
        formDetailsJson.put("slug", thread.getSlug());
        formDetailsJson.put("title", thread.getTitle());
        formDetailsJson.put("votes", thread.getVotes());
        return formDetailsJson;
    }

    private JSONArray ThreadListResponse(List<Thread> threads) {
        final JSONArray jsonArray = new JSONArray();

        for(Thread t : threads) {
            if (t == null) {
                continue;
            }
            jsonArray.add(ThreadDataResponse(t));
        }
        return jsonArray;
    }
}
