package org.example;

import com.google.gson.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.acrcloud.utils.ACRCloudRecognizer;

public class ServerRunner {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = null;
    HttpPost httpPost = null;
    HttpPut httpPut = null;
    CloseableHttpResponse response = null;
    String accessToken = null;
    ACRCloudRecognizer recognizer;

    /**
     * Main-metod som startar servern och lyssnar efter API-anrop.
     *
     * @param args
     */
    public static void main(String[] args) {
        ServerRunner serverRunner = new ServerRunner();
        serverRunner.requestAccessToken();
        serverRunner.recognizeSong();
        Javalin app = Javalin.create(config -> {
        })
                .get("/", ctx -> {
                    String htmlContent = serverRunner.readHtmlFile("static/views/index.html");
                    ctx.html(htmlContent);
                })
                .get("/scripts/{filename}", ctx -> {
                    serverRunner.serveJavaScriptFile(ctx);
                })
                .get("/{id}", ctx -> {
                    serverRunner.getSongUrl(ctx);
                })
                .get("/playlist/{id}", ctx -> {
                    serverRunner.getPlaylistID(ctx);
                })
                .post("/{id}", ctx -> {
                    serverRunner.addSongToPlaylist(ctx);
                })
                .start(5000);

        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
    }

    private void recognizeSong() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "identify-eu-west-1.acrcloud.com");
        config.put("protocol", "http");
        config.put("access_key", "4ee095c457208ad88f1a28a5b14b9f87");
        config.put("access_secret", "6O8dg2TJ95w0QSqODexAwrJqm0VaDvHzF5aCl8BE");
        config.put("timeout", 5);
        config.put("rec_type", ACRCloudRecognizer.RecognizerType.AUDIO);
        config.put("debug", false);
        recognizer = new ACRCloudRecognizer(config);

        String songInfo = recognizer.recognizeByFile("resources/StarWars60.wav", 0);
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(songInfo).getAsJsonObject();
        String title = json.getAsJsonObject("metadata").getAsJsonArray("music").get(0).getAsJsonObject().get("title").getAsString();
        System.out.println(title);
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
     * Metod som hämtar Spotify ID:t på den spellista som användaren vill lägga till en låt i.
     *
     * @param ctx
     */
    private void getPlaylistID(Context ctx) {
        // Implementation for getting playlist ID
    }

    /**
     * Metod för att lägga till låten i användarens Spotify-spellista.
     *
     * @param ctx
     */
    private void addSongToPlaylist(Context ctx) {
        // Implementation for adding a song to the playlist
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
            httpPost.setHeader("Content-Type:", "application/x-www-form-urlencoded");
            httpPost.setEntity(new StringEntity(requestBody));

            try {
                response = httpClient.execute(httpPost);
                accessToken = response.getEntity().toString(); //TODO: Klassens instansvariabel accessToken blir reassigned här. Dubbelkolla så att det funkar och om datatyp är giltig.
            } catch (Exception e) {
                System.out.println(e);
            }

        } catch (Exception e) {
            System.out.println(e);;
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
}
