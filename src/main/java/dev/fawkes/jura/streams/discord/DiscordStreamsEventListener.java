package dev.fawkes.jura.streams.discord;

import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
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
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {
        log.info("Voice stream event - user:{}, streaming:{}, responseID:{}", event.getMember().getEffectiveName(), event.isStream(), event.getResponseNumber());

        if (event.isStream()) {
            DiscordStreamer discordStreamer = new DiscordStreamer();
            discordStreamer.setGuildID(event.getGuild().getIdLong());
            discordStreamer.setUserID(event.getMember().getIdLong());
            discordStreamer.setStreamChannelName(event.getVoiceState().getChannel().getName());
            discordStreamer.setStreamStartTime(System.currentTimeMillis());
            this.discordStreamers.addStreamer(discordStreamer);
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
