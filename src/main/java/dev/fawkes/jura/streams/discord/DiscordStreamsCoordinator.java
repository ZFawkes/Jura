package dev.fawkes.jura.streams.discord;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscordStreamsCoordinator {

    private final JDA jda;
    private final DiscordStreamers discordStreamers;
    private final String streamerRoleID;
    private final List<DiscordStreamsSyncListener> syncListeners;

    public DiscordStreamsCoordinator(JDA jda, DiscordStreamers discordStreamers, @Value("${fawkes.discord.streaming.role}") String streamerRoleID, List<DiscordStreamsSyncListener> syncListeners) {
        this.jda = jda;
        this.discordStreamers = discordStreamers;
        this.streamerRoleID = streamerRoleID;
        this.syncListeners = syncListeners;
    }

    public void syncDiscordStreamers() {
        // Let any listeners know we are about to do a sync.
        for (DiscordStreamsSyncListener discordStreamsSyncListener : this.syncListeners) {
            discordStreamsSyncListener.onSyncStart();
        }

        Role streamingRole = this.jda.getRoleById(this.streamerRoleID);
        for (Guild guild : this.jda.getGuilds()) {
            for (Member member : guild.getMembers()) {

                DiscordStreamer discordStreamer = new DiscordStreamer();
                discordStreamer.setGuildID(guild.getIdLong());
                discordStreamer.setUserID(member.getIdLong());

                if (DiscordStreamsUtil.isStreaming(member.getVoiceState())) {
                    discordStreamer.setStreamChannelName(member.getVoiceState().getChannel().getName());

                    this.discordStreamers.addStreamer(discordStreamer);
                    guild.addRoleToMember(member, streamingRole);

                } else {
                    this.discordStreamers.removeStreamer(discordStreamer);
                    guild.removeRoleFromMember(member, streamingRole);
                }
            }
        }

        // Let any listeners know we have finished the sync.
        for (DiscordStreamsSyncListener discordStreamsSyncListener : this.syncListeners) {
            discordStreamsSyncListener.onSyncEnd();
        }

    }
}
