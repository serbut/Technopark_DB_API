package db.responses;

/**
 * Created by sergeybutorin on 09.05.17.
 */
public class StatusResponse {
    int user;
    int forum;
    int thread;
    int post;

    public StatusResponse(int user, int forum, int thread, int post) {
        this.user = user;
        this.forum = forum;
        this.thread = thread;
        this.post = post;
    }

    public int getUser() {
        return user;
    }

    public int getForum() {
        return forum;
    }

    public int getThread() {
        return thread;
    }

    public int getPost() {
        return post;
    }
}
