package dev.fawkes.jura.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordGuildCommandListener extends ListenerAdapter {

    private CommandFactory commandFactory;
    private String botIDdesktop;
    private String botIDmobile;

    public DiscordGuildCommandListener(CommandFactory commandFactory, String botID) {
        this.commandFactory = commandFactory;
        this.botIDdesktop = "<@!" + botID + ">";
        this.botIDmobile = "<@" + botID + ">";
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        String message[] = event.getMessage().getContentRaw().split(" ");
        if (message[0] != null && (message[0].equals(this.botIDdesktop) || message[0].equals(this.botIDmobile))) {
            Command command = this.commandFactory.getCommand(event, message);
            if (command != null) {
                command.doCommand(event);
            }
        }
    }
}
