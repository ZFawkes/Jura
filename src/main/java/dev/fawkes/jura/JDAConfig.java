package dev.fawkes.jura;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JDAConfig {

    private static final String DISCORD_BOT_TOKEN_PROPERTY_NAME = "fawkes.discord.token";

    @Bean
    public JDA getJDA() throws LoginException, InterruptedException {
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
        return jda;
    }
}
