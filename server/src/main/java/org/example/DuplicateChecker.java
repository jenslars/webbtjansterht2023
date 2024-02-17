package org.example;

import java.util.*;

public class DuplicateChecker {

    public static boolean isDuplicate(List<TrackInfo> trackInfoList, TrackInfo newTrack) {
        System.out.println("isDuplicate called");
        for (TrackInfo existingTrack : trackInfoList) {
            if (existingTrack.getTitle().equalsIgnoreCase(newTrack.getTitle()) && existingTrack.getArtist().equalsIgnoreCase(newTrack.getArtist())) {
                System.out.println("Duplicate found");
                return true;
            }
        }
        return false;
    }

    public static List<TrackInfo> checkDuplicates(List<TrackInfo> trackInfoList) {
        List<TrackInfo> deduplicatedList = new ArrayList<>();

        for (TrackInfo currentTrack : trackInfoList) {
            boolean isDuplicate = false;
            for (TrackInfo uniqueTrack : deduplicatedList) {
                if (currentTrack.getTitle().equalsIgnoreCase(uniqueTrack.getTitle()) && currentTrack.getArtist().equalsIgnoreCase(uniqueTrack.getArtist())) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                deduplicatedList.add(currentTrack);
            }
        }

        return deduplicatedList;
    }


}
