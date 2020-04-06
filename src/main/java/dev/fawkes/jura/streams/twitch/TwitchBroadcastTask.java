package dev.fawkes.jura.streams.twitch;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import dev.fawkes.jura.streams.twitch.api.TwitchBroadcast;
import dev.fawkes.jura.streams.twitch.api.TwitchBroadcasts;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static dev.fawkes.jura.FawkesApplicationRunner.NOTIFICATION_CHANNEL_ID_PROPERTY_NAME;
import static dev.fawkes.jura.FawkesApplicationRunner.NOTIFICATION_ROLE_MENTION_ID_PROPERTY_NAME;

@Slf4j
public class TwitchBroadcastTask extends TimerTask {

    private static MessageEmbed getTwitchStreamStartedMessage(String user, String iconURL, String title, String avatarUrl, String discordUser) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Twitch Stream Started", null, "https://static-cdn.jtvnw.net/jtv_user_pictures/27bfa19d-e9ab-4d31-bff5-eea89e47a3df-profile_image-300x300.png");
        embedBuilder.setTitle(title);
        embedBuilder.setDescription("[twitch.tv/" + user + "](https://www.twitch.tv/" + user  + ") (" + discordUser + ")");
        embedBuilder.setImage(iconURL.replace("{width}", "1280").replace("{height}", "720"));
        embedBuilder.setThumbnail(avatarUrl);
        embedBuilder.setColor(Color.MAGENTA);
        return embedBuilder.build();
    }

    private static MessageEmbed getTwitchStreamEndedMessage(String user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Twitch Stream Ended", null, "https://static-cdn.jtvnw.net/jtv_user_pictures/27bfa19d-e9ab-4d31-bff5-eea89e47a3df-profile_image-300x300.png");
        embedBuilder.setDescription(user + " has stopped streaming");
        embedBuilder.setColor(Color.MAGENTA);
        return embedBuilder.build();
    }

    private static final String TWITCH_CLIENT_ID_PROP_NAME = "fawkes.discord.twitch.clientid";
    private static final String TWITCH_USERS_LOGINS_PROP_NAME = "fawkes.discord.twitch.users";
    private static final String TWITCH_STREAMS_API = "https://api.twitch.tv/helix/streams";
    private static final String TWITCH_STREAMS_USERS_LOGIN_QUERY_PARAM = "user_login";
    private static final String TWITCH_CLIENT_ID_HEADER_NAME = "Client-ID";

    private final String twitchClientID = System.getenv().get(TWITCH_CLIENT_ID_PROP_NAME);
    private final Map<String, String> twitchUserDiscordIDs = new HashMap<>();

    private Map<String, TwitchBroadcast> twitchUserBroadcasts = new HashMap<>();
    private Boolean twitchInit = Boolean.TRUE;

    private JDA jda;

    private final TextChannel notificationChannel;
    private final String notificationMention;

    @Autowired
    private RestTemplate restTemplate = new RestTemplateBuilder().build();

    public TwitchBroadcastTask(JDA jda) {
        // Take the twitchLogin#discordIDs from the env and split them into a map for use later.
        List<String> twitchDiscordPairs = Arrays.asList(System.getenv().get(TWITCH_USERS_LOGINS_PROP_NAME).split(","));
        for (String twitchDiscordPair : twitchDiscordPairs) {
            String[] userPair = twitchDiscordPair.split("#");
            this.twitchUserDiscordIDs.put(userPair[0], userPair[1]);
        }

        // Populate our map with the users.
        for (String twitchUser : twitchUserDiscordIDs.keySet()) {
            this.twitchUserBroadcasts.put(twitchUser, null);
        }
        this.jda = jda;
        this.notificationChannel = this.jda.getTextChannelById(System.getenv().get(NOTIFICATION_CHANNEL_ID_PROPERTY_NAME));
        this.notificationMention = this.jda.getRoleById(System.getenv().get(NOTIFICATION_ROLE_MENTION_ID_PROPERTY_NAME)).getAsMention();
    }

    @Override
    public void run() {
        log.info("Twitch broadcast check running.");

        try {
            // Query twitch.
            UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUri(new URI(TWITCH_STREAMS_API));
            for (String twitchUser : twitchUserDiscordIDs.keySet()) {
                queryBuilder.queryParam(TWITCH_STREAMS_USERS_LOGIN_QUERY_PARAM, twitchUser);
            }
            String query = queryBuilder.toUriString();
            final HttpHeaders headers = new HttpHeaders();
            headers.set(TWITCH_CLIENT_ID_HEADER_NAME, this.twitchClientID);
            ResponseEntity<TwitchBroadcasts> response = this.restTemplate.exchange(query, HttpMethod.GET, new HttpEntity<>(null, headers), TwitchBroadcasts.class);

            // Handle response
            if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null) {
                log.info("Got result from API");

                List<TwitchBroadcast> twitchBroadcasts = response.getBody().getData();
                if (this.twitchInit) {
                    // on start up store what is going on
                    log.info("On start up found {} user streaming", twitchBroadcasts.size());
                    for (TwitchBroadcast twitchBroadcast : twitchBroadcasts) {
                        String user = twitchBroadcast.getUser_name();
                        twitchUserBroadcasts.put(user, twitchBroadcast);
                    }
                } else {
                    // lets map what data we have so we can determine where we are at.
                    Map<String, TwitchBroadcast> liveTwitchUsers = twitchBroadcasts.stream().collect(Collectors.toMap(TwitchBroadcast::getUser_name, twitchLiveBroadcast -> twitchLiveBroadcast));
                    this.twitchInit = Boolean.FALSE;
                    log.info("Found {} users streaming", twitchBroadcasts.size());

                    for (String user : this.twitchUserBroadcasts.keySet()) {
                        // User was not streaming
                        if (this.twitchUserBroadcasts.get(user) == null) {
                            if (liveTwitchUsers.containsKey(user)) {
                                log.info("{} has started streaming on twitch", user);
                                this.twitchUserBroadcasts.put(user, liveTwitchUsers.get(user));
                                User discordUser = this.jda.getUserById(this.twitchUserDiscordIDs.get(user));
                                this.notificationChannel
                                        .sendMessage(createTwitchMessage(discordUser.getName()))
                                        .embed(getTwitchStreamStartedMessage(
                                                user,
                                                liveTwitchUsers.get(user).getThumbnail_url(),
                                                liveTwitchUsers.get(user).getTitle(),
                                                discordUser.getAvatarUrl(),
                                                discordUser.getName()
                                        )).queue();
                            } else {
                                log.info("{} remains offline", user);
                            }
                        // User was streaming
                        } else {
                            // Is still streaming
                            if (liveTwitchUsers.containsKey(user)) {
                                log.info("{} is still streaming ", user);
                            // Has stopped streaming
                            } else {
                                log.info("{} has stopped streaming on twitch", user);
                                this.twitchUserBroadcasts.put(user, null);
                                User discordUser = this.jda.getUserById(this.twitchUserDiscordIDs.get(user));
                                this.notificationChannel.sendMessage(getTwitchStreamEndedMessage(discordUser.getName())).queue();
                            }
                        }
                    }
                }
            } else {
                log.error("Error accessing twitch API: code:'{}'", response.getStatusCodeValue());
            }
        } catch (Exception e) {
            log.error("Could not reach twitch API");
        }
    }

    private Message createTwitchMessage(String user) {
        String message = this.notificationMention + " - " + user + " is now streaming on Twitch";
        return new MessageBuilder(message).build();
    }
}
