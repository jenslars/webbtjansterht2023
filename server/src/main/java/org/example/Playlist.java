package org.example;

/**
 * Representerar en Spotify-spellista.
 */
public class Playlist {
    private String name; // Namnet på spellistan
    private String id; // ID:t för spellistan
    private String imageUrl; // URL till spellistans bild
    private int trackCount; // Antal låtar i spellistan


    /**
     * Skapar en ny instans av Playlist med angiven information.
     *
     * @param name Namnet på spellistan
     * @param id ID:t för spellistan
     * @param imageUrl URL till spellistans bild
     * @param trackCount Antal låtar i spellistan
     */
    public Playlist(String name, String id, String imageUrl, int trackCount) {
        this.name = name;
        this.id = id;
        this.imageUrl = imageUrl;
        this.trackCount = trackCount;
    }
    /**
     * Returnerar namnet på spellistan.
     *
     * @return Namnet på spellistan
     */
    public String getName() {
        return name;
    }
    /**
     * Returnerar ID:t för spellistan.
     *
     * @return ID:t för spellistan
     */
    public String getId() {
        return id;
    }
    /**
     * Returnerar URL till spellistans bild.
     *
     * @return URL till spellistans bild
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     * Returnerar antalet låtar i spellistan.
     *
     * @return Antal låtar i spellistan
     */
    public int getTrackCount() {
        return trackCount;
    }
}
