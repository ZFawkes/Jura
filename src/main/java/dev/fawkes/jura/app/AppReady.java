package dev.fawkes.jura.app;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.fawkes.jura.command.CommandFactory;
import dev.fawkes.jura.command.DiscordGuildCommandListener;
import dev.fawkes.jura.dev.DevStartupTask;
import dev.fawkes.jura.dev.ShutdownTask;
import dev.fawkes.jura.streams.discord.DiscordStreamers;
import dev.fawkes.jura.streams.discord.DiscordStreamsEventListener;
import dev.fawkes.jura.streams.twitch.TwitchBroadcastTask;

import net.dv8tion.jda.api.JDA;

public class AppReady {

    private final JDA jda;
    private final DiscordStreamers discordStreamers;
    private final AtomicBoolean isReady;

    private static final String DEV_CHANNEL_PROP_NAME = "fawkes.discord.dev.channel";

    AppReady(JDA jda, DiscordStreamers discordStreamers, AtomicBoolean isReady) {
        this.jda = jda;
        this.discordStreamers = discordStreamers;
        this.isReady = isReady;
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new ShutdownTask(jda, System.getenv().get(DEV_CHANNEL_PROP_NAME)));


        // Run twitch task every 30s (first run now.)
        Timer tasksTimer = new Timer();
        tasksTimer.schedule(new TwitchBroadcastTask(jda), 0, 30*1000);

        DiscordStreamsEventListener discordStreamsEventListener = new DiscordStreamsEventListener(discordStreamers);
        DiscordGuildCommandListener discordGuildCommandListener = new DiscordGuildCommandListener(new CommandFactory());
        isReady.set(true);
        this.jda.addEventListener(discordStreamsEventListener, discordGuildCommandListener);

        DevStartupTask devStartupTask = new DevStartupTask(jda,  System.getenv().get(DEV_CHANNEL_PROP_NAME));
        devStartupTask.doTask();
    }
}
