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

    public void addStreamer(Long guildID, Long userID, Long channelID, Long streamStartTime, List<Long> viewers) {
        DiscordStreamer streamer = new DiscordStreamer();
        streamer.setGuildID(guildID);
        streamer.setUserID(userID);
        streamer.setStreamChannelID(channelID);
        streamer.setStreamStartTime(streamStartTime);
        streamer.addViewers(viewers);
        if (this.streamers.put(streamer.getUserID(), streamer) == null) {
            for (DiscordStreamsListener listener : this.discordStreamsListeners) {
                listener.onStreamStart(streamer);
            }
        }
    }

    public void updateStreamer(DiscordStreamer streamer) {
        this.streamers.put(streamer.getUserID(), streamer);
        for (DiscordStreamsListener listener : this.discordStreamsListeners) {
            listener.onStreamUpdate(streamer);
        }
    }

    public void removeStreamer(Long userID) {
        DiscordStreamer streamerRemoved = this.streamers.remove(userID);
        if (streamerRemoved != null) {
            for (DiscordStreamsListener listener : this.discordStreamsListeners) {
                listener.onStreamEnd(streamerRemoved);
            }
        }
    }

    public HashMap<Long, DiscordStreamer> getStreamers() {
        return new HashMap<>(this.streamers);
    }

}
