package dev.fawkes.jura.streams.discord;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the collection of current streamers.
 */
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

    public void removeStreamer(Long userID) {
        DiscordStreamer streamer = this.streamers.remove(userID);
        if (streamer != null) {
            for (DiscordStreamsListener listener : this.discordStreamsListeners) {
                listener.onStreamEnd(streamer);
            }
        }
    }

    public HashMap<Long, DiscordStreamer> getStreamers() {
        return new HashMap<>(this.streamers);
    }

}
