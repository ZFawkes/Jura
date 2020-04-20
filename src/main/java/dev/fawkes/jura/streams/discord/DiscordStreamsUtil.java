package dev.fawkes.jura.streams.discord;


import net.dv8tion.jda.api.entities.GuildVoiceState;

public class DiscordStreamsUtil {

    public static boolean isStreaming(GuildVoiceState voiceState) {
        return voiceState != null && voiceState.isStream();
    }
}
