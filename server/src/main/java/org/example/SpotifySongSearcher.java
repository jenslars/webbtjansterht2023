package org.example;

import com.google.gson.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SpotifySongSearcher {

    private final String accessToken;
    private YouTubeVideoInfoExtractor youTubeVideoInfoExtractor;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final Gson gson;
    public SpotifySongSearcher(String accessToken, Gson gson) {
        this.youTubeVideoInfoExtractor = new YouTubeVideoInfoExtractor();
        this.accessToken = accessToken;
        this.gson = gson;
    }

    public List<TrackInfo> searchSongsOnSpotify(List<TrackInfo> tracks,String url) {
        System.out.println("searchSongsOnSpotify called");
        List<TrackInfo> foundTracks = new ArrayList<>();

        if (accessToken == null) {
            System.out.println("Access token is null.");
            return foundTracks;
        }

        for (TrackInfo trackInfo : tracks) {
            String searchUrl = buildSpotifySearchUrl(trackInfo);
            try {
                foundTracks.addAll(performSpotifySearch(searchUrl));
            } catch (Exception e) {
                System.out.println("Error searching on Spotify: " + e.getMessage());
            }
        }

        return foundTracks;
    }

    /**
     * Metod för att hämta lista av hittade låtar hos Spotify
     * Tar lista av strängar(titlar) som input och kan återanvändas av andra metoder
     */
    public List<TrackInfo> searchSongsOnSpotify(List<String> videoTitles, int limit,List<String> channelNameTitles) {
        System.out.println("searchSongsOnspotify with string called");
        List<TrackInfo> trackInfoList = new ArrayList<>();
        int j = 0;

        if (accessToken == null) {
            System.out.println("Access token is null.");
            return trackInfoList;
        }

        String spotifyApiUrl = "https://api.spotify.com/v1/search";

        for (String videoTitle : videoTitles) {
            try {
                String[] titleParts = parseTitle(videoTitle);

                String song = titleParts[0];
                String encodedSong = URLEncoder.encode(song, StandardCharsets.UTF_8.toString());

                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append(spotifyApiUrl);
                queryBuilder.append("?q=");
                queryBuilder.append(encodedSong);

                if (titleParts.length >= 2){
                    for (int i = 1; i < titleParts.length; i++) {
                        String artist = titleParts[i];
                        String encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString());
                        queryBuilder.append("%20");
                        queryBuilder.append(encodedArtist);
                    }
                }else {
                    String encodedChannelName = URLEncoder.encode(channelNameTitles.get(j),StandardCharsets.UTF_8.toString());
                    queryBuilder.append("%20");
                    queryBuilder.append(encodedChannelName);
                }


                queryBuilder.append("&type=track&limit=" + limit);

                String query = queryBuilder.toString();
                System.out.println(query);

                HttpGet httpGet = new HttpGet(query);
                httpGet.setHeader("Authorization", "Bearer " + accessToken);
                CloseableHttpResponse response = httpClient.execute(httpGet);

                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

                if (responseJson.has("tracks")) {

                    JsonArray tracksArray = responseJson.getAsJsonObject("tracks").getAsJsonArray("items");

                    for (JsonElement track : tracksArray) {
                        TrackInfo newTrack = extractTrackInfo(track);
                        System.out.println(newTrack);
                        if (!isDuplicate(trackInfoList, newTrack)) {
                            trackInfoList.add(newTrack);
                        }
                    }
                }
                response.close();


                j++;
            } catch (Exception e) {
                System.out.println("Error searching on Spotify: " + e);
            }
        }

        return trackInfoList;
    }


    private String buildSpotifySearchUrl(TrackInfo trackInfo) {
        // If a Spotify URI is available, use it to directly access the track
        if (trackInfo.getSpotifyUri() != null && !trackInfo.getSpotifyUri().isEmpty()) {
            String trackId = trackInfo.getSpotifyUri().split(":")[2];
            System.out.println("Using track ID for query: " + trackId);
            return "https://api.spotify.com/v1/tracks/" + trackId;
        } else {
            // Start building the search query
            StringBuilder queryBuilder = new StringBuilder("https://api.spotify.com/v1/search?q=");

            // Check if both artist and song name are not null/empty, and optionally include album
            if (trackInfo.getArtist() != null && !trackInfo.getArtist().isEmpty() && trackInfo.getTitle() != null && !trackInfo.getTitle().isEmpty()) {
                queryBuilder.append(URLEncoder.encode("artist:" + trackInfo.getArtist() + " ", StandardCharsets.UTF_8));
                queryBuilder.append(URLEncoder.encode("track:" + trackInfo.getTitle(), StandardCharsets.UTF_8));

                // Include album in the search if it's not null/empty
                if (trackInfo.getAlbum() != null && !trackInfo.getAlbum().isEmpty()) {
                    queryBuilder.append(URLEncoder.encode(" album:" + trackInfo.getAlbum(), StandardCharsets.UTF_8));
                }
            } else if (trackInfo.getArtist() == null || trackInfo.getArtist().isEmpty()) {
                // If the artist is null, search only by song name
                queryBuilder.append(URLEncoder.encode("track:" + trackInfo.getTitle(), StandardCharsets.UTF_8));
            } else {
                // Fallback to searching with whatever information is available (e.g., only artist or song name)
                if (!trackInfo.getTitle().isEmpty()) {
                    queryBuilder.append(URLEncoder.encode("track:" + trackInfo.getTitle(), StandardCharsets.UTF_8));
                }
                if (!trackInfo.getArtist().isEmpty()) {
                    queryBuilder.append(URLEncoder.encode(" artist:" + trackInfo.getArtist(), StandardCharsets.UTF_8));
                }
            }

            queryBuilder.append("&type=track&limit=1");

            return queryBuilder.toString();
        }
    }

    private List<TrackInfo> performSpotifySearch(String url) throws Exception {
        List<TrackInfo> foundTracks = new ArrayList<>();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        System.out.println("Spotify URL: "+url);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

            System.out.println("spotifyresponse: " + responseBody);
            // Assuming handling for multiple types might be necessary, adjust as needed
            if (responseJson.has("tracks")) {
                JsonArray tracksArray = responseJson.getAsJsonObject("tracks").getAsJsonArray("items");
                for (JsonElement trackElement : tracksArray) {
                    foundTracks.add(extractTrackInfo(trackElement));
                }
            }else{
                System.out.println("hej");
                //  JsonObject trackObject = responseJson.getAsJsonObject("track");
                foundTracks.add(extractTrackInfo(responseJson));
            }


            // Extend this to handle albums and artists if needed
        }
        return foundTracks;
    }

    private boolean isDuplicate(List<TrackInfo> trackInfoList, TrackInfo newTrack) {
        System.out.println("isDuplicate called");
        for (TrackInfo existingTrack : trackInfoList) {
            if (existingTrack.getTitle().equalsIgnoreCase(newTrack.getTitle()) &&
                    existingTrack.getArtist().equalsIgnoreCase(newTrack.getArtist())) {
                return true;
            }
        }
        return false;
    }

    private TrackInfo extractTrackInfo(JsonElement track) {
        System.out.println("extractTrackInfo called");
        JsonObject trackInfoJson = new JsonObject();
        trackInfoJson.addProperty("title", track.getAsJsonObject().getAsJsonPrimitive("name").getAsString());
        trackInfoJson.addProperty("artist", track.getAsJsonObject().getAsJsonArray("artists").get(0).getAsJsonObject().getAsJsonPrimitive("name").getAsString());
        trackInfoJson.addProperty("imageUrl", track.getAsJsonObject().getAsJsonObject("album").getAsJsonArray("images").get(0).getAsJsonObject().getAsJsonPrimitive("url").getAsString());
        trackInfoJson.addProperty("album", track.getAsJsonObject().getAsJsonObject("album").getAsJsonPrimitive("name").getAsString());

        String trackId = track.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
        String trackUri = "spotify:track:" + trackId;
        trackInfoJson.addProperty("uri", trackUri);


        return gson.fromJson(trackInfoJson, TrackInfo.class);
    }









    private String[] parseTitle(String title) {
        System.out.println("parseTitle called");
        // Remove anything inside parentheses
        title = title.replaceAll("\\(.*?\\)", "").trim();

        // Split by " - " or "–"
        String[] parts = title.split("\\s+-\\s+|\\s+–\\s+");

        // Trim leading and trailing whitespaces
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        // If there's only one part, return it immediately
        if (parts.length < 2) {
            return parts;
        }

        // If the title contains "feat." or "ft.", split it into song, main artist, and featuring artist(s)
        if (parts[1].contains("feat.") || parts[1].contains("ft.")) {
            String[] songParts = parts[1].split("(?:feat\\.|ft\\.)");
            if (songParts.length == 2) {
                String song = songParts[0].trim();
                String[] artists = songParts[1].split(",");
                String mainArtist = parts[0].trim();
                String[] newParts = new String[artists.length + 2];
                newParts[0] = song;
                newParts[1] = mainArtist;
                for (int i = 0; i < artists.length; i++) {
                    newParts[i + 2] = artists[i].trim();
                }
                parts = newParts;
            }
        }

        // Return the parts
        return parts;
    }




}
