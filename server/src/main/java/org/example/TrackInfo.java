package org.example;

import java.util.List;

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

    private List<String> featuredArtists;


    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setSpotifyURI(String spotifyUri) {
        uri = spotifyUri;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAlbum() {
        return album;
    }

    public String getSpotifyUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "TrackInfo{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", album='" + album + '\'' +
                ", spotifyURI='" + uri + '\'' +
                '}';
    }
}
