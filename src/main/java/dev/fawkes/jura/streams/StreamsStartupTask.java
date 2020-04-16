package dev.fawkes.jura.streams;

import dev.fawkes.jura.streams.discord.DiscordStreamsCoordinator;

import org.springframework.stereotype.Component;

@Component
public class StreamsStartupTask {

    private final DiscordStreamsCoordinator discordStreamsCoordinator;

    public StreamsStartupTask(DiscordStreamsCoordinator discordStreamsCoordinator) {
        this.discordStreamsCoordinator = discordStreamsCoordinator;
    }

    public void doTask() {
        this.discordStreamsCoordinator.setStreamers();
    }
}
