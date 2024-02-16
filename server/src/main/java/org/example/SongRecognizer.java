package org.example;

import com.acrcloud.utils.ACRCloudRecognizer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SongRecognizer {


    /**
     * Recognizes a song using the ACRCloud audio recognition service and returns a search query based on the recognized song's title and artist.
     *
     * @param downloadedAudioPath The path to the downloaded audio file that you want to recognize.
     * @return A search query for the recognized song in the format "track:[Song Title] artist:[Artist Name]" or "No song recognized" if no song was recognized.
     */

    public List<TrackInfo> recognizeSongs(String downloadedAudioPath, double starttime) {
        //System.out.println("Recognizing songs from: " + downloadedAudioPath);
        // Configuration for ACRCloudRecognizer
        Map<String, Object> config = new HashMap<>();
        config.put("host", "identify-eu-west-1.acrcloud.com");
        config.put("access_key", "4489b7284fdeaba761df5b03c63faa14");
        config.put("access_secret", "e4mp893fsRRm5a2vyILg54XeKk0njWAiBtP9cWaV");
        config.put("timeout", 10);

        ACRCloudRecognizer recognizer = new ACRCloudRecognizer(config);
        System.out.println("Starting song recognition at: " + starttime + " seconds");
        String result = recognizer.recognizeByFile(downloadedAudioPath, (int) starttime);
        // System.out.println("ACRCloud Response: " + result);

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
                    System.out.printf("Song identified: Title='%s', Artist='%s', Album='%s', Duration='%.2f', Spotify URI='%s'%n",
                            title, artistNames, albumName, durationsec, track.getSpotifyUri());
                } else {
                    System.out.printf("Song identified but not added cause of duplicate: Title='%s', Artist='%s', Album='%s', Duration='%.2f', Spotify URI='%s'%n",
                            title, artistNames, albumName, durationsec, track.getSpotifyUri());
                }
            }
        } else {
            System.out.println("No songs recognized or an error occurred.");
        }


        return tracks;
    }


// Other methods like parseArtists, parseAlbum, setSpotifyURI, and isExactDuplicate remain unchanged.


    private String parseArtists(JsonArray artists) {
        List<String> artistNames = new ArrayList<>();
        for (JsonElement artistElement : artists) {
            JsonObject artistObject = artistElement.getAsJsonObject();
            String artistName = artistObject.get("name").getAsString();
            artistNames.add(artistName);
        }
        return String.join(", ", artistNames);
    }

    private String parseAlbum(JsonObject album) {
        return album != null && album.has("name") ? album.get("name").getAsString() : "Unknown Album";
    }

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
     * Downloads audio from a given YouTube URL and saves it as an audio file to the specified output path.
     *
     * @param youtubeUrl The YouTube URL from which to download the audio.
     * @param outputPath The path where the downloaded audio will be saved, including the desired filename.
     * @return The path to the downloaded MP3 audio file if the download is successful, or null if there's an error.
     */

    private String executeProcess(List<String> command, String outputPath) {
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
                System.out.println("Download/trimming successful: " + outputPath);
                return outputPath; // Successful download path
            } else {
                System.out.println("Error downloading/trimming audio, exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Downloading/trimming failed: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Return null on failure
    }


    public List<TrackInfo> identifyYouTubeVideo(String youtubeUrl, int startTime) {
        String outputPath = "resources/downloaded_audio.m4a";
        String downloadAudioPath = downloadAudio(youtubeUrl, outputPath);
        List<TrackInfo> tracks;


        if (startTime >= 0) {
            System.out.println("Timestamp start:" + startTime);
            return recognizeSongs(downloadAudioPath, startTime);
        }

        tracks = recognizeSongs(downloadAudioPath, 0);

        return tracks;
    }


    public List<TrackInfo> identifyAllSongsInYTVideo(String youtubeUrl, double totalAudioLength) {
        String outputPath = "resources/downloaded_audio.m4a";
        String downloadAudioPath = downloadAudio(youtubeUrl, outputPath);

        List<TrackInfo> tracks = new ArrayList<>();
        double startTime = 0;
        final double buffer = 5; // Buffer length in seconds to avoid skipping songs


        while (startTime < totalAudioLength) {
            List<TrackInfo> identifiedTracks = recognizeSongs(downloadAudioPath, startTime);
            boolean newTrackAdded = false; // Flag to track if a new track is added


            // Process all identified tracks for adding to the final list
            for (TrackInfo identifiedTrack : identifiedTracks) {
                if (!tracks.contains(identifiedTrack)) {

                    if(!DuplicateChecker.isDuplicate(tracks,identifiedTrack)){
                        tracks.add(identifiedTrack);
                        System.out.println("Song added to final tracklist: " + identifiedTrack);
                        newTrackAdded = true; // Set flag to true since a new track was added
                    }

                }
            }


            if (newTrackAdded && !identifiedTracks.isEmpty()) {
                // Only adjust start time based on the first track if a new track was added
                TrackInfo firstIdentifiedTrack = identifiedTracks.get(0);
                startTime += firstIdentifiedTrack.getSongDuration() + buffer;
            } else {
                // If no new track is added, or no tracks are identified, increment the start time to scan the next segment
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


    public String downloadAudio(String youtubeUrl, String outputPath) {

        // listFormats(youtubeUrl);

        List<String> downloadCommand = Arrays.asList(
                "yt-dlp",
                "-f", "m4a", // Select m4a formats
                "-S", "+size", // Prioritize by file size to get the smallest file
                "--extract-audio",
                "--audio-format", "m4a",
                "--force-overwrite", // Force overwriting existing files
                "-o", outputPath, // Specify the output file path
                youtubeUrl // YouTube video URL
        );


        String downloadOutPutpath = executeProcess(downloadCommand, outputPath);
        if (downloadOutPutpath == null) {
            System.out.println("Download failed.");
            return null;
        }

        return downloadOutPutpath;
    }

    public String identifyYTUrlTimestamp(String startTime, String downloadAudioPath) {

        System.out.println("Starting trimming....");
        int startTimeInt = Integer.parseInt(startTime);
        int endTime = startTimeInt + 30; // Calculate end time for a 10-second clip

        String trimmedOutputPath = downloadAudioPath.replace(".m4a", "_trimmed.m4a");
        System.out.println("trimmed outpath: " + trimmedOutputPath);

        List<String> trimCommand = Arrays.asList(
                "ffmpeg",
                "-y", // Force overwrite without asking
                "-ss", String.valueOf(startTimeInt), // Start time
                "-to", String.valueOf(endTime), // End time, adjust accordingly
                "-i", downloadAudioPath, // Input file
                "-acodec", "copy", // Use the same audio codec to avoid re-encoding
                trimmedOutputPath // Output file
        );

        trimmedOutputPath = executeProcess(trimCommand, trimmedOutputPath);

        if (trimmedOutputPath == null) {
            System.out.println("Trimming failed.");
            return null;
        } else {
            System.out.println("trimming success output file+" + trimmedOutputPath);
        }
        return trimmedOutputPath;
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
}


