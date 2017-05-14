package db.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sergeybutorin on 09.03.17.
 */
public class Vote {
    @JsonProperty("nickname")
    private String author;
    private byte voice;
    private int threadId;

    @SuppressWarnings("unused")
    private Vote() {
    }

    public Vote(String author, int threadId, byte value) {
        this.author = author;
        this.threadId = threadId;
        this.voice = value;
    }

    public String getAuthor() {
        return author;
    }

    public int getThreadId() {
        return threadId;
    }

    public byte getVoice() {
        return voice;
    }
}
