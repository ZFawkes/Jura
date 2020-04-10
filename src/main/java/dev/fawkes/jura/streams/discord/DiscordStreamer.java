package dev.fawkes.jura.streams.discord;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

// TODO can I use a factory to populate generate a stopped streamer, starting streamer etc?
/**
 * Discord Streamer object.
 */
@Getter
@Setter
public class DiscordStreamer {
    private Long guildID;
    private Long userID;
    private Long streamStartMessageID;
    private String streamChannelName;

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
