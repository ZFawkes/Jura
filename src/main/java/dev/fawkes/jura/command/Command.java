package dev.fawkes.jura.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface Command {

    void doCommand(GuildMessageReceivedEvent event);
}
