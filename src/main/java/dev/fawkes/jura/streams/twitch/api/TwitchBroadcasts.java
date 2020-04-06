package dev.fawkes.jura.streams.twitch.api;

import java.util.List;

/**
 * Model of twitch API Broadscan object.
 * See <a href="https://dev.twitch.tv/docs/api/reference#get-streams">Twitch Get Streams</a>
 */
public class TwitchBroadcasts {

    private List<TwitchBroadcast> data;
    private TwitchPagination pagination;

    public List<TwitchBroadcast> getData() {
        return data;
    }

    public void setData(List<TwitchBroadcast> data) {
        this.data = data;
    }

    public TwitchPagination getPagination() {
        return pagination;
    }

    public void setPagination(TwitchPagination pagination) {
        this.pagination = pagination;
    }
}
