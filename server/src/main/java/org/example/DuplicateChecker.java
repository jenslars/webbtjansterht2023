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
            if (existingTrack.getTitle().equalsIgnoreCase(newTrack.getTitle()) &&
                    existingTrack.getArtist().equalsIgnoreCase(newTrack.getArtist()) &&
                    existingTrack.getAlbum().equalsIgnoreCase(newTrack.getAlbum()) &&
                    existingTrack.getSpotifyUri().equalsIgnoreCase(newTrack.getSpotifyUri())) {
                System.out.println("Exact duplicate found");
                return true;
            }
        }
        return false;
    }
}
