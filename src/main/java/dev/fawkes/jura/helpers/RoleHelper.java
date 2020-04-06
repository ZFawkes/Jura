package dev.fawkes.jura.helpers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

import static dev.fawkes.jura.FawkesApplicationRunner.CURRENT_STREAMING_ROLE_ID_PROPERTY_NAME;

public class RoleHelper {
    /**
     * Add a role to a member as long as the user doesn't already has this role.
     * @param roleToAdd The role to add to the member.
     * @param member The member the role has to be added on.
     * @param guild The guild the member is in.
     */
    public static void addRole(Role roleToAdd, Member member, Guild guild) {
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

    /**
     * Remove a role from the member if the member has the specified role.
     * @param roleToRemove The role that has to be removed from the member.
     * @param member The member to remove the role from.
     * @param guild The guild the member is in.
     */
    public static void removeRole(Role roleToRemove, Member member, Guild guild) {
        List<Role> memberRoles = member.getRoles();

        for (Role memberRole : memberRoles) {
            if (memberRole == roleToRemove) {
                guild.removeRoleFromMember(member, roleToRemove).complete();
                break;
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
            Role streamingRole = guild.getRoleById(streamingRoleId);

            for (Member member : members) {
                RoleHelper.removeRole(streamingRole, member, guild);
            }
        }
    }
}
