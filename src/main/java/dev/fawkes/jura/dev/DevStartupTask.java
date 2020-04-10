package dev.fawkes.jura.dev;

import java.awt.Color;

import dev.fawkes.jura.helpers.RoleHelper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

//TODO refactor this into two tasks, one to send the up notification the other to do the roles.
public class DevStartupTask {

    public static void sendReadyNotification(JDA jda, String devChannelID) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("State notification", null, jda.getSelfUser().getAvatarUrl());
        embedBuilder.setTitle("I am awake, I am aware...");
        embedBuilder.setThumbnail(jda.getSelfUser().getAvatarUrl());
        embedBuilder.setColor(new Color(10, 200, 10));

        // TODO refactor this into a startup task that popualates the streamers & then does the role comparison + changes.
        RoleHelper.addStreamingRoleToStreamingMembers(jda, devChannelID);

        jda.getTextChannelById(devChannelID).sendMessage(embedBuilder.build()).queue();
    }
}
