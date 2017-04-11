package db.controllers;

import db.models.Forum;
import db.models.Thread;
import db.services.ForumService;
import db.services.ThreadService;
import db.services.UserService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by sergey on 26.02.17.
 */
@SuppressWarnings("unchecked")
@RestController
@RequestMapping(path = "/api/forum")
class ForumController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumController.class.getName());

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @Autowired
    private ThreadService threadService;

    @RequestMapping(path = "/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createForum(@RequestBody Forum body) {
        final String slug = body.getSlug();
        final String title = body.getTitle();
        String userNickname = body.getUser();
        Forum forum = null;
        try {
            userNickname = userService.getUserByNickname(userNickname).getNickname();//убрать это
            forum = forumService.create(new Forum(slug, title, userNickname));
        } catch (NullPointerException e) { //убрать это
            LOGGER.info("Error creating forum - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        } catch (DuplicateKeyException e) {
            LOGGER.info("Error creating forum - forum already exists!");
            forum = forumService.getForumBySlug(slug);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumDataResponse(forum));
        } catch (DataAccessException e) {
            LOGGER.info("Error creating forum - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(forumDataResponse(forum));
    }

    @RequestMapping(path = "/{slug}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getForum(@PathVariable(value="slug") String slug) {
        final Forum forum = forumService.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(forumDataResponse(forum));
    }

    @RequestMapping(path = "/{slug}/users", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getUserList(@PathVariable(value="slug") String slug,
                                      @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
                                      @RequestParam(name = "since", required = false) String since,
                                      @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        final Forum forum = forumService.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(UserController.userListResponse(userService.getUsersForum(slug, limit, since, desc)));
    }

    @RequestMapping(path = "/{forum_slug}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createThread(@PathVariable(value="forum_slug") String forumSlug, @RequestBody Thread body) {
        String author = body.getAuthor();
        final String created = body.getCreated();
        final String message = body.getMessage();
        final String slug = body.getSlug();
        final String title = body.getTitle();
        Thread thread = null;
        try {
            author = userService.getUserByNickname(author).getNickname();//убрать это
            forumSlug = forumService.getForumBySlug(forumSlug).getSlug();//и это!
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ThreadController.threadDataResponse(thread));
        }
        catch (DataAccessException e) {
            LOGGER.info("Error creating thread - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ThreadController.threadDataResponse(thread));
    }

    @RequestMapping(path = "/{forum_slug}/threads", method = RequestMethod.GET, produces = "application/json")
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
        return ResponseEntity.status(HttpStatus.OK).body(ThreadController.threadListResponse(threads).toJSONString());
    }

    static JSONObject forumDataResponse(Forum forum) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("slug", forum.getSlug());
        formDetailsJson.put("title", forum.getTitle());
        formDetailsJson.put("user", forum.getUser());
        formDetailsJson.put("posts", forum.getPosts());
        formDetailsJson.put("threads", forum.getThreads());
        return formDetailsJson;
    }
}
