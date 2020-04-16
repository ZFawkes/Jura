package dev.fawkes.jura.jda;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JDAConfig {

    @Bean
    public JDA getJDA(@Value("${fawkes.discord.token}") String discordBotToken) throws LoginException, InterruptedException {
        if (discordBotToken == null || discordBotToken.isEmpty()) {
            throw new IllegalArgumentException("Missing discord token.");
        }
        // Startup JDA.
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(discordBotToken)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("Wannabe's streaming"))
                .setEnableShutdownHook(false)
                .build();
        jda.awaitReady();
        return jda;
    }
}
