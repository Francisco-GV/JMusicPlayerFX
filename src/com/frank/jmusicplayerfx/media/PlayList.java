package com.frank.jmusicplayerfx.media;

import java.util.List;

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

    public AudioFile get(int index) {
        return audioFiles.get(index);
    }

    public int indexOf(AudioFile audioFile) {
        return audioFiles.indexOf(audioFile);
    }

    public List<AudioFile> getAudioFiles() {
        return audioFiles;
    }
}