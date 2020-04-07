package dev.fawkes.jura.streams.discord;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the collection of current streamers.
 */
public class DiscordStreamers {

    private ConcurrentHashMap<Long, DiscordStreamer> streamers = new ConcurrentHashMap<>();

    public void addStreamer(DiscordStreamer streamer) {
        this.streamers.put(streamer.getUserID(), streamer);
    }

    public void removeStreamer(Long userID) {
        this.streamers.remove(userID);
    }

    public boolean isStreaming(Long userID) {
        return this.streamers.containsKey(userID);
    }

}
