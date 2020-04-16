package dev.fawkes.jura.streams.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Component;

@Component
public class DiscordStreamsCoordinator {

    private final JDA jda;
    private final DiscordStreamers discordStreamers;

    public DiscordStreamsCoordinator(JDA jda, DiscordStreamers discordStreamers) {
        this.jda = jda;
        this.discordStreamers = discordStreamers;
    }

    public void setStreamers() {
        for (Guild guild : this.jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                DiscordStreamer discordStreamer = new DiscordStreamer();
                discordStreamer.setGuildID(guild.getIdLong());
                discordStreamer.setUserID(member.getIdLong());
                if (member.getVoiceState() != null && member.getVoiceState().isStream()) {
                    discordStreamer.setStreamChannelName(member.getVoiceState().getChannel().getName());
                    this.discordStreamers.addStreamer(discordStreamer);
                } else {
                    this.discordStreamers.removeStreamer(discordStreamer, true);
                }
            }
        }
    }
}
