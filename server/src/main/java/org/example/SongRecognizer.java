package org.example;

import com.acrcloud.utils.ACRCloudRecognizer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SongRecognizer {


    /**
     * Recognizes a song using the ACRCloud audio recognition service and returns a search query based on the recognized song's title and artist.
     *
     * @param downloadedAudioPath The path to the downloaded audio file that you want to recognize.
     * @return A search query for the recognized song in the format "track:[Song Title] artist:[Artist Name]" or "No song recognized" if no song was recognized.
     */

    public List<TrackInfo> recognizeSongs(String downloadedAudioPath) {
        System.out.println("Recognize songs called");
        ACRCloudRecognizer recognizer;
        Map<String, Object> config = new HashMap<>();
        config.put("host", "identify-eu-west-1.acrcloud.com");
        config.put("access_key", "4ee095c457208ad88f1a28a5b14b9f87");
        config.put("access_secret", "6O8dg2TJ95w0QSqODexAwrJqm0VaDvHzF5aCl8BE");
        config.put("timeout", 100); // Adjust the timeout as needed
        recognizer = new ACRCloudRecognizer(config);

        List<TrackInfo> tracks = new ArrayList<>();

        String result = recognizer.recognizeByFile(downloadedAudioPath, 0);
        System.out.println("ACRCloud Response: " + result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        if (jsonResult.getAsJsonObject("status") != null) {
            int statusCode = jsonResult.getAsJsonObject("status").get("code").getAsInt();
            String statusMsg = jsonResult.getAsJsonObject("status").get("msg").getAsString();
            System.out.println("Status Code: " + statusCode + ", Message: " + statusMsg);

            if (statusCode == 0 && jsonResult.getAsJsonObject("metadata") != null) {
                JsonArray musicArray = jsonResult.getAsJsonObject("metadata").getAsJsonArray("music");
                for (JsonElement musicElement : musicArray) {
                    JsonObject musicInfo = musicElement.getAsJsonObject();
                    String title = musicInfo.get("title").getAsString();
                    StringBuilder artistNames = new StringBuilder();
                    JsonArray artists = musicInfo.getAsJsonArray("artists");
                    for (JsonElement artistElement : artists) {
                        JsonObject artistInfo = artistElement.getAsJsonObject();
                        if (artistNames.length() > 0) {
                            artistNames.append(", ");
                        }
                        artistNames.append(artistInfo.get("name").getAsString());
                    }
                    tracks.add(new TrackInfo(title, artistNames.toString()));
                    System.out.println(tracks.get(0));
                }
            } else {
                System.out.println("No song recognized or error occurred. Message: " + statusMsg);
            }
        } else {
            System.out.println("Response from ACRCloud did not contain a status object.");
        }

        return tracks;
    }


    public static void listFormats(String youtubeUrl) {
        ProcessBuilder listBuilder = new ProcessBuilder(
                "yt-dlp",
                "-F",
                youtubeUrl);
        listBuilder.redirectErrorStream(true);

        try {
            Process process = listBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Available formats:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Error listing formats, exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Downloads audio from a given YouTube URL and saves it as an audio file to the specified output path.
     *
     * @param youtubeUrl The YouTube URL from which to download the audio.
     * @param outputPath The path where the downloaded audio will be saved, including the desired filename.
     * @return The path to the downloaded MP3 audio file if the download is successful, or null if there's an error.
     */

    private String executeDownloadProcess(List<String> command, String outputPath) {
        ProcessBuilder downloadBuilder = new ProcessBuilder(command);
        downloadBuilder.redirectErrorStream(true); // Merge standard output and error stream

        try {
            Process process = downloadBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Print command line output for monitoring
            }
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Download successful: " + outputPath);
                return outputPath; // Successful download path
            } else {
                System.out.println("Error downloading audio, exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Download failed: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Return null on failure
    }

    public String downloadAudio(String youtubeUrl, String outputPath) {
        String startTime = extractTimestamp(youtubeUrl); // Extract the timestamp from the URL
        int startTimeInt = Integer.parseInt(startTime);
        int endTime = startTimeInt+5;
        System.out.println(startTime);
        // Build the command with conditional inclusion of the timestamp
        List<String> command = Arrays.asList(
                "yt-dlp",
                "-x", // Extract audio
                "--force-overwrite", // Overwrite files if they exist
                "--download-sections" ,"*"+startTime"-", // Apply timestamp if exists
                "-S +size",
                "-v",
                "-o", outputPath, // Set output path
                youtubeUrl // YouTube video URL
        );


        // Remove empty arguments which might have been added for optional parameters
        //command.removeAll(Arrays.asList("", null));

        // Start the download process
        return executeDownloadProcess(command, outputPath);
    }

    private String extractTimestamp(String youtubeUrl) {
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

