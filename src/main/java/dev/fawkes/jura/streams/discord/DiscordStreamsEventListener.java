package dev.fawkes.jura.streams.discord;

import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Listens for & reacts to discord "Go live" events;
 */
@Slf4j
public class DiscordStreamsEventListener extends ListenerAdapter {

    private final DiscordStreamers discordStreamers;

    public DiscordStreamsEventListener(DiscordStreamers streamers) {
        this.discordStreamers = streamers;
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Optional<DiscordStreamer> discordStreamer = this.discordStreamers.getStreamers().values().stream()
                .filter(ds -> ds.getStreamChannelID().equals(event.getChannelJoined().getIdLong()))
                .findFirst();
        addViewer(discordStreamer, event.getMember().getIdLong());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Optional<DiscordStreamer> discordStreamer = this.discordStreamers.getStreamers().values().stream()
                .filter(ds -> ds.getStreamChannelID().equals(event.getChannelLeft().getIdLong()))
                .findFirst();
        removeViewer(discordStreamer, event.getMember().getIdLong());
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        Optional<DiscordStreamer> discordStreamerJoined = this.discordStreamers.getStreamers().values().stream()
                .filter(ds -> ds.getStreamChannelID().equals(event.getChannelJoined().getIdLong()))
                .findFirst();
        Optional<DiscordStreamer> discordStreamerLeft = this.discordStreamers.getStreamers().values().stream()
                .filter(ds -> ds.getStreamChannelID().equals(event.getChannelLeft().getIdLong()))
                .findFirst();

        if (discordStreamerJoined.isPresent()) {
            addViewer(discordStreamerJoined, event.getMember().getIdLong());
        } else if (discordStreamerLeft.isPresent()) {
            removeViewer(discordStreamerLeft, event.getMember().getIdLong());
        }
    }

    private void removeViewer(Optional<DiscordStreamer> discordStreamer, Long viewerID) {
        if (discordStreamer.isPresent()) {
            discordStreamer.get().removeViewer(viewerID);
            this.discordStreamers.updateStreamer(discordStreamer.get());
        }
    }

    private void addViewer(Optional<DiscordStreamer> discordStreamer, Long viewerID) {
        if (discordStreamer.isPresent()) {
            discordStreamer.get().addViewer(viewerID);
            this.discordStreamers.updateStreamer(discordStreamer.get());
        }
    }

    @Override
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {
        log.info("Voice stream event - user:{}, streaming:{}, responseID:{}", event.getMember().getEffectiveName(), event.isStream(), event.getResponseNumber());

        if (event.isStream()) {
            this.discordStreamers.addStreamer(
                    event.getGuild().getIdLong(), event.getMember().getIdLong(), event.getVoiceState().getChannel().getIdLong(), System.currentTimeMillis(),
                    event.getVoiceState().getChannel().getMembers().stream().map(ISnowflake::getIdLong).collect(Collectors.toList()));
        } else {
            this.discordStreamers.removeStreamer(event.getMember().getIdLong());
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
            this.discordStreamers.removeStreamer(member.getIdLong());
        }
    }
}
