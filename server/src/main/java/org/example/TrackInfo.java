package org.example;

/**
 * Klass för en låt på spotify.
 */

public class TrackInfo {

    public TrackInfo(String title, String artist){
        this.title=title;
        this.artist=artist;
    }
    private String title;
    private String artist;
    private String imageUrl;
    private String album;
    private String uri;

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAlbum() {
        return album;
    }

    public String getUri() {
        return uri;
    }

    public String toString() {
        return "TrackInfo{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}
