package org.example;

public class VideoInfo {
    private String videoTitle;
    private String channelName;

    public VideoInfo(String videoTitle, String channelName) {
        this.videoTitle = videoTitle;
        this.channelName = channelName;
    }

    public String getVideoTitle() {
        return videoTitle;
    }


    public String getChannelName() {
        return channelName;
    }
}
