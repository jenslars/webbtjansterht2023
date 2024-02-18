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


    private final Gson gson;

    public SpotifySongSearcher(String accessToken, Gson gson) {
        this.youTubeVideoInfoExtractor = new YouTubeVideoInfoExtractor();
        this.accessToken = accessToken;
        this.gson = gson;

    }


    /**
     * Söker efter låtar på Spotify och returnerar en lista med matchade låtar.
     *
     * Metoden tar en lista med TrackInfo-objekt som inmatning och söker efter varje låt på Spotify.
     * För varje låt försöker den först att bygga en direkt Spotify-URL om en Spotify-URI finns tillgänglig.
     * Om detta misslyckas försöker den bygga en sök-URL baserad på album, artist och låttitel.
     * Om en matchande låt hittas läggs den till i en lista med matchade låtar.
     * Om ingen matchande låt hittas skrivs ett meddelande ut.
     * Metoden returnerar en lista med de matchade låtarna.
     *
     * @param tracks En lista med TrackInfo-objekt som representerar låtarna som ska sökas på Spotify.
     * @return En lista med TrackInfo-objekt som representerar de matchade låtarna från Spotify.
     */

    public List<TrackInfo> searchSongsOnSpotify(List<TrackInfo> tracks) {
        List<TrackInfo> foundTracks = new ArrayList<>();
        List<TrackInfo> returnedTracks = null;
        if (accessToken == null) {
            System.out.println("Access token is null.");
            return foundTracks;
        }

        for (TrackInfo trackInfo : tracks) {
            System.out.println("Current track being search with on spotify" + trackInfo);
            String searchUrl;
            try {

                if (trackInfo.getSpotifyUri() != null) {
                    searchUrl = buildDirectSpotifyTrackUrl(trackInfo);

                } else {
                    searchUrl = buildSearchUrlWithAlbumArtistTrack(trackInfo);
                }

                returnedTracks = performSpotifySearch(searchUrl);

                if (returnedTracks == null) {
                    System.out.println("foundtracks empty from album search, make new search");
                    searchUrl = buildSearchUrlWithArtistTrack(trackInfo);
                    returnedTracks = performSpotifySearch(searchUrl);

                    if (returnedTracks == null) {
                        System.out.println("foundtracks empty from song + arist search, make new search");
                        searchUrl = buildSearchUrlWithTrack(trackInfo);
                        returnedTracks = performSpotifySearch(searchUrl);

                        if (returnedTracks == null) {
                            System.out.println("track not found with filtering track only, making general search");
                            searchUrl = buildGeneralSpotifySearchUrl(trackInfo);
                            returnedTracks = performSpotifySearch(searchUrl);

                            if (returnedTracks == null) {
                                System.out.println("track not found on spotify");
                            }
                        }
                    }

                }
            } catch (Exception e) {
                System.out.println("Error searching on Spotify: " + e.getMessage());
            }
            if (returnedTracks != null) {

                foundTracks.addAll(returnedTracks);
            }
        }
        return foundTracks;
    }


    /**
     * Konstruerar en  Spotify-URL för en given låt baserat på låtens Spotify-URI.
     *
     * @param trackInfo TrackInfo-objekt som representerar låten.
     * @return En direkt Spotify-URL för låten.
     */
    private String buildDirectSpotifyTrackUrl(TrackInfo trackInfo) {
        String trackId = trackInfo.getSpotifyUri().split(":")[2];
        System.out.println("Using track ID for query: " + trackId);
        return "https://api.spotify.com/v1/tracks/" + trackId;
    }

    /**
     * Konstruerar en Spotify-sök-URL för en given låt baserat på album, artist och låttitel.
     *
     * @param trackInfo TrackInfo-objekt som representerar låten.
     * @return En Spotify-sök-URL för låten baserat på album, artist och låttitel.
     */
    private String buildSearchUrlWithAlbumArtistTrack(TrackInfo trackInfo) {
        return "https://api.spotify.com/v1/search?q=" +
                URLEncoder.encode("album:" + trackInfo.getAlbum() + " ", StandardCharsets.UTF_8) +
                URLEncoder.encode("artist:" + trackInfo.getArtist() + " ", StandardCharsets.UTF_8) +
                URLEncoder.encode("track:" + trackInfo.getTitle(), StandardCharsets.UTF_8) +
                "&type=track&limit=1";
    }

    /**
     * Konstruerar en Spotify-sök-URL för en given låt baserat på artist och låttitel.
     *
     * @param trackInfo TrackInfo-objekt som representerar låten.
     * @return En Spotify-sök-URL för låten baserat på artist och låttitel.
     */
    private String buildSearchUrlWithArtistTrack(TrackInfo trackInfo) {
        return "https://api.spotify.com/v1/search?q=" +
                URLEncoder.encode("artist:" + trackInfo.getArtist() + " ", StandardCharsets.UTF_8) +
                URLEncoder.encode("track:" + trackInfo.getTitle(), StandardCharsets.UTF_8) +
                "&type=track&limit=1";
    }
    /**
     * Konstruerar en Spotify-sök-URL för en given låt baserat endast på låttitel.
     *
     * @param trackInfo TrackInfo-objekt som representerar låten.
     * @return En Spotify-sök-URL för låten baserat endast på låttitel.
     */
    private String buildSearchUrlWithTrack(TrackInfo trackInfo) {
        return "https://api.spotify.com/v1/search?q=" +
                URLEncoder.encode("track:" + trackInfo.getTitle(), StandardCharsets.UTF_8) +
                "&type=track&limit=1";
    }

    /**
     * Konstruerar en generell Spotify-sök-URL för en given låt baserat på låttitel och artist.
     *
     * @param trackInfo TrackInfo-objekt som representerar låten.
     * @return En generell Spotify-sök-URL för låten baserat på låttitel och artist.
     */
    private String buildGeneralSpotifySearchUrl(TrackInfo trackInfo) {
        String searchQuery = trackInfo.getTitle() + " " + trackInfo.getArtist();
        return "https://api.spotify.com/v1/search?q=" +
                URLEncoder.encode(searchQuery, StandardCharsets.UTF_8) +
                "&type=track&limit=1";
    }

    /**
     * Utför en sökning på Spotify och returnerar en lista med matchade låtar.
     *
     * @param url URL:n för Spotify-sökningen.
     * @return En lista med TrackInfo-objekt som representerar de matchade låtarna från Spotify.
     * @throws Exception Om det uppstår ett fel vid utförandet av sökningen.
     */
    private List<TrackInfo> performSpotifySearch(String url) throws Exception {
        List<TrackInfo> foundTracks = new ArrayList<>();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        System.out.println("Spotify URL: " + url);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

                // System.out.println("spotifyresponse: " + responseBody);

                if (responseJson.has("tracks")) {

                    if (responseJson.has("tracks") && responseJson.getAsJsonObject("tracks").get("total").getAsInt() > 0) {
                        JsonArray tracksArray = responseJson.getAsJsonObject("tracks").getAsJsonArray("items");
                        for (JsonElement trackElement : tracksArray) {
                            foundTracks.add(extractTrackInfo(trackElement));
                        }


                    } else {
                        System.out.println("No tracks found");
                        return null;

                    }
                } else {

                    foundTracks.add(extractTrackInfo(responseJson));
                }

            }
        }
        return foundTracks;
    }

    /**
     * Extraherar information om en låt från en JSON-representation och returnerar en TrackInfo-objekt.
     *
     * @param track JSON-element som representerar information om låten.
     * @return Ett TrackInfo-objekt som representerar låten med extraherad information.
     */
    private TrackInfo extractTrackInfo(JsonElement track) {
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


}

