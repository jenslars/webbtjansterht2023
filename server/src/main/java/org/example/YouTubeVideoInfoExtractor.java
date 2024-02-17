package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
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
    private CloseableHttpClient httpClient;


    public YouTubeVideoInfoExtractor(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

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
                System.out.println(responseBody);
                Gson gson = new Gson();
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                if (responseJson.has("items")) {
                    JsonArray itemsArray = responseJson.getAsJsonArray("items");


                    for (JsonElement item : itemsArray) {
                        JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                        String videoTitle = snippet.getAsJsonPrimitive("title").getAsString();
                        String channelName = snippet.getAsJsonPrimitive("channelTitle").getAsString();
                        // channelName = channelName.replace(" - Topic", "");


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


    public String fetchVideoDuration(String url, CloseableHttpClient httpClient) {
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

        // Combine the artist and featured artists into one string
        String fullArtist = artist + (!featuredArtistsString.isEmpty() ? " feat. " + featuredArtistsString : "");
        return new TrackInfo(songName, fullArtist);
    }

    public static String parseSongNameFromTitle(String videoTitle) {
        // Split the title by " - " assuming the format "Artist Name - Song Title"
        String[] parts = videoTitle.split(" - ", 2);
        // If the title contains " - ", assume the second part is the song name.
        if (parts.length == 2) {
            return parts[1].trim();
        }
        // Return the original title if it does not contain " - "
        return videoTitle;
    }

    private String extractVideoId(String url) {
        System.out.println("extractVideoID called");
        String regex = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    public String extractPlaylistId(String url) {
        System.out.println("extract playlist id called");
        String regex = "[&?]list=([^&]+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    public String extractTimestamp(String youtubeUrl) {
        try {
            URI uri = new URI(youtubeUrl);
            String query = uri.getQuery();
            if (query == null) return "";

            // Simple parsing assuming 't' is always numeric
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("t=")) {
                    String value = param.split("=")[1];
                    if (value.matches("\\d+")) { // Check if 't' value is numeric
                        return value; // Return the timestamp in seconds
                    }
                }
            }
        } catch (URISyntaxException e) {
            System.err.println("Invalid URL: " + e.getMessage());
        }

        return ""; // Return an empty string if no valid timestamp is found
    }
}
