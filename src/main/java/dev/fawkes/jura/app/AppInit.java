package dev.fawkes.jura.app;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.fawkes.jura.streams.StreamsStartupTask;
import dev.fawkes.jura.streams.discord.DiscordStreamers;
import dev.fawkes.jura.streams.discord.DiscordStreamsCoordinator;
import dev.fawkes.jura.streams.discord.DiscordStreamsNotifier;
import dev.fawkes.jura.streams.discord.DiscordStreamsRoles;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class AppInit {

    private static final String DISCORD_BOT_TOKEN_PROPERTY_NAME = "fawkes.discord.token";

    public AppReady init() throws Exception {

        JDA jda = createJDA().awaitReady();
        AtomicBoolean isReady = new AtomicBoolean(false);

        DiscordStreamsNotifier discordStreamsNotifier = new DiscordStreamsNotifier(jda, isReady);
        DiscordStreamsRoles discordStreamsRoles = new DiscordStreamsRoles(jda);
        DiscordStreamers discordStreamers = new DiscordStreamers(Arrays.asList(discordStreamsNotifier, discordStreamsRoles));

        StreamsStartupTask streamsStartupTask = new StreamsStartupTask(new DiscordStreamsCoordinator(jda, discordStreamers));
        streamsStartupTask.doTask();

        return new AppReady(jda, discordStreamers, isReady);
    }

    private JDA createJDA() throws Exception  {
        String token = System.getenv().get(DISCORD_BOT_TOKEN_PROPERTY_NAME);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Missing discord token.");
        }
        // Startup JDA.
        return new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("Wannabe's streaming"))
                .setEnableShutdownHook(false)
                .build();
    }
}
