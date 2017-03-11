package db.controllers;

import db.models.Thread;
import db.models.Vote;
import db.services.ThreadService;
import db.services.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by sergeybutorin on 09.03.17.
 */
@RestController
public class VoteController {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private VoteService voteService;

    @RequestMapping(path = "/api/thread/{thread}/vote", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity addVote(@PathVariable(value="thread") String threadSlugOrId, @RequestBody Vote body) {
        Thread t;
        try {
            t = threadService.getThreadById(Integer.parseInt(threadSlugOrId));
        } catch(NumberFormatException e) {
            t = threadService.getThreadBySlug(threadSlugOrId);
        }
        String author = body.getAuthor();
        byte voice = body.getVoice();
        voteService.addVote(new Vote(author, t.getId(), voice));
        t.setVotes(voteService.getVotesForThread(t.getId())); // от этого лучше бы избавиться
        return ResponseEntity.status(HttpStatus.OK).body(ThreadController.ThreadDataResponse(t));
    }
}
