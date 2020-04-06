package dev.fawkes.jura.streams.discord;

import javax.annotation.Nonnull;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import dev.fawkes.jura.helpers.RoleHelper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static dev.fawkes.jura.FawkesApplicationRunner.*;

/**
 * Listens for & reacts to discord "Go live" events;
 */
@Slf4j
public class DiscordStreamListener extends ListenerAdapter {

    /**
     * Track discord streamers.
     */
    private final Set<String> streamers = new HashSet<>();

    private final String streamNotificationChannelID;
    private final String streamNotificationMentionID;
    private final String streamingRoleID;

    private Role streamingRole = null;

    public DiscordStreamListener() {
        this.streamNotificationChannelID = System.getenv().get(NOTIFICATION_CHANNEL_ID_PROPERTY_NAME);
        this.streamNotificationMentionID = System.getenv().get(NOTIFICATION_ROLE_MENTION_ID_PROPERTY_NAME);

        String role = System.getenv().get(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME);
        if (role != null && !role.isEmpty()) {
            this.streamingRoleID = System.getenv().get(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME);
        } else {
            this.streamingRoleID = null;
        }
    }

    @Override
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {
        log.info("Voice stream event - user:{}, streaming:{}, responseID:{}", event.getMember().getEffectiveName(), event.isStream(), event.getResponseNumber());

        Guild guild = event.getGuild();
        if (this.streamingRoleID != null) {
            this.streamingRole = guild.getRoleById(this.streamingRoleID);
        }

        TextChannel channel = guild.getTextChannelById(this.streamNotificationChannelID);
        if (channel != null) {
            if (event.isStream()) {
                MessageEmbed embedMessage = getDiscordStreamStartedMessage(event.getMember().getUser(), ":loud_sound: " + event.getVoiceState().getChannel().getName());

                Message message = new MessageBuilder()
                        .append( guild.getRoleById(this.streamNotificationMentionID).getAsMention())
                        .append(" - ")
                        .append(event.getMember().getEffectiveName())
                        .append(" is now streaming on Discord")
                        .build();

                channel.sendMessage(message).embed(embedMessage).complete();

                if (this.streamingRoleID != null) {
                    RoleHelper.addRole(this.streamingRole, event.getMember(), event.getGuild());
                }

                this.streamers.add(event.getMember().getId());
            } else if (this.streamers.contains(event.getMember().getId())) {
                MessageEmbed embedMessage = getDiscordStreamEndedMessage(event.getMember().getEffectiveName());
                channel.sendMessage(embedMessage).complete();

                if (this.streamingRoleID != null) {
                    RoleHelper.addRole(this.streamingRole, event.getMember(), event.getGuild());
                }

                this.streamers.remove(event.getMember().getId());
            }
        }
    }

    /**
     * When a user changes channel (which stops live streams) a @link{onGuildVoiceStream()} event is not fired.
     * So we track the streamers separately and use this event to mark the end of a stream.
      */
    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        Member member = event.getEntity();
        log.info("Voice update event - user:{}, streaming:{}, responseID:{}", member.getEffectiveName(), member.getVoiceState().isStream(), event.getResponseNumber());

        if (member.getVoiceState().isStream()) {
            MessageEmbed embedMessage = getDiscordStreamEndedMessage(member.getEffectiveName());
            event.getJDA().getTextChannelById(streamNotificationChannelID).sendMessage(embedMessage).complete();
            if (this.streamingRoleID != null) {
                RoleHelper.removeRole(this.streamingRole, member, member.getGuild());
            }
            this.streamers.remove(member.getId());
        }
    }

    /**
     * When a user leaves a voice chat we check if the user has the streamer role.
     * If the user has the role then remove the role.
     */
    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (this.streamingRoleID != null) {
            RoleHelper.addRole(this.streamingRole, event.getMember(), event.getGuild());
        }
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
