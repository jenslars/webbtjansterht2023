package org.example;

import java.util.List;
import java.util.Objects;

/**
 * Representerar information om en låt, inklusive titel, artist, album, Spotify-URI, bild-URL och låtens längd.
 */
public class TrackInfo {


    /**
     * Skapar ett TrackInfo-objekt med angiven titel och artist.
     *
     * @param title  Titeln på låten.
     * @param artist Artisten för låten.
     */
    public TrackInfo(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    /**
     * Sätter längden på låten.
     *
     * @param songDuration Längden på låten i sekunder.
     */
    public void setSongDuration(int songDuration) {
        this.songDuration = (int) songDuration;
    }

    /**
     * Hämtar längden på låten.
     *
     * @return Längden på låten i sekunder.
     */
    public double getSongDuration() {
        return songDuration;
    }

    private String title;
    private String artist;
    private String imageUrl;
    private String album;
    private String uri;

    private int songDuration;

    /**
     * Hämtar titel på låten.
     *
     * @return Titel på låten.
     */

    public String getTitle() {
        return title;
    }
    /**
     * Hämtar artist på låten.
     *
     * @return Artist på låten.
     */
    public String getArtist() {
        return artist;
    }
    /**
     * Sätter albumet för låten.
     *
     * @param album Albumet för låten.
     */
    public void setAlbum(String album) {
        this.album = album;
    }

    /**
     * Sätter Spotify-URI:en för låten.
     *
     * @param spotifyUri Spotify-URI:en för låten.
     */
    public void setSpotifyURI(String spotifyUri) {
        uri = spotifyUri;
    }

    /**
     * Hämtar bild-URL för låten.
     *
     * @return Bild-URL för låten.
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     * Hämtar albumet för låten.
     *
     * @return Albumet för låten.
     */
    public String getAlbum() {
        return album;
    }

    /**
     * Hämtar Spotify-URI:en för låten.
     *
     * @return Spotify-URI:en för låten.
     */
    public String getSpotifyUri() {
        return uri;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackInfo trackInfo = (TrackInfo) o;

        if (this.uri != null && trackInfo.uri != null) {

            if (this.uri.equals(trackInfo.uri)) {
                return true;
            }

        }


        return Objects.equals(this.title, trackInfo.title) &&
                Objects.equals(this.artist, trackInfo.artist);
    }


    @Override
    public int hashCode() {

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
