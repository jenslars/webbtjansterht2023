package org.example;
/**
 * Representerar information om en video, inklusive titeln på videon och kanalens namn.
 */
public class VideoInfo {
    private String videoTitle;
    private String channelName;

    /**
     * Skapar ett VideoInfo-objekt med angiven titel på videon och kanalens namn.
     *
     * @param videoTitle  Titeln på videon.
     * @param channelName Namnet på kanalen.
     */
    public VideoInfo(String videoTitle, String channelName) {
        this.videoTitle = videoTitle;
        this.channelName = channelName;
    }

    /**
     * Hämtar titeln på videon.
     *
     * @return Titeln på videon.
     */
    public String getVideoTitle() {
        return videoTitle;
    }

    /**
     * Hämtar kanalens namn.
     *
     * @return Namnet på kanalen.
     */
    public String getChannelName() {
        return channelName;
    }
}
