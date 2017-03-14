package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.services.ForumService;
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

/**
 * Created by sergey on 26.02.17.
 */
@SuppressWarnings("unchecked")
@RestController
class ForumController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForumController.class.getName());

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/api/forum", method = RequestMethod.GET)
    public void createTable() {
        forumService.clearTable();
        forumService.createTable();
    }

    @RequestMapping(path = "/api/forum/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
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

    @RequestMapping(path = "/api/forum/{slug}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getForum(@PathVariable(value="slug") String slug) {
        final Forum forum = forumService.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(forumDataResponse(forum));
    }

    @RequestMapping(path = "/api/forum/{slug}/users", method = RequestMethod.GET, produces = "application/json")
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
