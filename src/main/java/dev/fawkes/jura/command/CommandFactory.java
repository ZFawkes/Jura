package dev.fawkes.jura.command;

import dev.fawkes.jura.dev.PingCommand;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandFactory {

    public Command getCommand(GuildMessageReceivedEvent event) {

        if (!event.getAuthor().isBot()) {
            String[] messageParts = event.getMessage().getContentRaw().split(" ");
            if (messageParts.length > 1 && messageParts[0].equals("<@!" + event.getJDA().getSelfUser().getIdLong() + ">")) {
                switch (messageParts[1]) {
                    case "ping" : return new PingCommand();
                }
            }
        }
        return null;
    }
}
