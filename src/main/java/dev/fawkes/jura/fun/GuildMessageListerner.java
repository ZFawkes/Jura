package dev.fawkes.jura.fun;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageListerner extends ListenerAdapter {

    private static final String REACTION_ID = System.getenv().get("fawkes.discord.ping.reactionid");

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            if (!event.getMessage().getMentionedMembers().isEmpty() || !event.getMessage().getMentionedRoles().isEmpty()) {
                Emote emote = event.getGuild().getEmoteById(REACTION_ID);
                event.getMessage().addReaction(emote).queue();
            }
        }
    }
}
