package dev.fawkes.jura;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;

import dev.fawkes.jura.command.CommandFactory;
import dev.fawkes.jura.command.DiscordGuildCommandListener;
import dev.fawkes.jura.dev.DevStartupTask;
import dev.fawkes.jura.dev.ShutdownTask;
import dev.fawkes.jura.fun.GuildMessageListerner;
import dev.fawkes.jura.streams.StreamsStartupTask;
import dev.fawkes.jura.streams.discord.DiscordStreamers;
import dev.fawkes.jura.streams.discord.DiscordStreamsCoordinator;
import dev.fawkes.jura.streams.discord.DiscordStreamsEventListener;
import dev.fawkes.jura.streams.discord.DiscordStreamsNotifier;
import dev.fawkes.jura.streams.discord.DiscordStreamsRoles;
import dev.fawkes.jura.streams.twitch.TwitchBroadcastTask;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FawkesApplicationRunner implements ApplicationRunner {

    /**
     * Props (for now env props, should move most of these to config / make dynamic per guild).
     */
    private static final String DEV_CHANNEL_PROP_NAME = "fawkes.discord.dev.channel";
    private static final String DISCORD_BOT_TOKEN_PROPERTY_NAME = "fawkes.discord.token";

    private final AtomicReference<Boolean> ready = new AtomicReference<>(false);

    /**
     * Start up JDA and kick off any tasks.
     */
    public void run(ApplicationArguments args) throws Exception {

        String token = System.getenv().get(DISCORD_BOT_TOKEN_PROPERTY_NAME);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Missing discord token.");
        }
        // Startup JDA.
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("Wannabe's streaming"))
                .setEnableShutdownHook(false)
                .build();

        // Wait for bot login.
        jda.awaitReady();

        // TODO refactor tasks.
        // Generate event listeners
        DiscordStreamsNotifier discordStreamsNotifier = new DiscordStreamsNotifier(jda, this.ready);
        DiscordStreamsRoles discordStreamsRoles = new DiscordStreamsRoles(jda);
        DiscordStreamers discordStreamers = new DiscordStreamers(Arrays.asList(discordStreamsNotifier, discordStreamsRoles));
        DiscordStreamsEventListener discordStreamsEventListener = new DiscordStreamsEventListener(discordStreamers);
        DiscordGuildCommandListener discordGuildCommandListener = new DiscordGuildCommandListener(new CommandFactory());

        Runtime.getRuntime().addShutdownHook(new ShutdownTask(jda, System.getenv().get(DEV_CHANNEL_PROP_NAME)));

        /*
         * Disabling twitch until new API is sorted.
         */
        //Timer tasksTimer = new Timer();
        //tasksTimer.schedule(new TwitchBroadcastTask(jda), 0, 30*1000);

        StreamsStartupTask streamsStartupTask = new StreamsStartupTask(new DiscordStreamsCoordinator(jda, discordStreamers));
        DevStartupTask devStartupTask = new DevStartupTask(jda,  System.getenv().get(DEV_CHANNEL_PROP_NAME));
        LinkedList<StartupTask> startupTasks = new LinkedList<>();
        startupTasks.add(streamsStartupTask);
        startupTasks.add(devStartupTask);
        for (StartupTask startupTask : startupTasks) {
            startupTask.doTask();
        }

        this.ready.set(true);
        // Make sure everything is setup before responding to events
        GuildMessageListerner guildMessageListerner = new GuildMessageListerner();
        jda.addEventListener(guildMessageListerner, discordStreamsEventListener, discordGuildCommandListener);

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
