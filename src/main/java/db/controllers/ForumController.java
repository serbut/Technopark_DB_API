package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.services.ForumService;
import db.services.UserService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * Created by sergey on 26.02.17.
 */
@SuppressWarnings("unchecked")
@RestController
class ForumController {
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
    public ResponseEntity createForum(@RequestBody GetForumRequest body) {
        final String slug = body.getSlug();
        final String title = body.getTitle();
        String userNickname = body.getUser();
        Forum forum = null;
        try {
            userNickname = userService.getUserByNickname(userNickname).getNickname();//убрать это
            forum = forumService.create(new Forum(slug, title, userNickname));
        }
        catch (NullPointerException e) { //убрать это
            LOGGER.info("Error creating forum - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error creating forum - forum already exists!");
            forum = forumService.getForumBySlug(slug);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ForumDataResponse(forum));
        }
        catch (DataAccessException e) {
            LOGGER.info("Error creating forum - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ForumDataResponse(forum));
    }

    @RequestMapping(path = "/api/forum/{slug}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getUser(@PathVariable(value="slug") String slug) {
        final Forum forum = forumService.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ForumDataResponse(forum));
    }


    private static final class GetForumRequest {
        @JsonProperty("slug")
        private String slug;
        @JsonProperty("title")
        private String title;
        @JsonProperty("user")
        private String user;

        @SuppressWarnings("unused")
        private GetForumRequest() {
        }

        @SuppressWarnings("unused")
        private GetForumRequest(String slug, String title, String user) {
            this.slug = slug;
            this.title = title;
            this.user = user;
        }

        public String getSlug() {
            return slug;
        }

        public String getTitle() {
            return title;
        }

        public String getUser() {
            return user;
        }
    }

    private static JSONObject ForumDataResponse(Forum forum) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("slug", forum.getSlug());
        formDetailsJson.put("title", forum.getTitle());
        formDetailsJson.put("user", forum.getUser());
        return formDetailsJson;
    }
}
