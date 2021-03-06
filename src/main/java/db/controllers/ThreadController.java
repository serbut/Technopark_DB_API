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

import static org.apache.el.lang.ELArithmetic.isNumber;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@RestController
@RequestMapping(path = "/api/thread")
class ThreadController {

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
        final Thread thread = threadService.getThreadBySlugOrId(threadSlugOrId);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.ok(thread);
    }

    @RequestMapping(path = "/{thread_slug_or_id}/details", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity updateThread(@PathVariable(value = "thread_slug_or_id") String threadSlugOrId, @RequestBody Thread body) {
        Thread thread = threadService.getThreadBySlugOrId(threadSlugOrId);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final String message = body.getMessage();
        final String title = body.getTitle();
        try {
            thread = threadService.update(thread.getId(), message, title);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("");
        }
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(path = "/{thread_slug_or_id}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPost(@PathVariable(value = "thread_slug_or_id") String threadSlugOrId, @RequestBody List<Post> body) {
        final Thread thread = threadService.getThreadBySlugOrId(threadSlugOrId);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        for (Post post : body) {
            post.setThreadId(thread.getId());
            final int parentId = post.getParentId();
            if (parentId != 0) {
                final Post parentPost = postService.getPostById(parentId);
                if (parentPost == null || parentPost.getThreadId() != thread.getId()) {
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
        final Thread thread = threadService.getThreadBySlugOrId(threadSlugOrId);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        int markerInt = Integer.parseInt(marker);
        List<Post> posts = null;
        //noinspection SwitchStatementWithoutDefaultBranch
        switch (sort) {
            case "flat":
                posts = postService.getPostsFlat(thread.getId(), limit, markerInt, desc);
                if (!posts.isEmpty()) {
                    markerInt += posts.size();
                }
                break;
            case "tree":
                posts = postService.getPostsTree(thread.getId(), limit, markerInt, desc);
                if (!posts.isEmpty()) {
                    markerInt += posts.size();
                }
                break;
            case "parent_tree":
                final List<Integer> parentIds = postService.getParents(thread.getId(), limit, markerInt, desc);
                if (!parentIds.isEmpty()) {
                    markerInt += parentIds.size();
                }
                posts = postService.getPostsParentsTree(thread.getId(), desc, parentIds);
                break;
        }
        return ResponseEntity.ok(new PostSortResponse(posts, String.valueOf(markerInt)));
    }

    @RequestMapping(path = "/{thread}/vote", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity addVote(@PathVariable(value = "thread") String threadSlugOrId, @RequestBody Vote body) {
        final Thread thread = threadService.getThreadBySlugOrId(threadSlugOrId);

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        String author = body.getAuthor();
        try { // этот блок вынести
            author = userService.getUserByNickname(author).getNickname();//убрать это
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        final byte voice = body.getVoice();
        thread.setVotes(voteService.addVote(new Vote(author, thread.getId(), voice)));
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }
}
