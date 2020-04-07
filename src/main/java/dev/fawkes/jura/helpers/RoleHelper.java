package dev.fawkes.jura.helpers;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class RoleHelper {

    private static final String CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME = "fawkes.discord.streaming.role";

    /**
     * Add a role to a member as long as the user doesn't already has this role.
     * @param env The roleId of the role to add to the member.
     * @param member The member the role has to be added on.
     * @param guild The guild the member is in.
     */
    private static void addRole(String env, Member member, Guild guild) {
        String roleId = System.getenv().get(env);

        if (roleId != null && !roleId.isEmpty()) {
            Role roleToAdd = member.getGuild().getRoleById(roleId);
            List<Role> memberRoles = member.getRoles();
            boolean addRole = true;

            for (Role memberRole : memberRoles) {
                if (memberRole == roleToAdd) {
                    addRole = false;
                    break;
                }
            }

            if (addRole) {
                guild.addRoleToMember(member, roleToAdd).complete();
            }
        }


    }

    /**
     * Remove a role from the member if the member has the specified role.
     * @param env The roleId of the role to add to the member.
     * @param member The member to remove the role from.
     * @param guild The guild the member is in.
     */
    private static void removeRole(String env, Member member, Guild guild) {
        String roleId = System.getenv().get(env);

        if (roleId != null && !roleId.isEmpty()) {
            Role roleToRemove = member.getGuild().getRoleById(roleId);
            List<Role> memberRoles = member.getRoles();

            for (Role memberRole : memberRoles) {
                if (memberRole == roleToRemove) {
                    guild.removeRoleFromMember(member, roleToRemove).complete();
                    break;
                }
            }
        }
    }

    /**
     * Remove the streaming role from all users that have the role in the guild.
     * @param jda The bot.
     * @param channelId A channel of the current guild.
     */
    public static void removeStreamingRoleFromAllMembers(JDA jda, String channelId) {
        String streamingRoleId = System.getenv().get(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME);

        if (streamingRoleId != null && !streamingRoleId.isEmpty()) {
            Guild guild = jda.getTextChannelById(channelId).getGuild();
            List<Member> members = guild.getMembers();

            for (Member member : members) {
                RoleHelper.removeRole(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME, member, guild);
            }
        }
    }

    /**
     * Add the streaming role to all members that are streaming. Remove it when the member is not streaming.
     * @param jda The bot.
     * @param channelId A channel of the current guild.
     */
    public static void addStreamingRoleToStreamingMembers(JDA jda, String channelId) {
        String streamingRoleId = System.getenv().get(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME);

        if (streamingRoleId != null && !streamingRoleId.isEmpty()) {
            Guild guild = jda.getTextChannelById(channelId).getGuild();
            List<Member> members = guild.getMembers();

            for (Member member : members) {
                if (member.getVoiceState().isStream()) {
                    RoleHelper.addRole(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME, member, guild);
                } else {
                    RoleHelper.removeRole(CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME, member, guild);
                }
            }
        }
    }
}
