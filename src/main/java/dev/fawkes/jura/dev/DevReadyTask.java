package dev.fawkes.jura.dev;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DevReadyTask {

    private JDA jda;
    private final String devChannelID;

    public DevReadyTask(JDA jda, @Value("${fawkes.discord.dev.channel}") String devChannelID) {
        this.devChannelID = devChannelID;
        this.jda = jda;
    }

    public void doTask() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("State notification", null, this.jda.getSelfUser().getAvatarUrl());
        embedBuilder.setTitle("I am awake, I am aware...");
        embedBuilder.setThumbnail(this.jda.getSelfUser().getAvatarUrl());
        embedBuilder.setColor(new Color(10, 200, 10));
        this.jda.getTextChannelById(this.devChannelID).sendMessage(embedBuilder.build()).queue();
    }
}
