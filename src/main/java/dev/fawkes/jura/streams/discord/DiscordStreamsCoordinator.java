package dev.fawkes.jura.streams.discord;

import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;

public class DiscordStreamsCoordinator {

    private final JDA jda;
    private DiscordStreamers discordStreamers;

    public DiscordStreamsCoordinator(JDA jda, DiscordStreamers discordStreamers) {
        this.jda = jda;
        this.discordStreamers = discordStreamers;
    }

    public void setStreamers() {
        for (Guild guild : this.jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                if (member.getVoiceState() != null && member.getVoiceState().isStream()) {
                    List<Long> viewers = member.getVoiceState().getChannel().getMembers().stream().map(ISnowflake::getIdLong).collect(Collectors.toList());
                    this.discordStreamers.addStreamer(guild.getIdLong(), member.getIdLong(), member.getVoiceState().getChannel().getIdLong(), System.currentTimeMillis(), viewers);
                } else {
                    this.discordStreamers.removeStreamer(member.getIdLong());
                }
            }
        }
    }

    public void setRoles() {
        for (Guild guild : this.jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                List<String> memberRoles = member.getRoles().stream().map(r -> r.getId()).collect(Collectors.toList());
                if (memberRoles.contains(System.getenv().get("fawkes.discord.streaming.role")) && (member.getVoiceState() == null || !member.getVoiceState().isStream())) {
                    guild.removeRoleFromMember(member.getIdLong(), this.jda.getRoleById(System.getenv().get("fawkes.discord.streaming.role"))).complete();
                }
            }
        }
    }
}
