package dev.fawkes.jura.streams.discord;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
    private final AtomicBoolean ready;

    public DiscordStreamsNotifier(JDA jda, AtomicBoolean ready) {
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
        this.ready = ready;
    }

    @Override
    public void onStreamStart(DiscordStreamer streamer) {
        if (this.ready.get()) {
            User user = this.jda.getUserById(streamer.getUserID());
            MessageEmbed embedMessage = getDiscordStreamStartedMessage(
                    user,
                    jda.getVoiceChannelById(streamer.getStreamChannelID()).getName(),
                    streamer.getCurrentViewers(),
                    streamer.getAllViewers(),
                    streamDuration(streamer.getStreamDuration())
            );

            Message message = new MessageBuilder()
                    .append(this.streamNotificationMention)
                    .append(" - ")
                    .append(user.getName())
                    .append(" is now streaming on Discord")
                    .build();

            Message result = this.streamNotificationChannel.sendMessage(message).embed(embedMessage).complete();
            streamer.setStreamStartMessageID(result.getIdLong());
        }
    }

    @Override
    public void onStreamUpdate(DiscordStreamer streamer) {
        if (this.ready.get()) {
            User user = this.jda.getUserById(streamer.getUserID());
            MessageEmbed embedMessage = getDiscordStreamStartedMessage(
                    user,
                    jda.getVoiceChannelById(streamer.getStreamChannelID()).getName(),
                    streamer.getCurrentViewers(),
                    streamer.getAllViewers(),
                    streamDuration(streamer.getStreamDuration()));
            this.streamNotificationChannel.editMessageById(streamer.getStreamStartMessageID(), embedMessage).queue();
        }
    }

    @Override
    public void onStreamEnd(DiscordStreamer streamer) {
        if (this.ready.get()) {
            String user = this.jda.getUserById(streamer.getUserID()).getName();
            String streamDuration = streamDuration(streamer.getStreamDuration());

            MessageEmbed embedMessage = getDiscordStreamEndedMessage(user, streamDuration, streamer.getAllViewers());
            this.streamNotificationChannel.sendMessage(embedMessage).complete();
        }
    }


    private MessageEmbed getDiscordStreamStartedMessage(User user, String channel, Set<Long> currentViewers, Set<Long> allViewers, String streamTime) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Discord Stream Started", null, "https://discordapp.com/assets/2c21aeda16de354ba5334551a883b481.png");
        embedBuilder.setTitle(user.getName() + " is streaming");
        embedBuilder.setDescription(":loud_sound: " + channel);
        embedBuilder.setThumbnail(user.getEffectiveAvatarUrl());
        embedBuilder.setColor(new Color(132, 244, 251));
        if (currentViewers != null && allViewers != null) {
            List<String> currentViewersNames = currentViewers.stream().map(userID -> this.jda.getUserById(userID).getName()).collect(Collectors.toList());
            List<String> allViewersNames = allViewers.stream().map(userID -> this.jda.getUserById(userID).getName()).collect(Collectors.toList());
            embedBuilder.addField("Current Viewers: (" + currentViewers.size() + ")", String.join(", ", currentViewersNames), false);
            embedBuilder.addField("All Viewers: (" + allViewers.size() + ")", String.join(", ", allViewersNames), false);
        }
        embedBuilder.addField("Stream duration", streamTime, false);
        return embedBuilder.build();
    }

    private MessageEmbed getDiscordStreamEndedMessage(String user, String streamTime, Set<Long> allViewers) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Discord Stream Ended", null, "https://discordapp.com/assets/2c21aeda16de354ba5334551a883b481.png");
        embedBuilder.setDescription(user + " has stopped streaming");
        embedBuilder.setColor(new Color(132, 244, 251));
        embedBuilder.addField("Stream duration", streamTime, false);
        List<String> currentViewersNames = allViewers.stream().map(userID -> this.jda.getUserById(userID).getName()).collect(Collectors.toList());
        embedBuilder.addField("Viewers were", String.join(", ", currentViewersNames), false);
        return embedBuilder.build();
    }

    private static String streamDuration(Long duration) {
        if (duration != null) {
            long days = TimeUnit.MILLISECONDS.toDays(duration);
            duration -= TimeUnit.DAYS.toMillis(days);
            long hours = TimeUnit.MILLISECONDS.toHours(duration);
            duration -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            duration -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

            String durationText = "";
            if (days > 0) {
                durationText = durationText + days + "d ";
            }
            if (hours > 0) {
                durationText = durationText + hours + "h ";
            }
            if (minutes > 0) {
                durationText = durationText + minutes + "m ";
            }
            if (seconds > 0) {
                durationText = durationText + seconds + "s";
            }
            return durationText;
        } else {
            return "Oh sh!t: Here there be dragons.";
        }


    }
}
