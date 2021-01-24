package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioFile;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.media.MediaPlayer;

import java.util.List;

public class GlobalPlayer {
    private MediaPlayer currentMediaPlayer;
    private List<AudioFile> currentList;
    private int currentIndex;

    public GlobalPlayer() {
    }

    public void playMedia(AudioFile audioFile) {
        currentMediaPlayer = audioFile.getMediaPlayer();

        currentMediaPlayer.play();
    }

    public MediaPlayer getCurrentMediaPlayer() {
        return currentMediaPlayer;
    }

    public List<AudioFile> getCurrentList() {
        return currentList;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}