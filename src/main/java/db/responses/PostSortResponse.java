package db.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import db.models.Post;

import java.util.List;

/**
 * Created by sergeybutorin on 09.05.17.
 */
public class PostSortResponse {
    String marker;
    private final List<Post> posts;

    @JsonCreator
    public PostSortResponse(List<Post> posts, String marker) {
        this.marker = marker;
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public String getMarker() {
        return marker;
    }
}
