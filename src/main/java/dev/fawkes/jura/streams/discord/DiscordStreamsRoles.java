package dev.fawkes.jura.streams.discord;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;

public class DiscordStreamsRoles implements DiscordStreamsListener {

    private static final String CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME = "fawkes.discord.streaming.role";

    private JDA jda;
    private Role streamingRole;

    public DiscordStreamsRoles(JDA jda) {
        this.jda = jda;
        String roleID = System.getenv().get(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME);
        if (roleID != null && !roleID.isEmpty()) {
            this.streamingRole = this.jda.getRoleById(roleID);
        }
    }

    @Override
    public void onStreamStart(DiscordStreamer streamer) {
        if (this.streamingRole != null) {
            this.jda.getGuildById(streamer.getGuildID()).addRoleToMember(streamer.getUserID(), this.streamingRole);
        }
    }

    @Override
    public void onStreamEnd(DiscordStreamer streamer) {
        if (this.streamingRole != null) {
            this.jda.getGuildById(streamer.getGuildID()).removeRoleFromMember(streamer.getUserID(), this.streamingRole);
        }
    }
}
