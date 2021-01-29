package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.PlayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.media.MediaPlayer;

import static com.frank.jmusicplayerfx.JMusicPlayerFX.getInstance;

public final class GlobalPlayer {
    private final ObjectProperty<MediaPlayer> mediaPlayerProperty;
    private final ObjectProperty<AudioFile> currentAudioProperty;
    private PlayList currentPlayList;
    private int currentIndex;

    public GlobalPlayer() {
        mediaPlayerProperty = new ReadOnlyObjectWrapper<>();
        currentAudioProperty = new ReadOnlyObjectWrapper<>();

        mediaPlayerProperty.addListener((obs, oldPlayer, newPlayer) -> {
            double volume = 0.10;
            if (oldPlayer != null) {
                volume = oldPlayer.getVolume();
                oldPlayer.stop();
            }

            newPlayer.setVolume(volume);
            newPlayer.setOnEndOfMedia(() -> {
                next();
                play();
            });
        });

        currentAudioProperty.addListener((obs, oldAudio, newAudio) -> {
            if (newAudio != null) {
                getInstance().setTitle(newAudio.toString());
            } else {
                getInstance().resetTitle();
            }
        });
    }

    public void initNewAudio(PlayList playList, AudioFile audioFile) {
        int index = 0;

        if (currentPlayList != playList) {
            setCurrentPlayList(playList);
        }
        if (playList != null) {
            index = playList.indexOf(audioFile);
        }

        setCurrentIndex(index);

        currentAudioProperty.set(audioFile);
        mediaPlayerProperty.set(audioFile.getMediaPlayer());
    }

    private void initNewAudio(int index) {
        AudioFile audioFile = currentPlayList.get(index);
        setCurrentIndex(index);

        currentAudioProperty.set(audioFile);
        mediaPlayerProperty.set(audioFile.getMediaPlayer());
    }

    public void play() {
        mediaPlayerProperty.get().play();
    }

    public void pause() {
        mediaPlayerProperty.get().pause();
    }

    public void previous() {
        if (currentPlayList != null) {
            if (currentIndex > 0) {
                currentIndex--;
                initNewAudio(currentIndex);
            }
        }
    }

    public void next() {
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

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
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