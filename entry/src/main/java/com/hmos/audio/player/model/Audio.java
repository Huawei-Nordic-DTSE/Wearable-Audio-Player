package com.hmos.audio.player.model;

public class Audio {

    private String title;
    private String audioFiles;
    private String id;
    private String cover;

    public Audio() {}

    public String getTitle() {
        return title;
    }

    public String getAudioFiles() {
        return audioFiles;
    }

    public String getId() {
        return id;
    }

    public String getCover() {
        return cover;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAudioFiles(String audioFiles) {
        this.audioFiles = audioFiles;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCover(String cover) { this.cover = cover; }
}
