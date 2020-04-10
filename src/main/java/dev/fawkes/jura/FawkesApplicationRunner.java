package dev.fawkes.jura;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;

import dev.fawkes.jura.command.CommandFactory;
import dev.fawkes.jura.command.DiscordGuildCommandListener;
import dev.fawkes.jura.dev.DevStartupTask;
import dev.fawkes.jura.dev.ShutdownTask;
import dev.fawkes.jura.streams.StreamsStartupTask;
import dev.fawkes.jura.streams.discord.DiscordStreamers;
import dev.fawkes.jura.streams.discord.DiscordStreamsCoordinator;
import dev.fawkes.jura.streams.discord.DiscordStreamsEventListener;
import dev.fawkes.jura.streams.discord.DiscordStreamsNotifier;
import dev.fawkes.jura.streams.discord.DiscordStreamsRoles;
import dev.fawkes.jura.streams.twitch.TwitchBroadcastTask;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FawkesApplicationRunner implements ApplicationRunner {

    /**
     * Props (for now env props, should move most of these to config / make dynamic per guild).
     */
    private static final String DEV_CHANNEL_PROP_NAME = "fawkes.discord.dev.channel";

    @Autowired
    @Lazy
    private JDA jda;

    /**
     * Start up JDA and kick off any tasks.
     */
    public void run(ApplicationArguments args) throws Exception {

        // Wait for bot login.
        this.jda.awaitReady();

        // TODO refactor tasks.
        // Generate event listeners
        DiscordStreamsNotifier discordStreamsNotifier = new DiscordStreamsNotifier(this.jda);
        DiscordStreamsRoles discordStreamsRoles = new DiscordStreamsRoles(this.jda);
        DiscordStreamers discordStreamers = new DiscordStreamers(Arrays.asList(discordStreamsNotifier, discordStreamsRoles));
        DiscordStreamsEventListener discordStreamsEventListener = new DiscordStreamsEventListener(discordStreamers);
        DiscordGuildCommandListener discordGuildCommandListener = new DiscordGuildCommandListener(new CommandFactory());

        jda.addEventListener(discordStreamsEventListener, discordGuildCommandListener);

        Runtime.getRuntime().addShutdownHook(new ShutdownTask(this.jda, System.getenv().get(DEV_CHANNEL_PROP_NAME)));

        // Run twitch task every 30s (first run now.)
        Timer tasksTimer = new Timer();
        tasksTimer.schedule(new TwitchBroadcastTask(this.jda), 0, 30*1000);

        StreamsStartupTask streamsStartupTask = new StreamsStartupTask(new DiscordStreamsCoordinator(this.jda, discordStreamers));
        DevStartupTask devStartupTask = new DevStartupTask(this.jda,  System.getenv().get(DEV_CHANNEL_PROP_NAME));
        LinkedList<StartupTask> startupTasks = new LinkedList<>();
        startupTasks.add(streamsStartupTask);
        startupTasks.add(devStartupTask);
        for (StartupTask startupTask : startupTasks) {
            startupTask.doTask();
        }

        // Keep app alive.
        new Thread("Keep Alive") {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }
        }.start();
    }
}
