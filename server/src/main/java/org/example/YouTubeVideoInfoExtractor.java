package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.time.Duration;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YouTubeVideoInfoExtractor {

    /**
     * Konverterar en YouTube-videolänk till en VideoInfo-objekt som innehåller information om videons titel och kanalens namn.
     *
     * @param url        URL:n för YouTube-videon som ska konverteras.
     * @param httpClient En instans av CloseableHttpClient som används för att göra HTTP-begäranden.
     * @return Ett VideoInfo-objekt som innehåller titeln på videon och namnet på kanalen. Returnerar null om ingen video hittades eller om ett fel uppstod.
     */
    public VideoInfo convertVideoString(String url, CloseableHttpClient httpClient) {
        System.out.println(" convertvideoString called");
        System.out.println("Received URL: " + url);
        String videoId = extractVideoId(url);

        if (videoId != null) {
            String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";
            String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/videos";
            String youtubeApiParams = String.format("part=snippet&id=%s&key=%s", videoId, apiKey);

            HttpGet httpGet = new HttpGet(youtubeApiUrl + "?" + youtubeApiParams);
            try {
                CloseableHttpResponse response = httpClient.execute(httpGet);
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Gson gson = new Gson();
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                if (responseJson.has("items")) {
                    JsonArray itemsArray = responseJson.getAsJsonArray("items");


                    for (JsonElement item : itemsArray) {
                        JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                        String videoTitle = snippet.getAsJsonPrimitive("title").getAsString();
                        String channelName = snippet.getAsJsonPrimitive("channelTitle").getAsString();


                        System.out.println("Video Title: " + videoTitle);
                        System.out.println("Channel name: " + channelName);

                        return new VideoInfo(videoTitle, channelName);
                    }


                } else {
                    System.out.println("No videos found");
                }
            } catch (Exception e) {
                System.out.println("Error fetching YouTube video: " + e);
            }
        }
        return null;
    }

    /**
     * Hämtar längden på en YouTube-video baserat på dess URL.
     *
     * @param url URL:n för YouTube-videon vars längd ska hämtas.
     * @return Längden på videon i ISO 8601-format (ex. "PT1H3M52S"). Returnerar null om videon inte hittades eller om ett fel uppstår.
     */
    public String fetchVideoDuration(String url) {
        System.out.println("fetchVideoDuration called");
        String videoId = extractVideoId(url);

        if (videoId == null) {
            System.out.println("Invalid URL or Video ID not found.");
            return null;
        }

        String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";
        String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/videos";
        String params = String.format("part=contentDetails&id=%s&key=%s", videoId, apiKey);


        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(youtubeApiUrl + "?" + params);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

            if (responseJson.has("items") && responseJson.getAsJsonArray("items").size() > 0) {
                JsonObject contentDetails = responseJson.getAsJsonArray("items").get(0).getAsJsonObject().getAsJsonObject("contentDetails");
                String duration = contentDetails.get("duration").getAsString();

                return duration;
            } else {
                System.out.println("No video found for the given ID.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error fetching video duration: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parsar titeln på en låt för att extrahera artist och låtnamn.
     *
     * @param title Titeln på låten som ska parsas.
     * @return Ett TrackInfo-objekt som innehåller artist och låtnamn från titeln.
     */
    public static TrackInfo parseTitle(String title) {
        System.out.println("parseTitle called");
        title = title.replaceAll("\\s+", " ").trim();

        String regex = "^(.*?)\\s+-\\s+(.*?)(?:\\s+\\((feat\\.?.*|ft\\.?.*)\\))?$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);

        String artist = "";
        String songName = "";
        String featuredArtistsString = "";

        if (matcher.find()) {
            artist = matcher.group(1).trim();
            songName = matcher.group(2).trim();
            String featPart = matcher.group(3);

            if (featPart != null && !featPart.isEmpty()) {
                featPart = featPart.replaceAll("(feat\\.|ft\\.)", "").trim();
                List<String> featuredArtists = Arrays.asList(featPart.split(",|and"));
                featuredArtists = featuredArtists.stream().map(String::trim).collect(Collectors.toList());
                featuredArtistsString = String.join(", ", featuredArtists);
            }
        }


        String fullArtist = artist + (!featuredArtistsString.isEmpty() ? " feat. " + featuredArtistsString : "");
        return new TrackInfo(songName, fullArtist);
    }

    /**
     * Parsar låtnamnet från titeln på en video.
     *
     * @param videoTitle Titeln på videon från vilken låtnamnet ska parsas.
     * @return Låtnamnet som extraheras från titeln. Returnerar den ursprungliga titeln om den inte innehåller " - ".
     */
    public static String parseSongNameFromTitle(String videoTitle) {

        String[] parts = videoTitle.split(" - ", 2);

        if (parts.length == 2) {
            return parts[1].trim();
        }

        return videoTitle;
    }

    /**
     * Extraherar videoidentifikatorn från en YouTube-video-URL.
     *
     * @param url URL:en från vilken videoidentifikatorn ska extraheras.
     * @return Videoidentifikatorn om den hittas, annars null.
     */
    private String extractVideoId(String url) {
        System.out.println("extractVideoID called");
        String regex = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Extraherar spellistan-ID från en YouTube-spellista-URL.
     *
     * @param url URL:en från vilken spellista-ID ska extraheras.
     * @return Spellista-ID om det hittas, annars null.
     */
    public String extractPlaylistId(String url) {
        System.out.println("extract playlist id called");
        String regex = "[&?]list=([^&]+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extraherar tidsstämpeln från en YouTube-video-URL.
     *
     * @param youtubeUrl URL:en från vilken tidsstämpeln ska extraheras.
     * @return Tidsstämpeln i sekunder om den hittas, annars en tom sträng.
     */
    public String extractTimestamp(String youtubeUrl) {
        try {
            URI uri = new URI(youtubeUrl);
            String query = uri.getQuery();
            if (query == null) return "";


            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("t=")) {
                    String value = param.split("=")[1];
                    if (value.matches("\\d+")) {
                        return value;
                    }
                }
            }
        } catch (URISyntaxException e) {
            System.err.println("Invalid URL: " + e.getMessage());
        }

        return "";
    }
}
