package db.responses;

import db.models.Forum;
import db.models.Post;
import db.models.Thread;
import db.models.User;

/**
 * Created by sergeybutorin on 09.05.17.
 */
public class PostDetailResponse {
    User author;
    Forum forum;
    Thread thread;
    Post post;

    public PostDetailResponse(User author, Forum forum, Thread thread, Post post) {
        this.author = author;
        this.forum = forum;
        this.thread = thread;
        this.post = post;
    }

    public User getAuthor() {
        return author;
    }

    public Forum getForum() {
        return forum;
    }

    public Thread getThread() {
        return thread;
    }

    public Post getPost() {
        return post;
    }

}
