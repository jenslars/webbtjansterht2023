package org.example;

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
import java.io.UnsupportedEncodingException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class ServerRunner {
    CloseableHttpClient httpClient = null;
    HttpGet httpGet = null;
    HttpPost httpPost = null;
    HttpPut httpPut = null;
    CloseableHttpResponse response = null;
    String accessToken = null;

    /**
     * Main-metod som startar servern och lyssnar efter API-anrop.
     * @param args
     */
    public static void main(String[] args) {
        ServerRunner serverRunner = new ServerRunner();
        serverRunner.requestAccessToken();
        Javalin app = Javalin.create(config -> {})
                //TODO: Här nedanför fyller vi på med fler endpoints när vi vet vilka vi behöver/vill ha.
                .get("/{id}", ctx -> { serverRunner.addSongToPlaylist(ctx); })
                .start(5000);

        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
    }

    private void addSongToPlaylist(Context ctx) {
        String songName = ctx.pathParam("id");
    }

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
}