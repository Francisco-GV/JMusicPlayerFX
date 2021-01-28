package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.PlayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public final class GlobalPlayer {
    private final ObjectProperty<MediaPlayer> mediaPlayerProperty;
    private final ObjectProperty<AudioFile> currentAudioProperty;
    private PlayList currentPlayList;
    private int currentIndex;

    public GlobalPlayer() {
        mediaPlayerProperty = new ReadOnlyObjectWrapper<>();
        currentAudioProperty = new ReadOnlyObjectWrapper<>();

        mediaPlayerProperty.addListener((obs, oldPlayer, newPlayer) -> {
            double volume = 1.0;
            if (oldPlayer != null) {
                volume = oldPlayer.getVolume();
                oldPlayer.stop();
            }

            newPlayer.setVolume(volume);
        });

        currentAudioProperty.addListener((obs, oldAudio, newAudio) -> {
            if (newAudio != null) {
                JMusicPlayerFX.getInstance().setTitle(newAudio.toString());
            } else {
                JMusicPlayerFX.getInstance().resetTitle();
            }
        });


    }

    public void initNewAudio(int index) {
        if (currentPlayList != null) {
            currentIndex = index;
            AudioFile audioFile = currentPlayList.getAudioFiles().get(index);
            currentAudioProperty.set(audioFile);
            mediaPlayerProperty.set(audioFile.getMediaPlayer());
            play();
        }
    }

    public void initNewAudio(AudioFile audioFile) {
        if (currentPlayList != null) {
            if (currentPlayList.getAudioFiles().contains(audioFile)) {
                currentIndex = currentPlayList.getAudioFiles().indexOf(audioFile);
            } else {
                currentIndex = 0;
            }
        } else currentIndex = 0;

        currentAudioProperty.set(audioFile);
    }

    public void play() {
        mediaPlayerProperty.get().play();
    }

    public void pause() {
        mediaPlayerProperty.get().pause();
    }

    public void stop() {
        mediaPlayerProperty.get().stop();
    }

    public void next() {
        if (currentPlayList != null) {
            if (currentIndex > 0) {
                currentIndex--;
                initNewAudio(currentIndex);
            }
        }
    }

    public void previous() {
        if (currentPlayList != null) {
            if (currentIndex < currentPlayList.getAudioFiles().size() - 1) {
                currentIndex++;
                initNewAudio(currentIndex);
            }
        }
    }

    public void setCurrentPlayList(PlayList playList) {
        this.currentPlayList = playList;
    }


    public void setVolume(double volume) {
        mediaPlayerProperty.get().setVolume(volume);
    }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return mediaPlayerProperty.get().currentTimeProperty();
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public ObjectProperty<AudioFile> currentAudioProperty() {
        return currentAudioProperty;
    }

    public ObjectProperty<MediaPlayer> mediaPlayerProperty() {
        return mediaPlayerProperty;
    }

    public PlayList getCurrentPlayList() {
        return currentPlayList;
    }
}