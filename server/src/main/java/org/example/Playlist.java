package org.example;

public  class Playlist {
    private String name;
    private String id;
    private String imageUrl;
    private int trackCount;

    public Playlist(String name, String id, String imageUrl, int trackCount) {
        this.name = name;
        this.id = id;
        this.imageUrl = imageUrl;
        this.trackCount = trackCount;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getTrackCount() {
        return trackCount;
    }
}
