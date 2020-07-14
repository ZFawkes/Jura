package dev.fawkes.jura.dst;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.fawkes.jura.command.Command;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DSTCommand implements Command {

    private static final String GIST_URL = System.getenv().get("fawkes.discord.dst.gist.world");

    @Override
    public void doCommand(GuildMessageReceivedEvent event) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {

            Map<String, Map<String, Map>> gist = objectMapper.readValue(new URL(GIST_URL), LinkedHashMap.class);
            String content = (String) gist.get("files").get("game_latest").get("content");
            Map<String, Object> data = objectMapper.readValue(content, LinkedHashMap.class);

            event.getChannel().sendMessage(getMessage(data)).queue();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static MessageEmbed getMessage(Map data) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor("Wannabe DST Bot", null, "https://vignette.wikia.nocookie.net/dont-starve-game/images/5/5a/WX-78_Portrait.png/revision/latest?cb=20121227104437");
        builder.setThumbnail("https://vignette.wikia.nocookie.net/dont-starve-game/images/f/f1/Science_Machine_Build.png/revision/latest?cb=20140419153455");
        builder.setFooter((String) data.get("servertime"), "https://icons.iconarchive.com/icons/kxmylo/simple/512/utilities-terminal-icon.png");

        List<LinkedHashMap> players = (List) data.get("players");
        String playerString = "";
        if (!players.isEmpty()) {
            for(int i = 0; i < players.size(); i++) {
                LinkedHashMap<String, Object> player = players.get(i);
                int playerNum = i + 1;
                playerString += "> `" + playerNum + "` ";
                playerString += player.get("name");
                if (player.get("admin").toString().equals("true")) {
                    playerString += "*";
                }
                playerString += " - ";
                playerString += player.get("prefab") + " ";
                playerString += "(" + player.get("age") + ")";
                if (i < players.size() - 1) {
                    playerString += "\n";
                }
            }
        } else {
            playerString = "> *Charlie*";
        }

        String playersFieldTitle = "Players Online (" + players.size() +"/" + data.get("maxplayers") + ")";
        builder.addField(playersFieldTitle, playerString, false);

        String world = "> Day " + data.get("day") + " [" + data.get("phase") + "] - " + String.format("%.2f", (Double) data.get("temperature"));
        String weatherData = (String) data.get("weather");
        String weather = weatherData.equals("rain") ? ":cloud_rain:" : weatherData.equals("snow") ? ":cloud_snow:" : ":white_sun_small_cloud:";
        world = world + " " + weather + "\n";

        world += " > " + data.get("season") + ", " + data.get("remainingdaysinseason") + " days(s) remaining\n";


        builder.addField("World", world, false);


        builder.setDescription(data.get("serverName") + " (v" + data.get("version") + ")");

        return builder.build();

    }
}
