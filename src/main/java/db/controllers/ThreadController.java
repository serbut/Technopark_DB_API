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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@SuppressWarnings("unchecked")
@RestController
class ThreadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadController.class.getName());

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadDataResponse(thread));
        }
        catch (DataAccessException e) {
            LOGGER.info("Error creating thread - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(threadDataResponse(thread));
    }

    @RequestMapping(path = "/api/thread/{thread_slug_or_id}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getSingleThread(@PathVariable(value="thread_slug_or_id") String threadSlugOrId) {
        Thread thread;
        try {
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Thread with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(threadDataResponse(thread));
    }

    @RequestMapping(path = "/api/thread/{thread_slug_or_id}/details", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity updateThread(@PathVariable(value="thread_slug_or_id") String threadSlugOrId, @RequestBody Thread body) {
        Thread thread;
        try {
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Thread with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final String message = body.getMessage();
        final String title = body.getTitle();
        try {
            thread = threadService.update(thread.getSlug(), message, title);
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error updating thread - duplicate values exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("");
        }
        if (thread == null) {
            LOGGER.info("Error updating thread - thread doesn't exists!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(threadDataResponse(thread));
    }

    @RequestMapping(path = "/api/forum/{forum_slug}/threads", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getThreads(@PathVariable(value="forum_slug") String forumSlug,
                                     @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
                                     @RequestParam(name = "since", required = false) String sinceString,
                                     @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        final Forum forum = forumService.getForumBySlug(forumSlug); // наверное лучше убрать
        if (forum == null) {
            LOGGER.info("Error getting threads - forum with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final List<Thread> threads = threadService.getThreads(forumSlug, limit, sinceString, desc);
        return ResponseEntity.status(HttpStatus.OK).body(threadListResponse(threads).toJSONString());
    }

    static JSONObject threadDataResponse(Thread thread) {
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

    private JSONArray threadListResponse(List<Thread> threads) {
        final JSONArray jsonArray = new JSONArray();

        for(Thread t : threads) {
            if (t == null) {
                continue;
            }
            jsonArray.add(threadDataResponse(t));
        }
        return jsonArray;
    }
}
