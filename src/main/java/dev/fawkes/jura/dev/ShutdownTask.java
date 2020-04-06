package dev.fawkes.jura.dev;

import java.awt.*;

import dev.fawkes.jura.helpers.RoleHelper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

@Slf4j
public class ShutdownTask extends Thread {

    private JDA jda;
    private String devChannelID;

    public ShutdownTask(JDA jda, String devChannelID) {
        this.jda = jda;
        this.devChannelID = devChannelID;
    }

    public void run() {

        log.info("Attempting to ping discord before Shutting down");

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("State notification", null, this.jda.getSelfUser().getAvatarUrl());
        embedBuilder.setTitle("\"I'll be back\"");
        embedBuilder.setDescription("Shutting Down");
        embedBuilder.setThumbnail(this.jda.getSelfUser().getAvatarUrl());
        embedBuilder.setColor(new Color(10, 200, 10));

        RoleHelper.removeStreamingRoleFromAllMembers(this.jda, this.devChannelID);

        // Sync send message then shutdown.
        this.jda.getTextChannelById(this.devChannelID).sendMessage(embedBuilder.build()).complete();
        this.jda.shutdown();

        log.info("Done ping discord before Shutting down");
    }
}