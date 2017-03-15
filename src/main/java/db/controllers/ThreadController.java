package db.controllers;

import db.models.Post;
import db.models.Thread;
import db.models.Vote;
import db.services.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeybutorin on 27.02.17.
 */
@SuppressWarnings("unchecked")
@RestController
@RequestMapping(path = "/api/thread")
class ThreadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadController.class.getName());

    @Autowired
    private ThreadService threadService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @RequestMapping(path = "/{thread_slug_or_id}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getSingleThread(@PathVariable(value="thread_slug_or_id") String threadSlugOrId) {
        Thread thread;
        try {
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Thread with such slug not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(threadDataResponse(thread));
    }

    @RequestMapping(path = "/{thread_slug_or_id}/details", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity updateThread(@PathVariable(value="thread_slug_or_id") String threadSlugOrId, @RequestBody Thread body) {
        Thread thread;
        try {
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
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
        }
        catch (DuplicateKeyException e) {
            LOGGER.info("Error updating thread - duplicate values exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("");
        }
        if (thread == null) {
            LOGGER.info("Error updating thread - thread doesn't exists!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        return ResponseEntity.status(HttpStatus.OK).body(threadDataResponse(thread));
    }

    @RequestMapping(path = "/{thread_slug_or_id}/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPost(@PathVariable(value="thread_slug_or_id") String threadSlugOrId, @RequestBody List<Post> body) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Error creating posts - thread with such slug/id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        List<Post> posts = new ArrayList<>();
        for(Post postBody: body) {
            String author = postBody.getAuthor();
            String created = postBody.getCreated();
            final String message = postBody.getMessage();
            final boolean isEdited = postBody.getIsEdited();
            final int parentId = postBody.getParentId();
            if (parentId != 0) {
                Post parentPost = postService.getPostById(parentId);
                if (parentPost == null || parentPost.getThreadId() != thread.getId()) {
                    LOGGER.info("Error creating post - parent is not in this thread!");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("");
                }
            }
            try { // этот блок вынести
                author = userService.getUserByNickname(author).getNickname();//убрать это
            } catch (NullPointerException e) {
                LOGGER.info("Error creating post - user not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
            }
            if (created == null) {
                LocalDateTime a = LocalDateTime.now();
                created = a.toString() + "+03:00";
            }
            final Post post = postService.create(new Post(author, created, message, isEdited, parentId, thread.getId()));
            post.setForum(thread.getForum());
            posts.add(post);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(PostController.postListResponse(posts).toJSONString());
    }

    @RequestMapping(path = "/{thread_slug_or_id}/posts", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getPosts(@PathVariable(value="thread_slug_or_id") String threadSlugOrId,
                                   @RequestParam(name = "limit", required = false, defaultValue = "0") int limit,
                                   @RequestParam(name = "marker", required = false, defaultValue = "0") String marker,
                                   @RequestParam(name = "sort", required = false, defaultValue = "flat") String sort,
                                   @RequestParam(name = "desc", required = false, defaultValue = "false") boolean desc) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            thread = threadService.getThreadBySlug(threadSlugOrId);
        }
        if (thread == null) {
            LOGGER.info("Error getting posts - thread with such slug/id not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        int markerInt = Integer.parseInt(marker);
        List<Post> posts = null;
        switch (sort) {
            case "flat":
                posts = postService.getPostsFlat(thread.getSlug(), limit, markerInt, desc);
                if(!posts.isEmpty()){
                    markerInt += posts.size();
                }
                break;
            case "tree":
                posts = postService.getPostsTree(thread.getSlug(), limit, markerInt, desc);
                if(!posts.isEmpty()){
                    markerInt += posts.size();
                }
                break;
            case "parent_tree":
                List<Integer> parentIds = postService.getParents(thread.getSlug(), limit, markerInt, desc);
                if(!parentIds.isEmpty()){
                    markerInt += parentIds.size();
                }
                posts = postService.getPostsParentsTree(thread.getSlug(), desc, parentIds);
                break;
        }
        return ResponseEntity.ok(PostController.sortResponse(PostController.postListResponse(posts), String.valueOf(markerInt)));
    }

    @RequestMapping(path = "/{thread}/vote", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity addVote(@PathVariable(value="thread") String threadSlugOrId, @RequestBody Vote body) {
        Thread thread;
        try { // этот блок вынести
            thread = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
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
        byte voice = body.getVoice();
        voteService.addVote(new Vote(author, thread.getId(), voice));
        thread.setVotes(voteService.getVotesForThread(thread.getId())); // от этого лучше бы избавиться
        return ResponseEntity.status(HttpStatus.OK).body(ThreadController.threadDataResponse(thread));
    }

    static JSONObject threadDataResponse(Thread thread) {
        final JSONObject formDetailsJson = new JSONObject();
        formDetailsJson.put("author", thread.getAuthor());
        formDetailsJson.put("created", thread.getCreated());
        formDetailsJson.put("forum", thread.getForum());
        formDetailsJson.put("id", thread.getId());
        formDetailsJson.put("message", thread.getMessage());
        formDetailsJson.put("slug", thread.getSlug());
        formDetailsJson.put("title", thread.getTitle());
        formDetailsJson.put("votes", thread.getVotes());
        return formDetailsJson;
    }

    static JSONArray threadListResponse(List<Thread> threads) {
        final JSONArray jsonArray = new JSONArray();

        for(Thread t : threads) {
            if (t == null) {
                continue;
            }
            jsonArray.add(threadDataResponse(t));
        }
        return jsonArray;
    }
}
