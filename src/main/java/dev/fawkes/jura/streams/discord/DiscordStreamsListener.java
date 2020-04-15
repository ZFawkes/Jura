package dev.fawkes.jura.streams.discord;

public interface DiscordStreamsListener {

    void onStreamStart(DiscordStreamer streamer);

    void onStreamEnd(DiscordStreamer streamer);

    void onStreamUpdate(DiscordStreamer streamer);

}
