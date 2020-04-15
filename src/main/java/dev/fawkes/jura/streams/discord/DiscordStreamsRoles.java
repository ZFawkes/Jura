package dev.fawkes.jura.streams.discord;


import net.dv8tion.jda.api.JDA;

public class DiscordStreamsRoles implements DiscordStreamsListener {

    private static final String CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME = "fawkes.discord.streaming.role";

    private JDA jda;
    private String roleID;

    public DiscordStreamsRoles(JDA jda) {
        this.jda = jda;
        String roleID = System.getenv().get(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME);
        if (roleID != null && !roleID.isEmpty()) {
            this.roleID = roleID;
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

    @Override
    public void onStreamUpdate(DiscordStreamer streamer) {

    }


}
