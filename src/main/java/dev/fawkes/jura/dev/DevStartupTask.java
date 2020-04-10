package dev.fawkes.jura.dev;

import java.awt.Color;

import dev.fawkes.jura.StartupTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

public class DevStartupTask implements StartupTask {

    private JDA jda;
    private String devChannelID;

    public DevStartupTask(JDA jda, String devChannelID) {
        this.jda = jda;
        this.devChannelID = devChannelID;
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
