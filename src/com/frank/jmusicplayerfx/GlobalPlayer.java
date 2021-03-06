package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.Playlist;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import static com.frank.jmusicplayerfx.JMusicPlayerFX.getInstance;

public final class GlobalPlayer {
    private final ObjectProperty<MediaPlayer> mediaPlayer;
    private final ObjectProperty<AudioFile> currentAudio;
    private final ObjectProperty<Repeat> repeat;
    private final DoubleProperty volume;
    private final BooleanProperty mute;
    private final BooleanProperty shuffle;
    private final ObjectProperty<Playlist> currentPlaylist;
    // temporalPlaylist is use to save the ordered playlist when shuffle is used
    private int currentIndex;

    public enum Repeat {
        DISABLE,
        PLAYLIST,
        SONG
    }

    public GlobalPlayer() {
        mediaPlayer = new ReadOnlyObjectWrapper<>();
        currentAudio = new ReadOnlyObjectWrapper<>();
        volume = new SimpleDoubleProperty(0.5);
        mute = new SimpleBooleanProperty(false);
        repeat = new SimpleObjectProperty<>(Repeat.DISABLE);
        shuffle = new SimpleBooleanProperty(false);
        currentPlaylist = new SimpleObjectProperty<>();

        mediaPlayer.addListener((obs, oldPlayer, newPlayer) -> {
            if (oldPlayer != null) {
                oldPlayer.stop();
            }
            if (newPlayer != null) {
                newPlayer.volumeProperty().bind(volumeProperty());
                newPlayer.muteProperty().bind(muteProperty());

                newPlayer.setOnEndOfMedia(() -> {
                    if (repeat.get() == Repeat.SONG) {
                        newPlayer.seek(Duration.ZERO);
                        play();
                    } else if (hasNext()) {
                        next();
                        play();
                    }
                });
            }
        });

        currentAudio.addListener((obs, oldAudio, newAudio) -> {
            if (newAudio != null) {
                getInstance().setTitle(newAudio.toString());
            } else {
                getInstance().resetTitle();
            }
        });

        shuffle.addListener((obs, old, shuffle) -> {
            if (shuffle) {
                if (getCurrentPlaylist() != null) {

                }
            }
        });
    }

    public void initNewAudio(Playlist playList, AudioFile audioFile) {
        initNewAudio(playList, audioFile, null);
    }

    public void initNewAudio(Playlist playlist, AudioFile audioFile, Runnable onLoad) {
        MediaPlayer mediaPlayer = new MediaPlayer(audioFile.getMedia());

        mediaPlayer.setOnReady(() -> {
            int index = 0;

            if (currentPlaylist.get() != playlist) {
                setCurrentPlaylist(playlist);
            }
            if (playlist != null) {
                index = playlist.indexOf(audioFile);
            }
            setCurrentIndex(index);
            currentAudio.set(audioFile);
            mediaPlayerProperty().set(mediaPlayer);

            if (onLoad != null) onLoad.run();
        });
    }

    private void initNewAudio(int index) {
        AudioFile audioFile = getCurrentPlaylist().get(index);
        MediaPlayer mediaPlayer = new MediaPlayer(audioFile.getMedia());

        mediaPlayer.setOnReady(() -> {
            setCurrentIndex(index);
            currentAudio.set(audioFile);
            mediaPlayerProperty().set(mediaPlayer);

            getMediaPlayer().play();
        });
    }

    public void play() {
        mediaPlayer.get().play();
    }

    public void pause() {
        mediaPlayer.get().pause();
    }

    public void stop() {
        mediaPlayer.get().stop();
    }

    public void previous() {
        if (currentPlaylist != null) {
            if (currentIndex > 0) {
                currentIndex--;
                initNewAudio(currentIndex);
            }
        }
    }

    public void next() {
        if (currentPlaylist != null) {
            if (currentIndex < getCurrentPlaylist().getAudioFiles().size() - 1) {
                currentIndex++;
                initNewAudio(currentIndex);
            } else if (repeat.get() == Repeat.PLAYLIST) {
                currentIndex = 0;
                initNewAudio(currentIndex);
            }
        }
    }

    public boolean hasNext() {
        if (currentPlaylist != null) {
            if (currentIndex + 1 < getCurrentPlaylist().getAudioFiles().size()) {
                return true;
            } else return repeat.get() == Repeat.PLAYLIST;
        }
        return false;
    }

    public void setCurrentPlaylist(Playlist playlist) {
        currentPlaylist.set(playlist);
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public ObjectProperty<AudioFile> currentAudioProperty() {
        return currentAudio;
    }

    public ObjectProperty<MediaPlayer> mediaPlayerProperty() {
        return mediaPlayer;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer.get();
    }

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public BooleanProperty muteProperty() {
        return mute;
    }

    public ObjectProperty<Repeat> repeatProperty() {
        return repeat;
    }

    public BooleanProperty shuffleProperty() {
        return shuffle;
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist.get();
    }
}