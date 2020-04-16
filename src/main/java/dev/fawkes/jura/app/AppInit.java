package dev.fawkes.jura.app;

import java.util.Timer;

import dev.fawkes.jura.command.DiscordGuildCommandListener;
import dev.fawkes.jura.dev.ShutdownTask;
import dev.fawkes.jura.streams.StreamsStartupTask;
import dev.fawkes.jura.streams.discord.DiscordStreamsEventListener;
import dev.fawkes.jura.streams.twitch.TwitchBroadcastTask;

import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

@Component
public class AppInit {

    private final AppReadyImpl appReadyImpl;
    private final StreamsStartupTask streamsStartupTask;
    private final ShutdownTask shutdownTask;
    private final JDA jda;
    private final TwitchBroadcastTask twitchBroadcastTask;
    private final DiscordStreamsEventListener discordStreamsEventListener;
    private final DiscordGuildCommandListener discordGuildCommandListener;

    public AppInit(AppReadyImpl appReadyImpl, StreamsStartupTask streamsStartupTask, ShutdownTask shutdownTask, JDA jda,
                   DiscordStreamsEventListener discordStreamsEventListener, DiscordGuildCommandListener discordGuildCommandListener, TwitchBroadcastTask twitchBroadcastTask) {
        this.appReadyImpl = appReadyImpl;
        this.streamsStartupTask = streamsStartupTask;
        this.jda = jda;
        this.shutdownTask = shutdownTask;
        this.twitchBroadcastTask = twitchBroadcastTask;
        this.discordGuildCommandListener = discordGuildCommandListener;
        this.discordStreamsEventListener = discordStreamsEventListener;
    }

    public AppReady init() throws Exception {
        this.streamsStartupTask.doTask();
        Runtime.getRuntime().addShutdownHook(this.shutdownTask);

        Timer tasksTimer = new Timer();
        tasksTimer.schedule(this.twitchBroadcastTask, 0, 30*1000);

        this.jda.addEventListener(this.discordGuildCommandListener, this.discordStreamsEventListener);

        return this.appReadyImpl;
    }
}
