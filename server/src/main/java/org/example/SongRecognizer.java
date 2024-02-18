package org.example;

import com.acrcloud.utils.ACRCloudRecognizer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
public class SongRecognizer {
    private final AtomicInteger fileCounter;

    public SongRecognizer() {
        this.fileCounter = new AtomicInteger(1);
    }

    /**
     * Recognizes a song using the ACRCloud audio recognition service and returns a search query based on the recognized song's title and artist.
     *
     * @param downloadedAudioPath The path to the downloaded audio file that you want to recognize.
     * @return A search query for the recognized song in the format "track:[Song Title] artist:[Artist Name]" or "No song recognized" if no song was recognized.
     */

    public List<TrackInfo> recognizeSongs(String downloadedAudioPath, double starttime) {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "identify-eu-west-1.acrcloud.com");
        config.put("access_key", "e456d40680a67ea2b49ebc1dc9d074ab");
        config.put("access_secret", "VZcI8Hd0r0Xfnu5rRDR52M4ZYfDMKABQzq9YRDy5");
        config.put("timeout", 10);

        ACRCloudRecognizer recognizer = new ACRCloudRecognizer(config);
        System.out.println("Starting song recognition at: " + starttime + " seconds");
        String result = recognizer.recognizeByFile(downloadedAudioPath, (int) starttime);


        List<TrackInfo> tracks = new ArrayList<>();
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();

        if (jsonResult.has("status") && jsonResult.getAsJsonObject("status").get("code").getAsInt() == 0) {
            JsonArray musicArray = jsonResult.getAsJsonObject("metadata").getAsJsonArray("music");
            for (JsonElement element : musicArray) {
                JsonObject musicInfo = element.getAsJsonObject();
                String title = musicInfo.get("title").getAsString();
                String artistNames = parseArtists(musicInfo.getAsJsonArray("artists"));
                String albumName = parseAlbum(musicInfo.getAsJsonObject("album"));
                double durationsec = (musicInfo.get("duration_ms")).getAsInt() / 1000.0;

                TrackInfo track = new TrackInfo(title, artistNames);
                track.setSongDuration((int) durationsec);
                track.setAlbum(albumName);
                setSpotifyURI(track, musicInfo);

                // Check for duplicates before adding
                if (!tracks.contains(track)) {
                    tracks.add(track);
                    System.out.printf("Song identified: Title='%s', Artist='%s', Album='%s', Duration='%.2f', Spotify URI='%s'%n", title, artistNames, albumName, durationsec, track.getSpotifyUri());
                    break; // Remove this to retrieve all songs identified from one identification
                } else {
                    System.out.printf("Song identified but not added cause of duplicate: Title='%s', Artist='%s', Album='%s', Duration='%.2f', Spotify URI='%s'%n", title, artistNames, albumName, durationsec, track.getSpotifyUri());
                }
            }
        } else {
            System.out.println("No songs recognized or an error occurred.");
        }
        return tracks;
    }


    /**
     * Returnerar en kommaseparerad sträng av artistnamn från en JSON-Array av artister.
     *
     *
     * @param artists En JSON-Array av artister som ska parsas.
     * @return En kommaseparerad sträng av artistnamn.
     */


    public String parseArtists(JsonArray artists) {
        List<String> artistNames = new ArrayList<>();
        for (JsonElement artistElement : artists) {
            JsonObject artistObject = artistElement.getAsJsonObject();
            String artistName = artistObject.get("name").getAsString();
            artistNames.add(artistName);
        }
        return String.join(", ", artistNames);
    }
    /**
     * Returnerar namnet på ett album från en JsonObject som representerar albuminformation.
     *
     * @param album JsonObject som representerar albuminformation.
     * @return Namnet på albumet om det finns, annars "Unknown Album".
     */

    public String parseAlbum(JsonObject album) {
        return album != null && album.has("name") ? album.get("name").getAsString() : "Unknown Album";
    }


    /**
     * Sätter Spotify-URI:en för en låt baserat på information från en JsonObject som representerar musikinformation.
     *
     * @param track En TrackInfo-objekt som representerar låten som Spotify-URI:en ska sättas för.
     * @param musicInfo En JsonObject som representerar musikinformation, inklusive Spotify-metadata.
     */

    private void setSpotifyURI(TrackInfo track, JsonObject musicInfo) {
        if (musicInfo.has("external_metadata") && musicInfo.getAsJsonObject("external_metadata").has("spotify")) {
            JsonObject spotifyInfo = musicInfo.getAsJsonObject("external_metadata").getAsJsonObject("spotify");
            if (spotifyInfo.has("track") && spotifyInfo.getAsJsonObject("track").has("id")) {
                String spotifyURI = "spotify:track:" + spotifyInfo.getAsJsonObject("track").get("id").getAsString();
                track.setSpotifyURI(spotifyURI);
            }
        }
    }


    /**
     * Utför en given kommandoradsprocess och fångar upp- och utdata.
     *
     * @param command En lista med kommandon som ska utföras av processen.
     * @param outputPath Sökvägen för utdata från processen.
     * @return Sökvägen för utdata om processen lyckades, annars returneras null.
     */

    private String executeProcess(List<String> command, String outputPath) {
        ProcessBuilder downloadBuilder = new ProcessBuilder(command);
        downloadBuilder.redirectErrorStream(true);

        try {
            Process process = downloadBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Download/trimming successful: " + outputPath);
                return outputPath;
            } else {
                System.out.println("Error downloading/trimming audio, exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Downloading/trimming failed: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Identifierar låtar från en YouTube-video.
     *
     * Metoden tar en YouTube-video-URL och en starttid som inmatning.
     * Den laddar ner ljudet från YouTube-videon och identifierar sedan låtar från ljudet.
     * Om en starttid ges, identifierar den bara låtar från den angivna starttiden.
     * Metoden returnerar en lista av TrackInfo-objekt som representerar de identifierade låtarna.
     *
     * @param youtubeUrl URL till YouTube-videon som ska identifieras.
     * @param startTime Starttid för identifiering av låtar från videon. Om negativ används hela videon.
     * @return En lista av TrackInfo-objekt som representerar de identifierade låtarna från YouTube-videon.
     */
    public List<TrackInfo> identifyYouTubeVideo(String youtubeUrl, int startTime) {
        String uniqueFileName = "downloaded_audioOneSong-" + UUID.randomUUID().toString() + ".m4a";
        String outputPath = "resources/" + uniqueFileName;
        String downloadAudioPath = downloadAudio(youtubeUrl, outputPath);
        List<TrackInfo> tracks;

        if (startTime >= 0) {
            System.out.println("Timestamp start:" + startTime);
            return recognizeSongs(downloadAudioPath, startTime);
        }

        tracks = recognizeSongs(downloadAudioPath, startTime);

        return tracks;
    }




    /**
     * Identifierar alla låtar i en YouTube-video och returnerar en lista med TrackInfo-objekt.
     *
     * @param youtubeUrl       URL:en för YouTube-videoklippet.
     * @param totalAudioLength Total ljudlängd i videon.
     * @return En lista med TrackInfo-objekt som representerar de identifierade låtarna.
     */
    public List<TrackInfo> identifyAllSongsInYTVideo(String youtubeUrl, double totalAudioLength) {

        String uniqueFileName = "downloaded_audioIdentifyAllsongs-" + UUID.randomUUID().toString() + ".m4a";
        String outputPath = "resources/" + uniqueFileName;
        String downloadAudioPath = downloadAudio(youtubeUrl, outputPath);

        List<TrackInfo> tracks = new ArrayList<>();
        double startTime = 0;
        final double buffer = 5;

        while (startTime < totalAudioLength) {
            List<TrackInfo> identifiedTracks = recognizeSongs(downloadAudioPath, startTime);
            boolean newTrackAdded = false;

            for (TrackInfo identifiedTrack : identifiedTracks) {
                if (!tracks.contains(identifiedTrack)) {
                    if (!DuplicateChecker.isDuplicate(tracks, identifiedTrack)) {
                        tracks.add(identifiedTrack);
                        System.out.println("Song added to final tracklist: " + identifiedTrack);
                        newTrackAdded = true;
                    }
                }
            }

            if (newTrackAdded && !identifiedTracks.isEmpty()) {
                TrackInfo firstIdentifiedTrack = identifiedTracks.get(0);
                startTime += firstIdentifiedTrack.getSongDuration() + buffer;
            } else {
                startTime += buffer;
            }
        }

        tracks = DuplicateChecker.checkDuplicates(tracks);
        System.out.println("Final tracks: ");
        for (TrackInfo track : tracks) {
            System.out.println(track);
        }

        return tracks;
    }


    /**
     * Laddar ner ljudet från en YouTube-video till en lokal fil.
     *
     * @param youtubeUrl   URL:en för YouTube-videoklippet.
     * @param outputPath   Sökväg till den lokala filen där ljudet ska sparas.
     * @return Sökvägen till den nerladdade ljudfilen om nedladdningen lyckades, annars null.
     */
    public String downloadAudio(String youtubeUrl, String outputPath) {
        System.out.println("download audio called");


        List<String> downloadCommand = Arrays.asList("yt-dlp", "-f", "m4a",
                "-S", "+size",
                "--extract-audio", "--audio-format", "m4a", "--force-overwrite",
                "-o", outputPath,
                youtubeUrl
        );


        String downloadOutPutpath = executeProcess(downloadCommand, outputPath);
        if (downloadOutPutpath == null) {
            System.out.println("Download failed.");
            return null;
        }

        return downloadOutPutpath;
    }

    /**
     * Laddar ner flera videor från en spellista på YouTube parallellt.
     *
     * @param videoUrls   En lista med URL:er för videoklippen i spellistan.
     * @return En lista med sökvägar till de nerladdade ljudfilerna om nedladdningen lyckades.
     */
    public List<String> downloadPlaylistVideosInParallel(List<String> videoUrls) {

        int numberOfThreads = 5;
        String outputDirectory = "resources/Playlistfiles";
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<String> outputPaths = new ArrayList<>();


        for (String videoUrl : videoUrls) {
            executorService.submit(() -> {
                String uniqueFileName = String.format("%s/downloaded_audio_%s.m4a", outputDirectory, UUID.randomUUID().toString());

                List<String> downloadCommand = Arrays.asList("yt-dlp",
                        "-f",
                        "bestaudio",
                        "--extract-audio",
                        "--audio-format",
                        "m4a",
                        "--force-overwrite",
                        "-o", uniqueFileName, videoUrl);


                String outPath = executeProcess(downloadCommand, uniqueFileName);
                synchronized (outputPaths) {
                    outputPaths.add(outPath);
                }

            });
        }

        executorService.shutdown();
        try {

            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("Downloads interrupted: " + e.getMessage());
        }
        return outputPaths;
    }


}


