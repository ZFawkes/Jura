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
                System.out.println("Reaction ID: " + REACTION_ID);
                Emote emote = event.getJDA().getEmoteById(REACTION_ID);
                System.out.println("Emote: " + emote);
                event.getMessage().addReaction(emote).queue();
            }
        }
    }
}
