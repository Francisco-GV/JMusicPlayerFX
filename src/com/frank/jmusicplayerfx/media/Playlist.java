package com.frank.jmusicplayerfx.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist {
    private String name;
    private List<AudioFile> audioFiles;

    public Playlist(String name) {
        this(name, null);
    }

    public Playlist(String name, List<AudioFile> audioFiles) {
        this.name = name;
        this.audioFiles = audioFiles;
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

    public AudioFile remove(int index) {
        return audioFiles.remove(index);
    }

    public void add(int index, AudioFile audioFile) {
        audioFiles.add(index, audioFile);
    }

    public Playlist getShufflePlaylist() {
        List<AudioFile> shuffleList = new ArrayList<>(audioFiles);
        Collections.shuffle(shuffleList);
        return new Playlist("Shuffle", shuffleList);
    }

    @Override
    public String toString() {
        return name + ": " + audioFiles;
    }
}