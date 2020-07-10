package dev.fawkes.jura.command;

import dev.fawkes.jura.dev.PingCommand;
import dev.fawkes.jura.dst.DSTCommand;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandFactory {

    Command getCommand(GuildMessageReceivedEvent event, String[] message) {

        if (!event.getAuthor().isBot() && message.length > 1) {
            switch (message[1]) {
                case "ping":
                    return new PingCommand();
                case "dst":
                    return new DSTCommand();
            }
        }
        return null;
    }
}
