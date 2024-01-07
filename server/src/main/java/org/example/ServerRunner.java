package org.example;

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


import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ServerRunner {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = null;
    HttpPost httpPost = null;
    HttpPut httpPut = null;
    CloseableHttpResponse response = null;
    String accessToken = null;
    static Gson gson = new Gson();

    /**
     * Main-metod som startar servern och lyssnar efter API-anrop.
     *
     * @param args
     */
    public static void main(String[] args) {
        ServerRunner serverRunner = new ServerRunner();
        serverRunner.requestAccessToken();
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
                
                
                .get("/convertPlaylist", ctx -> {
                    String url = ctx.queryParam("url");

                    List<TrackInfo> trackInfoList = serverRunner.convertPlaylist(url);

                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Playlist converted successfully");
                    jsonResponse.add("tracks", gson.toJsonTree(trackInfoList)); 
                    ctx.json(jsonResponse.toString());
                })
                //Behöver implementeras
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
                    String url = ctx.queryParam("url");
                    System.out.println("url: " + url);
                    List<TrackInfo> trackInfoList = serverRunner.convertVideo(url);

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

    /**
     * Metod för att hämta URL till den låt som användaren/Shazam angav.
     *
     * @param ctx
     */
    private void getSongUrl(Context ctx) {
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

    /**
     * Metod för att konvertera YouTube-spellista till Spotify-spellista.
    */
    private List<TrackInfo> convertVideo(String url) {
    System.out.println("Received URL: " + url);
    String videoId = extractVideoId(url);

    if (videoId != null) {
        String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";
        String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/videos";
        String youtubeApiParams = String.format("part=snippet&id=%s&key=%s", videoId, apiKey);
        
        httpGet = new HttpGet(youtubeApiUrl +"?" + youtubeApiParams);
        try {
            response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println(responseBody);
            Gson gson = new Gson();
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            if (responseJson.has("items")) {
                JsonArray itemsArray = responseJson.getAsJsonArray("items");

                List<String> videoTitles = new ArrayList<>();

                // Extract video details from the response
                for (JsonElement item : itemsArray) {
                JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                        String videoTitle = snippet.getAsJsonPrimitive("title").getAsString();
                System.out.println("Video Title: " + videoTitle);
                videoTitles.add(videoTitle);
            }
                return searchSongsOnSpotify(videoTitles);
                
            } else {
                System.out.println("No videos found");
            }
        } catch (Exception e) {
            System.out.println("Error fetching YouTube video: " + e);
        }
    }
            return new ArrayList<>();
}
    /**
     * Extract video ID
     * @param url
     * @return
     */
    private String extractVideoId(String url) {
        String regex = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    private List<TrackInfo> convertPlaylist(String url) {
        System.out.println("Received URL: " + url);
        String playlistId = extractPlaylistId(url);
    
        if (playlistId != null) {
            String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";  // Replace with your YouTube API key
            String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/playlistItems";
            String youtubeApiParams = String.format("part=snippet&playlistId=%s&key=%s", playlistId, apiKey);
    
            httpGet = new HttpGet(youtubeApiUrl + "?" + youtubeApiParams);
    
            try {
                response = httpClient.execute(httpGet);
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    
                Gson gson = new Gson();
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
    
                if (responseJson.has("items")) {
                    JsonArray itemsArray = responseJson.getAsJsonArray("items");
    
                    List<String> videoTitles = new ArrayList<>();
    
                    for (JsonElement item : itemsArray) {
                        JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                        String videoTitle = snippet.getAsJsonPrimitive("title").getAsString();
    
                        System.out.println("Video Title: " + videoTitle);
    
                        videoTitles.add(videoTitle);
                    }
    
                    // Perform a single Spotify search for all video titles
                    return searchSongsOnSpotify(videoTitles);
                } else {
                    System.out.println("No videos found in the playlist");
                }
            } catch (Exception e) {
                System.out.println("Error fetching YouTube playlist: " + e);
            }
        } else {
            System.out.println("Invalid YouTube playlist URL");
        }
    
        return new ArrayList<>();
    }

    private String extractPlaylistId(String url) {
        String regex = "[&?]list=([^&]+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Metod för att hämta lista av hittade låtar hos Spotify
     * Tar lista av strängar(titlar) som input och kan återanvändas av andra metoder
    */

    private List<TrackInfo> searchSongsOnSpotify(List<String> youtubeTitles) {
        List<TrackInfo> trackInfoList = new ArrayList<>();
    
        if (accessToken == null) {
            System.out.println("Access token is null.");
            return trackInfoList;
        }
    
        String spotifyApiUrl = "https://api.spotify.com/v1/search";
    
        for (String youtubeTitle : youtubeTitles) {
            try {
                String[] titleParts = parseTitle(youtubeTitle);
                if (titleParts.length == 2) {
                    String artist = titleParts[0];
                    String song = titleParts[1];
    
                    // Encode artist and track separately
                    String encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString());
                    String encodedSong = URLEncoder.encode(song, StandardCharsets.UTF_8.toString());
    
                    // Construct the Spotify API search URL with encoded parameters
                    String searchUrl = String.format("%s?q=artist:%s%%20track:%s&type=track", spotifyApiUrl, encodedArtist, encodedSong);
                    System.out.println(searchUrl);
                    // Perform the Spotify API search
                    HttpGet httpGet = new HttpGet(searchUrl);
                    httpGet.setHeader("Authorization", "Bearer " + accessToken);
                    CloseableHttpResponse response = httpClient.execute(httpGet);
    
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    
                    JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
    
                    if (responseJson.has("tracks")) {
                        JsonArray tracksArray = responseJson.getAsJsonObject("tracks").getAsJsonArray("items");
    
                        for (JsonElement track : tracksArray) {
                            // Extract track information and add to the result list
                            TrackInfo newTrack = extractTrackInfo(track);
                            if (!isDuplicate(trackInfoList, newTrack)) {
                                trackInfoList.add(newTrack);
                            }
                        }
                    }
                    response.close();
                } else {
                    // Handle cases where parsing did not result in expected parts
                    System.out.println("Error parsing YouTube title: " + youtubeTitle);
                }
            } catch (Exception e) {
                System.out.println("Error searching on Spotify: " + e);
            }
        }
    
        return trackInfoList;
    }
    

    private String[] parseTitle(String title) {
        // Remove anything inside parentheses
        title = title.replaceAll("\\([^)]*\\)", "");
    
        // Split by "feat." or "-"
        String[] parts = title.split("( feat\\.| - )");
    
        if (parts.length == 2) {
            // Trim leading and trailing whitespaces
            parts[0] = parts[0].trim();
            parts[1] = parts[1].trim();
            return parts;
        } else {
            // Return the entire title as track name if parsing is unsuccessful
            return new String[]{title.trim()};
        }
    }

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
    
    
    private boolean isDuplicate(List<TrackInfo> trackInfoList, TrackInfo newTrack) {
        for (TrackInfo existingTrack : trackInfoList) {
            if (existingTrack.getTitle().equalsIgnoreCase(newTrack.getTitle()) &&
                existingTrack.getArtist().equalsIgnoreCase(newTrack.getArtist())) {
                return true;
            }
        }
        return false;
    }
    
    

    /**
     * Klass för en låt på spotify.
    */

    private static class TrackInfo {
        private String title;
        private String artist;
        private String imageUrl;
        private String album;
        private String uri;
    
        public String getTitle() {
            return title;
        }
    
        public String getArtist() {
            return artist;
        }
    
        public String getImageUrl() {
            return imageUrl;
        }
    
        public String getAlbum() {
            return album;
        }
    
        public String getUri() {
            return uri;
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
}
