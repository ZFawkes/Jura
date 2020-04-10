package dev.fawkes.jura.streams.discord;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class DiscordStreamsCoordinator {

    private static AtomicBoolean init = new AtomicBoolean(false);

    private final JDA jda;
    private DiscordStreamers discordStreamers;

    public DiscordStreamsCoordinator(JDA jda, DiscordStreamers discordStreamers) {
        this.jda = jda;
        this.discordStreamers = discordStreamers;
    }

    public void setStreamers() {
        Set<Long> trackedStreamers = this.discordStreamers.getStreamers().keySet();

        for (Guild guild : this.jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                if (member.getVoiceState() != null && member.getVoiceState().isStream()) {
                    DiscordStreamer discordStreamer = new DiscordStreamer();
                    discordStreamer.setGuildID(guild.getIdLong());
                    discordStreamer.setUserID(member.getIdLong());
                    discordStreamer.setStreamChannelName(member.getVoiceState().getChannel().getName());
                    // Track the streamer
                    this.discordStreamers.addStreamer(discordStreamer);
                    // Remove
                    trackedStreamers.remove(discordStreamer.getUserID());
                }
            }
        }

        // These streamers are no longer live, remove them.
        for (Long userID : trackedStreamers) {
            this.discordStreamers.removeStreamer(userID);
        }

        init.set(true);
    }

    public static boolean isInit() {
        return init.get();
    }

}
