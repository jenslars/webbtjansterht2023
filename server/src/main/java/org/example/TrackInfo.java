package org.example;

import java.util.List;
import java.util.Objects;

/**
 * Klass för en låt på spotify.
 */

public class TrackInfo {

    public TrackInfo(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public void setSongDuration(int songDuration) {
        this.songDuration = (int) songDuration;
    }

    public double getSongDuration() {
        return songDuration;
    }

    private String title;
    private String artist;
    private String imageUrl;
    private String album;
    private String uri;

    private int songDuration;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackInfo trackInfo = (TrackInfo) o;

        // Check if both URIs are non-null
        if (this.uri != null && trackInfo.uri != null) {
            // If both URIs are non-null and equal, consider tracks equal
            if (this.uri.equals(trackInfo.uri)) {
                return true;
            }
            // If URIs are non-null but different, still check the title and artist
            // This line is reached only if URIs are both non-null and different
        }

        // Either one URI is null, or URIs are different; check title and artist
        return Objects.equals(this.title, trackInfo.title) &&
                Objects.equals(this.artist, trackInfo.artist);
    }


    @Override
    public int hashCode() {
        // Include URI in the hash code computation if it is non-null; otherwise, rely on title and artist
        return (uri != null) ? Objects.hash(uri) : Objects.hash(title, artist);
    }


    @Override
    public String toString() {
        return "TrackInfo{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", uri='" + uri + '\'' +
                ", songDuration=" + songDuration +
                '}';
    }
}
