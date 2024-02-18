package org.example;

import org.apache.http.HttpResponse;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpHeaders;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ServerRunner {

    static String accessToken = null;

    public ServerRunner() {
        requestAccessToken();
    }


    /**
     * Huvudmetod som startar servern och lyssnar efter API-anrop.
     *
     * Denna server hanterar olika ändpunkter för en webbapplikation som låter användare hantera spellistor och låtar.
     * Den inkluderar funktioner för användarinloggning, skapande av spellistor, tillägg av låtar till spellistor,
     * konvertering av spellistor, konvertering av videor till låtar, identifiering av låtar i videor,
     * och tillhandahållande av statiska filer som HTML, JavaScript, CSS och bilder.
     *
     * @param args Kommandoradsargument (används inte).
     */
    public static void main(String[] args) {
        ServerRunner serverRunner = new ServerRunner();


        Javalin app = Javalin.create(config -> {
                    // Tillhandahåller huvudsidan
                }).get("/", ctx -> {
                    String htmlContent = serverRunner.readHtmlFile("static/views/index.html");
                    ctx.html(htmlContent);
                    // Tillhandahåller inloggningssidan
                }).get("/login", ctx -> {
                    String htmlContent = serverRunner.readHtmlFile("static/views/login.html");
                    ctx.html(htmlContent);
                    // Skapar en ny spellista med angivna spårens URI
                }).post("/createPlaylist", ctx -> {
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
                    String playlistId = serverRunner.createPlaylist(trackUris);


                    JsonObject response = new JsonObject();
                    response.addProperty("playlistId", playlistId);


                    ctx.contentType("application/json");


                    ctx.status(200);
                    ctx.result(response.toString());

                    // Lägger till låtar till en befintlig spellista
                }).post("/addTracksToPlaylist", ctx -> {
                    JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    String playlistId = requestBody.get("playlistId").getAsString();
                    JsonArray trackUrisJson = requestBody.getAsJsonArray("trackUris");

                    if (trackUrisJson != null) {
                        List<String> trackUris = new ArrayList<>();
                        for (JsonElement trackUriJson : trackUrisJson) {
                            trackUris.add(trackUriJson.getAsString());
                        }

                        serverRunner.addTracksToPlaylist(playlistId, trackUris);


                        JsonObject response = new JsonObject();
                        response.addProperty("status", "success");
                        response.addProperty("message", "Tracks added to playlist successfully.");
                        ctx.status(200);
                        ctx.json(response.toString());
                    } else {

                        JsonObject response = new JsonObject();
                        response.addProperty("status", "error");
                        response.addProperty("message", "Invalid request data.");
                        ctx.status(400);
                        ctx.json(response.toString());
                    }
                })
                // Hämtar användarens spellistor
                .get("/getUserPlaylists", ctx -> {
                    List<Playlist> playlists = serverRunner.fetchUserPlaylists();
                    if(playlists.isEmpty()){
                        ctx.status(400);
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.addProperty("message", "No playlist found");
                        ctx.status(400).json(jsonResponse.toString());
                    }

                    ctx.status(200);
                    ctx.json(playlists);
                })
                // Översätter youtube spellista till Spotify spellista
                .get("/convertPlaylist", ctx -> {
                    Gson gson = new Gson();
                    String url = ctx.queryParam("url");

                    List<TrackInfo> trackInfoList = serverRunner.convertPlayList(url);
                    if(trackInfoList == null){
                        JsonObject jsonResponse = new JsonObject();
                        ctx.status(400);
                        jsonResponse.addProperty("message", "No songs were found");
                    }
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Playlist converted successfully");
                    jsonResponse.add("tracks", gson.toJsonTree(trackInfoList));
                    ctx.json(jsonResponse.toString());
                })
                // Översätter youtube video till spotify tracks
                .get("/convertVideo", ctx -> {
                    Gson gson = new Gson();
                    System.out.println("convertvideo get called from frontend");
                    String url = ctx.queryParam("url");
                    System.out.println("url: " + url);


                    List<TrackInfo> trackInfoList = serverRunner.convertVideo(url);
                    if(!trackInfoList.isEmpty()){
                        System.out.println(trackInfoList);
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.addProperty("message", "Video converted successfully");
                        jsonResponse.add("tracks", gson.toJsonTree(trackInfoList));
                        ctx.status(200).json(jsonResponse.toString());
                    }else {
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.addProperty("message", "No tracks found or conversion failed");
                        ctx.status(400).json(jsonResponse.toString());
                    }

                // Översätter youtube video till spotify tracks
                }).get("/identifyAllSongsInVideo", ctx -> {
                    System.out.println("identifyAllSongsInVideo get called from frontend");

                    String url = ctx.queryParam("url");

                    List<TrackInfo> trackInfoList = serverRunner.identifyAllSongsInVideo(url);
                    System.out.println(trackInfoList.size());

                    if(trackInfoList.isEmpty()){
                        JsonObject jsonResponse = new JsonObject();
                        ctx.status(400);
                        jsonResponse.addProperty("message", "No songs were found");
                        ctx.json(jsonResponse.toString());
                    }else {
                        Gson gson = new Gson();
                        ctx.status(200);
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.addProperty("message", "Video converted successfully");
                        jsonResponse.add("tracks", gson.toJsonTree(trackInfoList));
                        ctx.json(jsonResponse.toString());
                    }
                // Används för att ta emot en 'code' som sedan kan bytas mot en access-token.
                }).get("/callback", ctx -> {
                    String code = ctx.queryParam("code");
                    serverRunner.exchangeCodeForAccessToken(code);
                    String htmlContent = serverRunner.readHtmlFile("static/views/redirect.html");
                    ctx.html(htmlContent);
                // Serverar JavaScript-filer. Filnamnet specificeras i URL:en och filen hämtas från en fördefinierad mapp.

                }).get("/scripts/{filename}", ctx -> {
                    serverRunner.serveJavaScriptFile(ctx);

              // Serverar CSS-filer. Liksom med JavaScript, specificeras filnamnet i URL:en.
                }).get("/static/styling/{filename}", ctx -> {
                    serverRunner.serveCssFile(ctx);
                });
        // Serverar SVG-bilder. Filnamnet specificeras i URL:en.
        app.get("/images/svg/{filename}", ctx -> {
            serverRunner.serveSvgFile(ctx);
        });
        // Serverar PNG-bilder. Även här specificeras filnamnet i URL:en.
        app.get("/images/png/{filename}", ctx -> {
            serverRunner.servePngFile(ctx);
        }).get("/{id}", ctx -> {

        })// Startar servern på port 5000.
                .start(5000);

        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
    }

    /**
     * Hämtar användarens spellistor från Spotify.
     *
     * Denna metod anropar Spotify's Web API för att hämta en lista över den inloggade användarens spellistor.
     * För att genomföra detta anrop krävs en giltig access-token, som används för att autentisera förfrågan mot Spotify's API.
     * Metoden filtrerar och returnerar endast de spellistor där den inloggade användaren är ägaren.
     *

     * Om förfrågan misslyckas eller om ett undantag inträffar under processen, skrivs ett felmeddelande ut och en tom lista returneras.
     *
     * @return En lista av Playlist-objekt som representerar användarens spellistor på Spotify.
     *         Om inga spellistor hittas eller om ett fel uppstår returneras en tom lista.
     */

    private List<Playlist> fetchUserPlaylists() {
        System.out.println("fetch user playlist called");
        try {
            String userId = getUserId(accessToken);
            String url = "https://api.spotify.com/v1/me/playlists";
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            CloseableHttpResponse response;

            CloseableHttpClient httpClient = HttpClients.createDefault();
                response = httpClient.execute(httpGet);


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
     * Konverterar en video från en given URL till en lista av TrackInfo-objekt.
     *
     * Metoden hanterar processen att identifiera låtar från en video, främst genom att använda YouTube och ACRCloud för låtigenkänning,
     * samt att söka efter motsvarande låtar på Spotify. Den stödjer både direkta tidsstämplar i videons URL för specifika segment
     * och enskild analys om ingen tidstämpel ges.
     *

     * @param url URL till videon som ska analyseras.
     * @return En lista av TrackInfo-objekt som representerar identifierade och på Spotify matchade låtar från videon.
     *         Returnerar en tom lista om inga låtar kan identifieras eller om ett fel uppstår.
     */

    private List<TrackInfo> convertVideo(String url) {
        Gson gson = new Gson();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        SpotifySongSearcher spotifySongSearcher = new SpotifySongSearcher(accessToken,gson);
        SongRecognizer songRecognizer = new SongRecognizer();
        YouTubeVideoInfoExtractor youTubeVideoInfoExtractor = new YouTubeVideoInfoExtractor();
        System.out.println(" convertvideo called");
        System.out.println("Received URL: " + url);
        List<TrackInfo> tracks = new ArrayList<>();
        VideoInfo videoInfo = null;


        String startTime = youTubeVideoInfoExtractor.extractTimestamp(url); // Extract the timestamp from the URL

        if (!Objects.equals(startTime, "")) {
            System.out.println("Timestamp used");
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
        }
        return tracks;
    }

    /**
     * Identifierar låtar från en given videolänk och returnerar en lista av TrackInfo-objekt.
     *
     * Denna metod tar en URL till en video som inmatning och använder YouTube och ACRCloud för att identifiera låtar
     * alla som förekommer i videon. Den söker sedan efter motsvarande låtar på Spotify och returnerar en lista av TrackInfo-objekt
     * som representerar de identifierade låtarna.
     *
     * @param url URL till videon som ska analyseras.
     * @return En lista av TrackInfo-objekt som representerar identifierade och matchade låtar från videon på Spotify.
     *         Returnerar en tom lista om inga låtar kan identifieras eller om ett fel uppstår.
     */
    private List<TrackInfo> identifyAllSongsInVideo(String url) {
        Gson gson = new Gson();
        SongRecognizer songRecognizer = new SongRecognizer();
        YouTubeVideoInfoExtractor youTubeVideoInfoExtractor = new YouTubeVideoInfoExtractor();
        SpotifySongSearcher spotifySongSearcher = new SpotifySongSearcher(accessToken,gson);
        System.out.println(" identifyAllSongsInVideo called");
        System.out.println("Received URL: " + url);
        String videoId = extractIdMultipleSongs(url);
        List<TrackInfo> tracks = new ArrayList<>();

        if (videoId != null && !videoId.isEmpty()) {
            System.out.println("youtube video id:" + videoId);

            String ISOduration = youTubeVideoInfoExtractor.fetchVideoDuration(url);

            if (ISOduration == null) {
                return tracks;
            }

            Duration duration = Duration.parse(ISOduration);
            System.out.println(ISOduration);
            System.out.println("video duration in seconds: " + duration.getSeconds());


            tracks = songRecognizer.identifyAllSongsInYTVideo(url, (double) duration.getSeconds());

            if (tracks != null && !tracks.isEmpty()) {
                tracks = spotifySongSearcher.searchSongsOnSpotify(tracks);
                System.out.println("Number of songs added to array and being sent to frontend: " + tracks.size());
                tracks = DuplicateChecker.checkDuplicates(tracks);
                return tracks;

            }
        }
        return tracks;
    }

    /**
     * Extraherar videoidentifikatorn från en YouTube-videolänk.
     *
     * Metoden tar en URL till en video som inmatning och försöker extrahera videoidentifikatorn från den. Om URL:en
     * är en giltig YouTube-länk (antingen youtube.com eller youtu.be) och innehåller ett videoidentifikationsparametrar,
     * extraherar metoden videoid:et och returnerar det. Annars returneras strängen "ID not found".
     *
     * @param url URL till videon vars videoidentifikator ska extraheras.
     * @return En sträng som representerar videoidentifikatorn, om den finns i URL:en. Returnerar "ID not found" om
     *         videoidentifikatorn inte kunde extraheras eller om det uppstod ett fel under bearbetningen.
     */
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

    /**
     * Konverterar en YouTube-spellista till en lista av TrackInfo-objekt.
     *
     * Metoden tar en URL till en YouTube-spellista som inmatning och bearbetar spellistan för att identifiera
     * låtar från videor i spellistan. Först extraherar den spellistans ID från URL:en och kontrollerar dess giltighet.
     * Därefter använder den YouTube API för att hämta videoidentifierare för videorna i spellistan och skapar en lista
     * med videolänkar. Efter att ha hämtat videorna från spellistan laddar den ner videorna och identifierar låtar från
     * dem. Sedan söker den efter låtarna på Spotify och returnerar en lista av TrackInfo-objekt som representerar
     * de identifierade och matchade låtarna från spellistan.
     *
     * @param url URL till YouTube-spellistan som ska konverteras.
     * @return En lista av TrackInfo-objekt som representerar identifierade och matchade låtar från spellistan på Spotify.
     *         Returnerar null om URL:en till spellistan är ogiltig eller om det uppstår fel under bearbetningen.
     */
    private List<TrackInfo> convertPlayList(String url) {
        Gson gson = new Gson();
        YouTubeVideoInfoExtractor youTubeVideoInfoExtractor = new YouTubeVideoInfoExtractor();
        String playlistId = youTubeVideoInfoExtractor.extractPlaylistId(url);
        SpotifySongSearcher spotifySongSearcher = new SpotifySongSearcher(accessToken,gson);
        if (playlistId == null) {
            System.out.println("Invalid YouTube playlist URL");
            return null;
        }

        SongRecognizer songRecognizer = new SongRecognizer();

        String apiKey = "AIzaSyDN60vbLZ6CNekmYd7WP_r8C96unRI4CaY";
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
            trackInfoList.addAll(songRecognizer.recognizeSongs(outputPaths.get(i), 5));
        }


        List<TrackInfo> finalList = spotifySongSearcher.searchSongsOnSpotify(trackInfoList);
        finalList = DuplicateChecker.checkDuplicates(finalList);

        for (int i = 0; i < finalList.size(); i++) {
            System.out.println(finalList.get(i));
        }

        return finalList;
    }


    /**
     * Skapar en ny spellista på användarens Spotify-konto och lägger till låtar till den.
     *
     * Metoden skapar en ny spellista på användarens Spotify-konto med en given lista av låt-URIs.
     * Den skickar en HTTP POST-förfrågan till Spotify API:et för att skapa spellistan med de angivna detaljerna.
     * Därefter lägger den till låtarna till den skapade spellistan och returnerar dess ID.
     *
     * @param trackUris En lista av låt-URIs som ska läggas till i den nya spellistan.
     * @return Spellistans ID om spellistan skapades och låtarna lades till framgångsrikt, annars returneras null.
     */


    private String createPlaylist(List<String> trackUris) {
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
     * Lägger till låtar till en befintlig spellista på användarens Spotify-konto.
     *
     * Metoden skickar en HTTP POST-förfrågan till Spotify API:et för att lägga till en lista av låtar till en
     * befintlig spellista med det angivna spellistans ID:t. Låtarna specificeras med en lista av låt-URIs.
     *
     * @param playlistId Spellistans ID där låtarna ska läggas till.
     * @param trackUris En lista av låt-URIs som ska läggas till i spellistan.
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
            CloseableHttpClient httpClient = HttpClients.createDefault();
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
            CloseableHttpClient httpClient;
            httpClient = HttpClients.createDefault();

            String clientID = "c32d1829b55d4c5eac178bc34fdd6728";
            String clientSecret = "b9f53919c0774da89f480a8863d5234e";
            String requestBody = "grant_type=client_credentials&client_id=" + clientID + "&client_secret=" + clientSecret;

            HttpPost httpPost = new HttpPost("https://accounts.spotify.com/api/token");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new StringEntity(requestBody));

            try {
                HttpResponse response = httpClient.execute(httpPost);
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(responseBody).getAsJsonObject();
                accessToken = json.get("access_token").getAsString();


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
     * Hämtar användarens Spotify-användar-ID.
     *
     * Metoden skickar en HTTP GET-förfrågan till Spotify API:et för att hämta information om den aktuella användaren.
     * Därefter parsar den svaret för att extrahera användarens Spotify-användar-ID och returnerar det.
     *
     * @param accessToken Åtkomsttoken för att autentisera användaren gentemot Spotify API:et.
     * @return Användarens Spotify-användar-ID om det hämtas framgångsrikt, annars returneras null.
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

    /**
     * Metod för att läsa innehållet i en HTML-fil.
     *
     * @param filePath Sökväg till HTML-filen.
     * @return Innehållet i HTML-filen som en sträng.
     */

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
