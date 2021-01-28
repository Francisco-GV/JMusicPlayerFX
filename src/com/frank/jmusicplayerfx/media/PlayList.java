package com.frank.jmusicplayerfx.media;

import java.util.List;

@SuppressWarnings("unused")
public class PlayList {
    private String name;
    private List<AudioFile> audioFiles;

    public PlayList(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAudioFiles(List<AudioFile> audioFiles) {
        this.audioFiles = audioFiles;
    }

    public List<AudioFile> getAudioFiles() {
        return audioFiles;
    }
}