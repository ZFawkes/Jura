package dev.fawkes.jura.streams;

import dev.fawkes.jura.StartupTask;
import dev.fawkes.jura.streams.discord.DiscordStreamsCoordinator;


public class StreamsStartupTask implements StartupTask {

    private DiscordStreamsCoordinator discordStreamsCoordinator;

    public StreamsStartupTask(DiscordStreamsCoordinator discordStreamsCoordinator) {
        this.discordStreamsCoordinator = discordStreamsCoordinator;
    }

    @Override
    public void doTask() {
        this.discordStreamsCoordinator.setStreamers();
        // TODO have discord streamers coordinate roles.
        this.discordStreamsCoordinator.setRoles();
    }
}
