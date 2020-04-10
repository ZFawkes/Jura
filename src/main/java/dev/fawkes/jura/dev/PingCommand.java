package dev.fawkes.jura.dev;

import dev.fawkes.jura.command.Command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PingCommand implements Command {

    @Override
    public void doCommand(GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage("pong").queue();
    }
}
