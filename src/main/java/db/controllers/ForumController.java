package db.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import db.models.Forum;
import db.models.User;
import db.services.ForumService;
import db.services.UserService;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * Created by sergey on 26.02.17.
 */
@RestController
public class ForumController {
    private final ForumService forumServ;

    public ForumController(ForumService forumServ) {
        this.forumServ = forumServ;
    }

    @RequestMapping(path = "/api/forum", method = RequestMethod.GET)
    public void createTable() {
        forumServ.clearTable();
        forumServ.createTable();
    }

    @RequestMapping(path = "/api/forum/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createForum(@RequestBody GetForumRequest body) {
        String slug = body.getSlug();
        String title = body.getTitle();
        String user_nickname = body.getUser();
        Forum forum = forumServ.create(slug, title, user_nickname);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ForumDataResponse(forum, user_nickname));
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

    private static JSONObject ForumDataResponse(Forum forum, String user_nickname) {
        JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("slug", forum.getSlug());
        formDetailsJson.put("title", forum.getTitle());
        formDetailsJson.put("user", user_nickname);
        return formDetailsJson;
    }
}
