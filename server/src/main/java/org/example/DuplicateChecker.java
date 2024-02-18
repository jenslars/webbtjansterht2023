package org.example;

import java.util.*;

public class DuplicateChecker {

    /**
     * Kontrollerar om en ny låt redan finns i en given lista av låtar.
     *
     * Metoden jämför den nya låten med varje låt i den givna listan av låtar. Om en matchning hittas baserat på
     * låtens titel och artist returneras true för att indikera att en duplicerad låt har hittats. Annars returneras false.
     *
     * @param trackInfoList En lista av TrackInfo-objekt som representerar befintliga låtar.
     * @param newTrack En ny TrackInfo som ska jämföras med befintliga låtar.
     * @return true om den nya låten redan finns i listan av befintliga låtar, annars false.
     */
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


    /**
     * Kontrollerar och tar bort eventuella dubbletter från en lista av låtar.
     *
     * Metoden tar en lista av TrackInfo-objekt som inmatning och kontrollerar varje låt mot de tidigare sett låtarna
     * för att identifiera och ta bort eventuella dubbletter. Den returnerar en ny lista som bara innehåller unika låtar.
     *
     * @param trackInfoList En lista av TrackInfo-objekt som representerar låtar som kan innehålla dubbletter.
     * @return En lista av TrackInfo-objekt utan dubbletter.
     */
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
