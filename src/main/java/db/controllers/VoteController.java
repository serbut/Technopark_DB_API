package db.controllers;

import db.models.Thread;
import db.models.Vote;
import db.services.ThreadService;
import db.services.UserService;
import db.services.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * Created by sergeybutorin on 09.03.17.
 */
@RestController
public class VoteController {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/api/thread/{thread}/vote", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
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
}
