package com.frank.jmusicplayerfx.media;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;

public class Playlist {
    private String name;
    private ObservableList<AudioFile> audioFiles;

    public Playlist(String name) {
        this(name, null);
    }

    public Playlist(String name, ObservableList<AudioFile> audioFiles) {
        this.name = name;
        this.audioFiles = audioFiles;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setList(ObservableList<AudioFile> audioFiles) {
        this.audioFiles = audioFiles;
    }

    public AudioFile get(int index) {
        return audioFiles.get(index);
    }

    public int indexOf(AudioFile audioFile) {
        return audioFiles.indexOf(audioFile);
    }

    public ObservableList<AudioFile> getAudioFiles() {
        return audioFiles;
    }

    public AudioFile remove(int index) {
        return audioFiles.remove(index);
    }

    public void add(int index, AudioFile audioFile) {
        audioFiles.add(index, audioFile);
    }

    public Playlist getShufflePlaylist() {
        ObservableList<AudioFile> shuffleList = FXCollections.observableArrayList(audioFiles);
        Collections.shuffle(shuffleList);
        return new Playlist("Shuffle", shuffleList);
    }

    @Override
    public String toString() {
        return name + ": " + audioFiles;
    }
}