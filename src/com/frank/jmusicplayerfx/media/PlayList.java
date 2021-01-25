package com.frank.jmusicplayerfx.media;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class PlayList {
    private String name;
    private final List<AudioFile> audioList;

    public PlayList(String name) {
        this.name = name;
        audioList = new ArrayList<>();
    }

    public void add(AudioFile audioFile) {
        audioList.add(audioFile);
    }

    public void addAll(AudioFile... files) {
        audioList.addAll(Arrays.asList(files));
    }

    public void remove(AudioFile audioFile) {
        audioList.remove(audioFile);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<AudioFile> getAudioList() {
        return audioList;
    }
}