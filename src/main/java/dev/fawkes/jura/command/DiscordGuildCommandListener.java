package dev.fawkes.jura.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordGuildCommandListener extends ListenerAdapter {

    private CommandFactory commandFactory;

    public DiscordGuildCommandListener(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Command command = this.commandFactory.getCommand(event);
        if (command != null) {
            command.doCommand(event);
        }

    }

}
