package dev.fawkes.jura.streams.discord;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Discord Streamer object.
 */
// TODO refactor to builder
@Getter
@Setter
public class DiscordStreamer {
    private Long guildID;
    private Long userID;
    private Long streamStartMessageID;
    private Long streamChannelID;
    private Long streamStartTime;
    private Set<Long> currentViewers = new HashSet<>();
    private Set<Long> allViewers = new HashSet<>();

    public void addViewer(Long viewerID) {
        this.currentViewers.add(viewerID);
        this.allViewers.add(viewerID);
    }

    public void addViewers(List<Long> viewerIDs) {
        this.currentViewers.addAll(viewerIDs);
        this.allViewers.addAll(viewerIDs);
    }

    public void removeViewer(Long viewerID) {
        this.currentViewers.remove(viewerID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordStreamer that = (DiscordStreamer) o;
        return Objects.equals(userID, that.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }
}
