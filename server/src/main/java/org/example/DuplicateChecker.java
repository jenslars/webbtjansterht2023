package org.example;

import java.util.List;

public class DuplicateChecker {

    public static boolean isDuplicate(List<TrackInfo> trackInfoList, TrackInfo newTrack) {
        System.out.println("isDuplicate called");
        for (TrackInfo existingTrack : trackInfoList) {
            if (existingTrack.getTitle().equalsIgnoreCase(newTrack.getTitle()) &&
                    existingTrack.getArtist().equalsIgnoreCase(newTrack.getArtist())) {
                System.out.println("Duplicate found");
                return true;
            }
        }
        return false;
    }

    public static boolean isExactDuplicate(List<TrackInfo> trackInfoList, TrackInfo newTrack) {
        System.out.println("isExactDuplicate called");
        for (TrackInfo existingTrack : trackInfoList) {
            boolean titleMatch = existingTrack.getTitle().equalsIgnoreCase(newTrack.getTitle());
            boolean artistMatch = existingTrack.getArtist().equalsIgnoreCase(newTrack.getArtist());
            boolean albumMatch = existingTrack.getAlbum().equalsIgnoreCase(newTrack.getAlbum());

            boolean spotifyUriMatch = existingTrack.getSpotifyUri() != null && newTrack.getSpotifyUri() != null
                    && existingTrack.getSpotifyUri().equalsIgnoreCase(newTrack.getSpotifyUri());

            // If Spotify URI for either track is null, skip URI comparison
            boolean isMatch = titleMatch && artistMatch && albumMatch &&
                    (existingTrack.getSpotifyUri() == null || newTrack.getSpotifyUri() == null || spotifyUriMatch);

            if (isMatch) {
                System.out.println("Exact duplicate found");
                return true;
            }
        }
        return false;
    }

}
