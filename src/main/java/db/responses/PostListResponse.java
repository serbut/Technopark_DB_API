package db.responses;

import db.models.Post;

import java.util.List;

/**
 * Created by sergeybutorin on 09.05.17.
 */
public class PostListResponse {
    private final List<Post> posts;

    public PostListResponse(List<Post> posts) {
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
