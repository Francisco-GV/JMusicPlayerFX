package com.frank.jmusicplayerfx.media;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

@SuppressWarnings("unused")
public class AudioFile {
    private final StringProperty title;
    private final StringProperty artist;
    private final StringProperty album;
    private final ObjectProperty<Duration> duration;
    private final StringProperty durationFormated;
    private final MediaPlayer mediaPlayer;

    public AudioFile(File file) {
        title = new SimpleStringProperty("Unknown");
        artist = new SimpleStringProperty("Unknown");
        album = new SimpleStringProperty("Unknown");
        durationFormated = new SimpleStringProperty("00:00");
        duration = new SimpleObjectProperty<>(Duration.seconds(0));

        Media media = new Media(file.toURI().toString());
        this.mediaPlayer = new MediaPlayer(media);

        mediaPlayer.statusProperty().addListener((obs, old, newValue) -> {
            if (newValue == MediaPlayer.Status.READY) {
                ObservableMap<String, Object> metadata = mediaPlayer.getMedia().getMetadata();

                title.bind(Bindings.valueAt(metadata, "title").asString());
                artist.bind(Bindings.valueAt(metadata, "artist").asString());
                album.bind(Bindings.valueAt(metadata, "album").asString());
                duration.bind(Bindings.createObjectBinding(media::getDuration));
                durationFormated.bind(Bindings.createStringBinding(() -> formatTime(media.getDuration())));
            }
        });
    }

    private String formatTime(Duration duration) {
        double totalSeconds = duration.toSeconds();
        int minutes = (int) totalSeconds / 60;
        int seconds = (int) totalSeconds % 60;

        String stringMinutes = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
        String stringSeconds = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);

        return stringMinutes + ":" + stringSeconds;
    }

    @Override
    public String toString() {
        return getTitle();
    }


    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static String[] EXTENSIONS = new String[]{
            "mp3", "aiff", "wav", "mpeg4"
    };

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getArtist() {
        return artist.get();
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public String getAlbum() {
        return album.get();
    }

    public StringProperty albumProperty() {
        return album;
    }

    public Duration getDuration() {
        return duration.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public String getDurationFormated() {
        return durationFormated.get();
    }

    public StringProperty durationFormatedProperty() {
        return durationFormated;
    }
}