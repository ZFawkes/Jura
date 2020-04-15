package dev.fawkes.jura.streams.discord;

import java.util.TimerTask;

public class DiscordStreamersUpdateTask extends TimerTask {

    private DiscordStreamers discordStreamers;

    public DiscordStreamersUpdateTask(DiscordStreamers discordStreamers) {
        this.discordStreamers = discordStreamers;
    }

    @Override
    public void run() {
        for (DiscordStreamer discordStreamer : this.discordStreamers.getStreamers().values()) {
            this.discordStreamers.updateStreamer(discordStreamer);
        }
    }
}
