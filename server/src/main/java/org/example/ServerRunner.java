package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

public class ServerRunner {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = null;
    HttpPost httpPost = null;
    HttpPut httpPut = null;
    CloseableHttpResponse response = null;
    String accessToken = null;
    Gson gson = new Gson();

    /**
     * Main-metod som startar servern och lyssnar efter API-anrop.
     * @param args
     */
    public static void main(String[] args) {
        ServerRunner serverRunner = new ServerRunner();
        serverRunner.requestAccessToken();
        Javalin app = Javalin.create(config -> {})
                //TODO: Här nedanför fyller vi på med fler endpoints när vi vet vilka vi behöver/vill ha.
                .get("/{id}", ctx -> { serverRunner.getSongUrl(ctx); })
                .get("/playlist/{id}", ctx -> { serverRunner.getPlaylistID(ctx); })
                .post("/{id}", ctx -> { serverRunner.addSongToPlaylist(ctx); })
                .start(5000);

        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
    }

    /**
     * Metod för att hämta URL till den låt som användaren/Shazam angav.
     * @param ctx
     */
    //TODO: Har inte kollat om denna metod fungerar ännu, så förvänta er inte att något vettigt resultat, om den inte fungerar så är det pga url:en är knasig
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
     * Metod som hämtar Spotify ID:t på den spellista som användaren vill lägga till en låt i.
     * @param ctx
     */
    private void getPlaylistID(Context ctx) {
    }

    /**
     * Metod för att lägga till låten i användarens Spotify-spellista.
     * @param ctx
     */
    private void addSongToPlaylist(Context ctx) {
    }

    /**
     * Metod som skickar en förfrågan till Spotify's API för att få en access token som senare används i alla andra anrop till Spotify's API.
     */
    private void requestAccessToken() {
        try {
            String clientID = "c32d1829b55d4c5eac178bc34fdd6728";
            String clientSecret = "b9f53919c0774da89f480a8863d5234e";
            String credentials = Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes());
            String requestBody = "grant_type=client_credentials";

            httpPost = new HttpPost("https://accounts.spotify.com/api/token");
            httpPost.setHeader("Authorization", "Basic " + credentials);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new StringEntity(requestBody));

            try {
                response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) {
                    JsonObject jsonObject = gson.fromJson(new InputStreamReader(response.getEntity().getContent()), JsonObject.class);
                    accessToken = jsonObject.get("access_token").getAsString();
                }
                else {
                    System.out.println(response.getStatusLine().getReasonPhrase());
                }
            } catch (Exception e) {
                System.out.println(e);
            }

        } catch (Exception e) {
            System.out.println(e);;
        }
    }
}