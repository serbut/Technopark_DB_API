package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.services.ForumService;
import db.services.UserService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * Created by sergey on 26.02.17.
 */
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
        final int userId;
        try {
            userId = userService.getUserByNickname(userNickname).getId();
        }
        catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        Forum forum = forumService.create(slug, title, userId);
        if (forum == null) {
            try {
                forum = forumService.getForumBySlug(slug);
                userNickname = userService.getUserById(forum.getUserId()).getNickname();
            }
            catch (NullPointerException e) {
                LOGGER.info("There is no forum with such slug");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ForumDataResponse(forum, userNickname));
        }
        userNickname = userService.getUserByNickname(userNickname).getNickname();
        return ResponseEntity.status(HttpStatus.CREATED).body(ForumDataResponse(forum, userNickname));
    }

    @RequestMapping(path = "/api/forum/{slug}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getUser(@PathVariable(value="slug") String slug) {
        final Forum forum = forumService.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final String userNickname = userService.getUserById(forum.getUserId()).getNickname();
        return ResponseEntity.status(HttpStatus.OK).body(ForumDataResponse(forum, userNickname));
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

    private static JSONObject ForumDataResponse(Forum forum, String userNickname) {
        JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("slug", forum.getSlug());
        formDetailsJson.put("title", forum.getTitle());
        formDetailsJson.put("user", userNickname);
        return formDetailsJson;
    }
}
