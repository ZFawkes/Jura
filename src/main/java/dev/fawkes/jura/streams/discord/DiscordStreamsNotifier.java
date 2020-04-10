package dev.fawkes.jura.streams.discord;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import static dev.fawkes.jura.streams.Streams.NOTIFICATION_CHANNEL_ID_PROPERTY_NAME;
import static dev.fawkes.jura.streams.Streams.NOTIFICATION_ROLE_MENTION_ID_PROPERTY_NAME;

public class DiscordStreamsNotifier implements DiscordStreamsListener {

    private final TextChannel streamNotificationChannel;
    private final String streamNotificationMention;
    private final JDA jda;

    public DiscordStreamsNotifier(JDA jda) {
        this.jda = jda;
        Role streamMentionRole = jda.getRoleById(System.getenv().get(NOTIFICATION_ROLE_MENTION_ID_PROPERTY_NAME));
        if (streamMentionRole == null) {
            throw new IllegalStateException("Could not get stream mention.");
        }
        this.streamNotificationMention = streamMentionRole.getAsMention();
        this.streamNotificationChannel = jda.getTextChannelById(System.getenv().get(NOTIFICATION_CHANNEL_ID_PROPERTY_NAME));
        if (this.streamNotificationChannel == null) {
            throw new IllegalStateException("Could not get stream notification channel.");
        }
    }

    @Override
    public void onStreamStart(DiscordStreamer streamer) {
        User user = this.jda.getUserById(streamer.getUserID());
        MessageEmbed embedMessage = getDiscordStreamStartedMessage(user, ":loud_sound: " + streamer.getStreamChannelName());

        Message message = new MessageBuilder()
                .append(this.streamNotificationMention)
                .append(" - ")
                .append(user.getName())
                .append(" is now streaming on Discord")
                .build();

        Message result = this.streamNotificationChannel.sendMessage(message).embed(embedMessage).complete();
        streamer.setStreamStartMessageID(result.getIdLong());
    }

    @Override
    public void onStreamEnd(DiscordStreamer streamer) {
        MessageEmbed embedMessage = getDiscordStreamEndedMessage(this.jda.getUserById(streamer.getUserID()).getName());
        this.streamNotificationChannel.sendMessage(embedMessage).complete();
    }


    private static MessageEmbed getDiscordStreamStartedMessage(User user, String channel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Discord Stream Started", null, "https://discordapp.com/assets/2c21aeda16de354ba5334551a883b481.png");
        embedBuilder.setTitle(user.getName() + " is streaming");
        embedBuilder.setDescription(channel);
        embedBuilder.setThumbnail(user.getEffectiveAvatarUrl());
        embedBuilder.setColor(new Color(132, 244, 251));
        return embedBuilder.build();
    }

    private static MessageEmbed getDiscordStreamEndedMessage(String user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Discord Stream Ended", null, "https://discordapp.com/assets/2c21aeda16de354ba5334551a883b481.png");
        embedBuilder.setDescription(user + " has stopped streaming");
        embedBuilder.setColor(new Color(132, 244, 251));
        return embedBuilder.build();
    }
}
