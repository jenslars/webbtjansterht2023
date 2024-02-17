package org.example;

import com.acrcloud.utils.ACRCloudRecognizer;
import org.apache.http.HttpStatus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpHeaders;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;


import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.example.DuplicateChecker.isDuplicate;

public class ServerRunner {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = null;
    HttpPost httpPost = null;
    HttpPut httpPut = null;
    CloseableHttpResponse response = null;
    static String accessToken = null;
    SongRecognizer songRecognizer;

    SpotifySongSearcher spotifySongSearcher;
    YouTubeVideoInfoExtractor youTubeVideoInfoExtractor;

    static Gson gson = new Gson();

    public ServerRunner() {
        requestAccessToken();
        this.spotifySongSearcher = new SpotifySongSearcher(accessToken, gson, httpClient);
        this.youTubeVideoInfoExtractor = new YouTubeVideoInfoExtractor(httpClient);
    }


    /**
     * Main-metod som startar servern och lyssnar efter API-anrop.
     *
     * @param args
     */
    public static void main(String[] args) {
        ServerRunner serverRunner = new ServerRunner();


        Javalin app = Javalin.create(config -> {
                })
                .get("/", ctx -> {
                    String htmlContent = serverRunner.readHtmlFile("static/views/index.html");
                    ctx.html(htmlContent);
                })
                .get("/login", ctx -> {
                    String htmlContent = serverRunner.readHtmlFile("static/views/login.html");
                    ctx.html(htmlContent);
                })
                .post("/createPlaylist", ctx -> {
                    JsonObject requestBody = new JsonParser().parse(ctx.body()).getAsJsonObject();
                    JsonArray trackUrisJson = requestBody.getAsJsonArray("trackUris");
                    if (trackUrisJson == null) {
                        ctx.status(400).result("Bad Request: trackUris is missing or not an array");
                        return;
                    }

                    List<String> trackUris = new ArrayList<>();
                    for (JsonElement trackUriJson : trackUrisJson) {
                        trackUris.add(trackUriJson.getAsString());
                    }

                    System.out.println(trackUris);
                    String playlistId = serverRunner.createPlaylist(ctx, trackUris);

                    // Create a JSON response object with the playlistId
                    JsonObject response = new JsonObject();
                    response.addProperty("playlistId", playlistId);

                    // Set the response content type to JSON
                    ctx.contentType("application/json");

                    // Send the JSON response
                    ctx.result(response.toString());
                })
                .post("/addTracksToPlaylist", ctx -> {
                    JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    String playlistId = requestBody.get("playlistId").getAsString();
                    JsonArray trackUrisJson = requestBody.getAsJsonArray("trackUris");

                    if (trackUrisJson != null) {
                        List<String> trackUris = new ArrayList<>();
                        for (JsonElement trackUriJson : trackUrisJson) {
                            trackUris.add(trackUriJson.getAsString());
                        }

                        // Implement the logic to add tracks to the playlist
                        // You should replace this with your actual implementation
                        serverRunner.addTracksToPlaylist(playlistId, trackUris);

                        // Construct and send the response
                        JsonObject response = new JsonObject();
                        response.addProperty("status", "success");
                        response.addProperty("message", "Tracks added to playlist successfully.");
                        ctx.json(response.toString());
                    } else {
                        // Handle the case where trackUrisJson is null or not an array
                        JsonObject response = new JsonObject();
                        response.addProperty("status", "error");
                        response.addProperty("message", "Invalid request data.");
                        ctx.json(response.toString());
                    }
                })

                .get("/getUserPlaylists", ctx -> {
                    List<Playlist> playlists = serverRunner.fetchUserPlaylists();

                    ctx.status(200);
                    ctx.json(playlists);
                })

                .get("/convertPlaylist", ctx -> {
                    String url = ctx.queryParam("url");

                    List<TrackInfo> trackInfoList = serverRunner.convertPlayList(url);

                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Playlist converted successfully");
                    jsonResponse.add("tracks", gson.toJsonTree(trackInfoList));
                    ctx.json(jsonResponse.toString());
                })
                //används inte
                .get("/identifyAllSongs", ctx -> {
                    /* 
                    String url = ctx.queryParam("url");
                    System.out.println("url: " + url);
                    List<TrackInfo> trackInfoList = serverRunner.identifyAllSongs(url);

                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Video converted successfully");
                    jsonResponse.add("tracks", gson.toJsonTree(trackInfoList)); 
                    ctx.json(jsonResponse.toString());
                    */
                })
                .get("/convertVideo", ctx -> {
                    System.out.println("convertvideo get called from frontend");
                    String url = ctx.queryParam("url");
                    System.out.println("url: " + url);
                    List<TrackInfo> trackInfoList = serverRunner.convertVideo(url);

                    System.out.println(trackInfoList);

                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Video converted successfully");
                    jsonResponse.add("tracks", gson.toJsonTree(trackInfoList));
                    ctx.json(jsonResponse.toString());

                })
                .get("/identifyAllSongsInVideo", ctx -> {
                    System.out.println("identifyAllSongsInVideo get called from frontend");

                    String url = ctx.queryParam("url");

                    List<TrackInfo> trackInfoList = serverRunner.identifyAllSongsInVideo(url);


                    System.out.println("Print all songs found from spotify: nmbr of songs: " + trackInfoList.size());
                    for (int i = 0; i < trackInfoList.size(); i++) {
                        System.out.println(trackInfoList.get(i));
                    }


                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Video converted successfully");
                    jsonResponse.add("tracks", gson.toJsonTree(trackInfoList));
                    ctx.json(jsonResponse.toString());
                })
                .get("/callback", ctx -> {
                    String code = ctx.queryParam("code");
                    serverRunner.exchangeCodeForAccessToken(code);
                    String htmlContent = serverRunner.readHtmlFile("static/views/redirect.html");
                    ctx.html(htmlContent);
                })
                .get("/scripts/{filename}", ctx -> {
                    serverRunner.serveJavaScriptFile(ctx);
                })
                .get("/static/styling/{filename}", ctx -> {
                    serverRunner.serveCssFile(ctx);
                });
        app.get("/images/svg/{filename}", ctx -> {
            serverRunner.serveSvgFile(ctx);
        });
        app.get("/images/png/{filename}", ctx -> {
                    serverRunner.servePngFile(ctx);
                })
                .get("/{id}", ctx -> {
                    serverRunner.getSongUrl(ctx);
                })
                .get("/playlist/{id}", ctx -> {
                    serverRunner.getPlaylistID(ctx);
                })
                /*.post("/{id}", ctx -> {
                    serverRunner.addSongToPlaylist(ctx);
                })*/
                .start(5000);

        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            ctx.header("Access-Control-Allow-Headers", "*");
        });


    }


    private List<Playlist> fetchUserPlaylists() {
        System.out.println("fetch user playlist called");
        try {
            String userId = getUserId(accessToken);
            String url = "https://api.spotify.com/v1/me/playlists";
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray itemsArray = responseJson.getAsJsonArray("items");

                List<Playlist> playlists = new ArrayList<>();

                for (JsonElement item : itemsArray) {
                    JsonObject playlistJson = item.getAsJsonObject();
                    JsonObject owner = playlistJson.getAsJsonObject("owner");

                    // Check if the current user is the owner of the playlist
                    if (owner != null && userId.equals(owner.get("id").getAsString())) {
                        String playlistName = playlistJson.get("name").getAsString();
                        String playlistId = playlistJson.get("id").getAsString();

                        // Extract the image URL
                        String imageUrl = "";
                        if (playlistJson.getAsJsonArray("images").size() > 0) {
                            imageUrl = playlistJson.getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString();
                        }

                        // Extract the number of tracks
                        int trackCount = playlistJson.getAsJsonObject("tracks").get("total").getAsInt();

                        Playlist playlist = new Playlist(playlistName, playlistId, imageUrl, trackCount);

                        playlists.add(playlist);

                    }
                }
                return playlists;
            } else {
                System.out.println("Error fetching user playlists. Status code: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    /**
     * Metod för att hämta URL till den låt som användaren/Shazam angav.
     *
     * @param ctx
     */
    private void getSongUrl(Context ctx) {
        System.out.println("get song url called");
        String url = "https://api.spotify.com/v1/search?q=track%3A" + ctx.pathParam("id") + "&type=track";
        httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization: Bearer ", accessToken);
        try {
            response = httpClient.execute(httpGet);
        } catch (Exception e) {
            System.out.println(e);
        }
        ctx.json(response);
    }


    private List<TrackInfo> convertVideo(String url) {
        songRecognizer = new SongRecognizer();
        System.out.println(" convertvideo called");
        System.out.println("Received URL: " + url);
        List<TrackInfo> tracks = new ArrayList<>();
        VideoInfo videoInfo = null;


        String startTime = youTubeVideoInfoExtractor.extractTimestamp(url); // Extract the timestamp from the URL

        if (!Objects.equals(startTime, "")) {
            System.out.println("Timestamp used");
            //videoInfo = youTubeVideoInfoExtractor.convertVideoString(url,httpClient);
            tracks = songRecognizer.identifyYouTubeVideo(url, Integer.parseInt(startTime));
        } else {
            videoInfo = youTubeVideoInfoExtractor.convertVideoString(url, httpClient);
            tracks = songRecognizer.identifyYouTubeVideo(url, -1);
        }

        if (videoInfo != null) {
            TrackInfo ytTrack = YouTubeVideoInfoExtractor.parseTitle(videoInfo.getVideoTitle());
            if (!DuplicateChecker.isDuplicate(tracks, ytTrack)) {
                tracks.add(ytTrack);
            }

            String songname = YouTubeVideoInfoExtractor.parseSongNameFromTitle(videoInfo.getVideoTitle());
            TrackInfo ytTrack1 = new TrackInfo(songname, videoInfo.getChannelName());
            if (!DuplicateChecker.isDuplicate(tracks, ytTrack)) {
                tracks.add(ytTrack1);
            }
        }

        if (tracks != null && !tracks.isEmpty()) {
            tracks = spotifySongSearcher.searchSongsOnSpotify(tracks);
            return tracks;
        } else {
            // tracks = convertVideoString(url);
        }
        return tracks;
    }


// Ensure the TrackInfo class, extractTrackInfo, and isDuplicate methods are correctly implemented.


    /**
     * Metod för att konvertera YouTube-spellista till Spotify-spellista.
     */
    private List<TrackInfo> convertVideoString(String url) {

        System.out.println(" convertvideo called");
        System.out.println("Received URL: " + url);
        String videoId = extractVideoId(url);

        if (videoId != null) {
            String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";
            String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/videos";
            String youtubeApiParams = String.format("part=snippet&id=%s&key=%s", videoId, apiKey);

            httpGet = new HttpGet(youtubeApiUrl + "?" + youtubeApiParams);
            try {
                response = httpClient.execute(httpGet);
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println(responseBody);
                Gson gson = new Gson();
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                if (responseJson.has("items")) {
                    JsonArray itemsArray = responseJson.getAsJsonArray("items");

                    List<String> videoTitles = new ArrayList<>();
                    List<String> channelNames = new ArrayList<>();

                    // Extract video details from the response
                    for (JsonElement item : itemsArray) {
                        JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                        String videoTitle = snippet.getAsJsonPrimitive("title").getAsString();
                        String channelName = snippet.getAsJsonPrimitive("channelTitle").getAsString();
                        channelName = channelName.replace(" - Topic", "");


                        System.out.println("Video Title: " + videoTitle);
                        System.out.println("Channel name: " + channelName);
                        videoTitles.add(videoTitle);
                        channelNames.add(channelName);
                    }
                    return spotifySongSearcher.searchSongsOnSpotify(videoTitles, 1, channelNames);

                } else {
                    System.out.println("No videos found");
                }
            } catch (Exception e) {
                System.out.println("Error fetching YouTube video: " + e);
            }
        }

        return new ArrayList<>();
    }


    private List<TrackInfo> identifyAllSongsInVideo(String url) {
        songRecognizer = new SongRecognizer();
        System.out.println(" identifyAllSongsInVideo called");
        System.out.println("Received URL: " + url);
        String videoId = extractIdMultipleSongs(url);
        List<TrackInfo> tracks = new ArrayList<>();
        System.out.println("youtube video id:" + videoId);

        String ISOduration = youTubeVideoInfoExtractor.fetchVideoDuration(url, httpClient);
        Duration duration = Duration.parse(ISOduration);
        System.out.println(ISOduration);
        System.out.println("video duration in seconds: " + duration.getSeconds());


        tracks = songRecognizer.identifyAllSongsInYTVideo(url, (double) duration.getSeconds());

        if (tracks != null && !tracks.isEmpty()) {
            tracks = spotifySongSearcher.searchSongsOnSpotify(tracks);
            System.out.println("Number of songs added to array and being sent to frontend: " + tracks.size());
            tracks = DuplicateChecker.checkDuplicates(tracks);
            return tracks;

        } else {
            // tracks = convertVideoString(url);
        }
        return tracks;
    }



    private List<TrackInfo> identifyAllSongsInVideoOLD(String url) {
        System.out.println(" identifyAllSongsInVideo called");
        System.out.println("Received URL: " + url);
        String videoId = extractIdMultipleSongs(url);
        List<String> trackList = new ArrayList<>();
        System.out.println(videoId);
        if (videoId != null) {
            String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";
            String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/videos?id=";
            String youtubeApiParams = String.format("%s&key=%s", videoId, apiKey + "&part=snippet");

            httpGet = new HttpGet(youtubeApiUrl + youtubeApiParams);

            try {
                System.out.println("Hej :^)");
                response = httpClient.execute(httpGet);
                String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray items = jsonObject.getAsJsonArray("items");


                JsonObject videoInfo = items.get(0).getAsJsonObject();
                JsonObject snippet = videoInfo.get("snippet").getAsJsonObject();
                String description = snippet.get("description").getAsString();

                String regex = "\\b([0-5]?\\d):([0-5]?\\d)(?::([0-5]?\\d))?\\b";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(description);

                while (matcher.find()) {
                    int start = matcher.start();
                    int end = description.indexOf("\n", start);
                    if (end == -1) {
                        end = description.length();
                    }
                    String matchedLine = description.substring(start, end).trim();
                    matchedLine = matchedLine.replaceFirst(regex, "");
                    System.out.println(matchedLine);
                    trackList.add(matchedLine);
                }

                return spotifySongSearcher.searchSongsOnSpotify(trackList, 1, null);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Extract video ID
     *
     * @param url
     * @return
     */
    private String extractVideoId(String url) {
        System.out.println("extractVideoID called");
        String regex = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractIdMultipleSongs(String url) {
        System.out.println("extractIdmultipleSongs called");
        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host.endsWith("youtube.com") || host.endsWith("youtu.be")) {
                String path = uri.getPath();
                if (path != null && path.startsWith("/watch")) {
                    String query = uri.getQuery();
                    if (query != null && query.contains("v=")) {
                        String[] parts = query.split("v=");
                        String videoId = parts[1];

                        return videoId;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return "ID not found";
    }






    private List<TrackInfo> convertPlayList(String url){
        // Your logic to extract the playlist ID from the URL
        String playlistId = extractPlaylistId(url);
        if (playlistId == null) {
            System.out.println("Invalid YouTube playlist URL");
            return null;
        }

        songRecognizer = new SongRecognizer();

        String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY"; // Use your actual YouTube Data API key
        String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/playlistItems";
        String nextPageToken = "";
        List<String> videoUrlList = new ArrayList<>();
        List<TrackInfo> trackInfoList = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            do {
                String youtubeApiParams = String.format("part=snippet&playlistId=%s&key=%s&maxResults=50", playlistId, apiKey);
                if (!nextPageToken.isEmpty()) {
                    youtubeApiParams += "&pageToken=" + nextPageToken;
                }

                String requestUrl = youtubeApiUrl + "?" + youtubeApiParams;
                HttpGet request = new HttpGet(requestUrl);
                String responseBody = EntityUtils.toString(httpClient.execute(request).getEntity(), StandardCharsets.UTF_8);

                JsonObject responseJson = new Gson().fromJson(responseBody, JsonObject.class);
                if (responseJson.has("items")) {
                    JsonArray itemsArray = responseJson.getAsJsonArray("items");
                    for (JsonElement item : itemsArray) {
                        JsonObject resourceId = item.getAsJsonObject().getAsJsonObject("snippet").getAsJsonObject("resourceId");
                        String videoId = resourceId.getAsJsonPrimitive("videoId").getAsString();
                        videoUrlList.add("https://www.youtube.com/watch?v=" + videoId);
                    }
                }

                nextPageToken = responseJson.has("nextPageToken") ? responseJson.getAsJsonPrimitive("nextPageToken").getAsString() : "";
            } while (!nextPageToken.isEmpty());
        } catch (Exception e) {
            System.out.println("Error fetching YouTube playlist: " + e.getMessage());
        }

        List<String> outputPaths = songRecognizer.downloadPlaylistVideosInParallel(videoUrlList);

        for (int i = 0; i < outputPaths.size(); i++) {
            trackInfoList.addAll(songRecognizer.recognizeSongs(outputPaths.get(i),5));
        }


        List<TrackInfo> finalList = spotifySongSearcher.searchSongsOnSpotify(trackInfoList);
        finalList = DuplicateChecker.checkDuplicates(finalList);

        for (int i = 0; i < finalList.size(); i++) {
            System.out.println(finalList.get(i));
        }

        // This function currently returns null, but you have the videoUrls array ready for when you need it
        return finalList;
    }



    private List<TrackInfo> convertPlaylistOLD(String url) {
        System.out.println("convert playlist called");
        System.out.println("Received URL: " + url);
        String playlistId = extractPlaylistId(url);

        if (playlistId != null) {
            String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";
            String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/playlistItems";
            String youtubeApiParams = String.format("part=snippet&playlistId=%s&key=%s&maxResults=50", playlistId, apiKey);
            String nextPageToken = "";

            List<String> videoTitles = new ArrayList<>();
            List<String> channelTitles = new ArrayList<>();

            do {
                if (!nextPageToken.isEmpty()) {
                    youtubeApiParams += "&pageToken=" + nextPageToken;
                }

                httpGet = new HttpGet(youtubeApiUrl + "?" + youtubeApiParams);

                try {
                    response = httpClient.execute(httpGet);
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                    Gson gson = new Gson();
                    JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                    if (responseJson.has("items")) {
                        JsonArray itemsArray = responseJson.getAsJsonArray("items");

                        for (JsonElement item : itemsArray) {
                            if (videoTitles.size() >= 99) {
                                break;
                            }
                            JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                            String videoTitle = snippet.getAsJsonPrimitive("title").getAsString();
                            String channelName = snippet.getAsJsonPrimitive("videoOwnerChannelTitle").getAsString();
                            channelName = channelName.replace(" - Topic", "");

                            System.out.println("Video Title: " + videoTitle);
                            System.out.println("Channel name: " + channelName);


                            channelTitles.add(channelName);
                            videoTitles.add(videoTitle);
                        }
                    } else {
                        System.out.println("No videos found in the playlist");
                    }

                    if (responseJson.has("nextPageToken")) {
                        nextPageToken = responseJson.getAsJsonPrimitive("nextPageToken").getAsString();
                    } else {
                        nextPageToken = "";
                    }
                } catch (Exception e) {
                    System.out.println("Error fetching YouTube playlist: " + e);
                }
            } while (!nextPageToken.isEmpty());

            // Perform a single Spotify search for all video titles
            return spotifySongSearcher.searchSongsOnSpotify(videoTitles, 1, channelTitles);
        } else {
            System.out.println("Invalid YouTube playlist URL");
        }

        return new ArrayList<>();
    }


    private String extractPlaylistId(String url) {
        System.out.println("extract playlist id called");
        String regex = "[&?]list=([^&]+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }


    /**
     * Metod som hämtar Spotify ID:t på den spellista som användaren vill lägga till en låt i.
     *
     * @param ctx
     */
    private void getPlaylistID(Context ctx) {
        // Implementation for getting playlist ID
    }

    /**
     * Metod för att skapa en ny spellista i användarens Spotify-konto.
     *
     * @param ctx
     */
    private String createPlaylist(Context ctx, List<String> trackUris) {
        System.out.println("create Playlist called");
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String userId = getUserId(accessToken);
            HttpPost httpPost = new HttpPost("https://api.spotify.com/v1/users/" + userId + "/playlists");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            JsonObject playlistDetails = new JsonObject();
            playlistDetails.addProperty("name", currentDate);
            playlistDetails.addProperty("description", "Created with Spotify API");
            playlistDetails.addProperty("public", false);
            StringEntity requestEntity = new StringEntity(playlistDetails.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            JsonObject responseJson = new JsonParser().parse(responseBody).getAsJsonObject();
            String playlistId = responseJson.get("id").getAsString();

            addTracksToPlaylist(playlistId, trackUris);

            return playlistId;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    /**
     * Metod för att lägga till låten i användarens Spotify-spellista.
     *
     * @param ctx
     */
    private void addTracksToPlaylist(String playlistId, List<String> trackUris) {
        System.out.println("addTracksToPlaylist called");
        try {
            String spotifyApiUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";
            System.out.println("addtracks");
            HttpPost httpPost = new HttpPost(spotifyApiUrl);
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            httpPost.setHeader("Content-Type", "application/json");

            Gson gson = new Gson();
            String jsonTrackUris = gson.toJson(trackUris);

            StringEntity requestEntity = new StringEntity("{\"uris\":" + jsonTrackUris + "}", ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);

            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println("Response from Spotify API: " + responseBody);

        } catch (Exception e) {
            System.out.println("Error adding tracks to playlist: " + e);
        }
    }

    /**
     * Metod som skickar en förfrågan till Spotify's API för att få en access token som senare används i alla andra anrop till Spotify's API.
     */
    private void requestAccessToken() {
        try {
            httpClient = HttpClients.createDefault();

            String clientID = "c32d1829b55d4c5eac178bc34fdd6728";
            String clientSecret = "b9f53919c0774da89f480a8863d5234e";
            String requestBody = "grant_type=client_credentials&client_id=" + clientID + "&client_secret=" + clientSecret;

            httpPost = new HttpPost("https://accounts.spotify.com/api/token");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new StringEntity(requestBody));

            try {
                response = httpClient.execute(httpPost);
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(responseBody).getAsJsonObject();
                accessToken = json.get("access_token").getAsString();

                // Print the access token for debugging
                System.out.println("Access Token: " + accessToken);
            } catch (Exception e) {
                System.out.println(e);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Metod som byter ut den temporära koden som användaren fick vid inloggning mot en access token.
     *
     * @param code
     */
    private void exchangeCodeForAccessToken(String code) {
        try {
            System.out.println("Exchanging code for access token: " + code);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String clientID = "c32d1829b55d4c5eac178bc34fdd6728";
            String clientSecret = "b9f53919c0774da89f480a8863d5234e";
            String redirectURI = "http://localhost:5000/callback";
            String requestBody = "grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirectURI;

            HttpPost httpPost = new HttpPost("https://accounts.spotify.com/api/token");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
            String auth = clientID + ":" + clientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            accessToken = json.get("access_token").getAsString();

            // Get and print the user ID
            String userId = getUserId(accessToken);
            System.out.println("User ID: " + userId);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Metod för att läsa innehållet i en HTML-fil.
     *
     * @param filePath Sökväg till HTML-filen.
     * @return Innehållet i HTML-filen som en sträng.
     */


    private String getUserId(String accessToken) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("https://api.spotify.com/v1/me");
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            return json.get("id").getAsString();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private String readHtmlFile(String filePath) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            return new String(encoded);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading HTML file.";
        }
    }

    /**
     * Metod för att hämta SVG-fil.
     */
    private void serveSvgFile(io.javalin.http.Context ctx) {
        String fileName = ctx.pathParam("filename");

        Path filePath = Paths.get("images/svg/" + fileName);

        if (Files.exists(filePath)) {
            ctx.contentType("image/svg+xml");

            try {
                ctx.result(new String(Files.readAllBytes(filePath)));
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Internal Server Error");
            }
        } else {
            ctx.status(404).result("File not found");
        }
    }

    /**
     * Metod för att hämta PNG-fil.
     */
    private void servePngFile(io.javalin.http.Context ctx) {
        String fileName = ctx.pathParam("filename");

        Path filePath = Paths.get("images/png/" + fileName);

        if (Files.exists(filePath)) {
            ctx.contentType("image/png");

            try {
                ctx.result(Files.readAllBytes(filePath));
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Internal Server Error");
            }
        } else {
            ctx.status(404).result("File not found");
        }
    }

    /**
     * Metod för att hämta CSS-fil.
     */
    private void serveCssFile(io.javalin.http.Context ctx) {
        String fileName = ctx.pathParam("filename");

        Path filePath = Paths.get("static/styling/" + fileName);

        if (Files.exists(filePath)) {
            ctx.contentType("text/css");

            try {
                ctx.result(new String(Files.readAllBytes(filePath)));
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Internal Server Error");
            }
        } else {
            ctx.status(404).result("File not found");
        }
    }

    /**
     * Metod för att hämta Javascript-fil.
     */
    private void serveJavaScriptFile(Context ctx) {
        String fileName = ctx.pathParam("filename");

        Path filePath = Paths.get("scripts/" + fileName);

        if (Files.exists(filePath)) {
            ctx.contentType("application/javascript");

            try {
                ctx.result(new String(Files.readAllBytes(filePath)));
            } catch (IOException e) {
                e.printStackTrace();
                ctx.status(500).result("Internal Server Error");
            }
        } else {
            ctx.status(404).result("File not found");
        }
    }
}
