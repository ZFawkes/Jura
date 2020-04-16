package dev.fawkes.jura.streams.discord;

import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscordStreamsRoles implements DiscordStreamsListener {


    private final String streamerRoleID;

    private JDA jda;
    private String roleID;

    public DiscordStreamsRoles(JDA jda, @Value("${fawkes.discord.streaming.role}") String streamerRoleID) {
        this.streamerRoleID = streamerRoleID;
        this.jda = jda;
        if (streamerRoleID != null && !streamerRoleID.isEmpty()) {
            this.roleID = streamerRoleID;
        }
    }

    @Override
    public void onStreamStart(DiscordStreamer streamer) {
        if (this.roleID != null) {
            this.jda.getGuildById(streamer.getGuildID()).addRoleToMember(streamer.getUserID(), this.jda.getRoleById(this.roleID)).queue();
        }
    }

    @Override
    public void onStreamEnd(DiscordStreamer streamer) {
        if (this.roleID != null) {
            this.jda.getGuildById(streamer.getGuildID()).removeRoleFromMember(streamer.getUserID(), this.jda.getRoleById(this.roleID)).queue();
        }
    }
}
