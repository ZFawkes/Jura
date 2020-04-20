package dev.fawkes.jura.streams.discord;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * Holds the collection of current streamers.
 */
@Repository
public class DiscordStreamers {

    private final List<DiscordStreamsListener> discordStreamsListeners;

    private ConcurrentHashMap<Long, DiscordStreamer> streamers = new ConcurrentHashMap<>();

    public DiscordStreamers(List<DiscordStreamsListener> discordStreamsListeners) {
        this.discordStreamsListeners = discordStreamsListeners;
    }

    public void addStreamer(DiscordStreamer streamer) {
        if (this.streamers.put(streamer.getUserID(), streamer) == null) {
            for (DiscordStreamsListener listener : this.discordStreamsListeners) {
                listener.onStreamStart(streamer);
            }
        }
    }

    public void removeStreamer(DiscordStreamer streamer) {
        DiscordStreamer streamerRemoved = this.streamers.remove(streamer.getUserID());
        if (streamerRemoved != null) {
            for (DiscordStreamsListener listener : this.discordStreamsListeners) {
                listener.onStreamEnd(streamerRemoved);
            }
        }
    }

    public void removeStreamer(Long userID) {
        DiscordStreamer discordStreamer = new DiscordStreamer();
        discordStreamer.setUserID(userID);
        removeStreamer(discordStreamer);
    }
}
