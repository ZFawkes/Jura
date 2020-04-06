package dev.fawkes.jura;

import java.util.Timer;

import dev.fawkes.jura.dev.ShutdownTask;
import dev.fawkes.jura.dev.StartupTask;
import dev.fawkes.jura.streams.discord.DiscordStreamListener;
import dev.fawkes.jura.streams.twitch.TwitchBroadcastTask;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.*;
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
    public static final String NOTIFICATION_CHANNEL_ID_PROPERTY_NAME = "fawkes.discord.notify.channel";
    public static final String NOTIFICATION_ROLE_MENTION_ID_PROPERTY_NAME  = "fawkes.discord.notify.role";
    public static final String CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME = "fawkes.discord.streaming.role";
    private static final String DISCORD_BOT_TOKEN_PROPERTY_NAME = "fawkes.discord.token";
    private static final String DEV_CHANNEL_PROP_NAME = "fawkes.discord.dev.channel";

    private DiscordStreamListener discordStreamListener = new DiscordStreamListener();

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
                .addEventListeners(this.discordStreamListener)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("Wannabe's streaming"))
                .setEnableShutdownHook(false)
                .build();
        jda.awaitReady();
        Runtime.getRuntime().addShutdownHook(new ShutdownTask(jda, System.getenv().get(DEV_CHANNEL_PROP_NAME)));

        // Run twitch task every 30s (first run now.)
        Timer tasksTimer = new Timer();
        tasksTimer.schedule(new TwitchBroadcastTask(jda), 1, 30*1000);

        // Ok ready.
        StartupTask.sendReadyNotification(jda, System.getenv().get(DEV_CHANNEL_PROP_NAME));

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
