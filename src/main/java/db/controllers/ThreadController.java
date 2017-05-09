package db.controllers;

import db.models.Post;
import db.models.Thread;
import db.models.Vote;
import db.responses.PostSortResponse;
import db.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(path = "/api/thread")
class ThreadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadController.class.getName());

    private UserService userService;
    private ThreadService threadService;
    private PostService postService;
    private VoteService voteService;

    @Autowired
    ThreadController(UserService userService, ThreadService threadService, PostService postService, VoteService voteService) {
        this.userService = userService;
        this.threadService = threadService;
        this.postService = postService;
        this.voteService = voteService;
    }

    @RequestMapping(path = "/{thread_slug_or_id}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getSingleThread(@PathVariable(value = "thread_slug_or_id") String threadSlugOrId) {
        Thread thread;
        try {
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch (NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Thread with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.ok(thread);
    }

    @RequestMapping(path = "/{thread_slug_or_id}/details", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity updateThread(@PathVariable(value = "thread_slug_or_id") String threadSlugOrId, @RequestBody Thread body) {
        Thread thread;
        try {
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch (NumberFormatException e) {
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
        } catch (DuplicateKeyException e) {
            LOGGER.info("Error updating thread - duplicate values exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("");
        }
        if (thread == null) {
            LOGGER.info("Error updating thread - thread doesn't exists!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(path = "/{thread_slug_or_id}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPost(@PathVariable(value = "thread_slug_or_id") String threadSlugOrId, @RequestBody List<Post> body) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch (NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Error creating posts - thread with such slug/id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        for (Post post : body) {
            post.setThreadId(thread.getId());
            final int parentId = post.getParentId();
            if (parentId != 0) {
                final Post parentPost = postService.getPostById(parentId);
                if (parentPost == null || parentPost.getThreadId() != thread.getId()) {
                    LOGGER.info("Error creating post - parent is not in this thread!");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("");
                }
            }
            post.setForum(thread.getForum());
        }
        final List<Post> createdPosts = postService.create(body);
        if (createdPosts == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPosts);
    }

    @RequestMapping(path = "/{thread_slug_or_id}/posts", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getPosts(@PathVariable(value = "thread_slug_or_id") String threadSlugOrId,
                                   @RequestParam(name = "limit", required = false, defaultValue = "0") int limit,
                                   @RequestParam(name = "marker", required = false, defaultValue = "0") String marker,
                                   @RequestParam(name = "sort", required = false, defaultValue = "flat") String sort,
                                   @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch (NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Error getting posts - thread with such slug/id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        int markerInt = Integer.parseInt(marker);
        List<Post> posts = null;
        //noinspection SwitchStatementWithoutDefaultBranch
        switch (sort) {
            case "flat":
                posts = postService.getPostsFlat(thread.getSlug(), limit, markerInt, desc);
                if (!posts.isEmpty()) {
                    markerInt += posts.size();
                }
                break;
            case "tree":
                posts = postService.getPostsTree(thread.getSlug(), limit, markerInt, desc);
                if (!posts.isEmpty()) {
                    markerInt += posts.size();
                }
                break;
            case "parent_tree":
                final List<Integer> parentIds = postService.getParents(thread.getSlug(), limit, markerInt, desc);
                if (!parentIds.isEmpty()) {
                    markerInt += parentIds.size();
                }
                posts = postService.getPostsParentsTree(thread.getSlug(), desc, parentIds);
                break;
        }
        return ResponseEntity.ok(new PostSortResponse(posts, String.valueOf(markerInt)));
    }

    @RequestMapping(path = "/{thread}/vote", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity addVote(@PathVariable(value = "thread") String threadSlugOrId, @RequestBody Vote body) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch (NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Error creating vote - thread with such slug/id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        String author = body.getAuthor();
        try { // этот блок вынести
            author = userService.getUserByNickname(author).getNickname();//убрать это
        } catch (NullPointerException e) {
            LOGGER.info("Error creating vote - user not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final byte voice = body.getVoice();
        thread.setVotes(voteService.addVote(new Vote(author, thread.getId(), voice)));
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }
}
