package com.frank.jmusicplayerfx.media;

import static com.frank.jmusicplayerfx.Util.formatTime;
import static com.frank.jmusicplayerfx.media.AudioLoader.Info.*;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.util.Map;

@SuppressWarnings("unused")
public class AudioFile {
    private final StringProperty title;
    private final StringProperty artist;
    private final StringProperty album;
    private final StringProperty durationFormated;
    private final MediaPlayer mediaPlayer;

    public AudioFile(File file) {
        title = new SimpleStringProperty(DEFAULT_TITLE);
        artist = new SimpleStringProperty(DEFAULT_ARTIST);
        album = new SimpleStringProperty(DEFAULT_ALBUM);
        durationFormated = new SimpleStringProperty("00:00");

        Media media = new Media(file.toURI().toString());
        this.mediaPlayer = new MediaPlayer(media);

        mediaPlayer.statusProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends MediaPlayer.Status> observable, MediaPlayer.Status oldValue, MediaPlayer.Status newValue) {
                if (newValue == MediaPlayer.Status.READY) {
                    ObservableMap<String, Object> metadata = mediaPlayer.getMedia().getMetadata();

                    title.bind(getOrDefault(metadata, "title", DEFAULT_TITLE));
                    artist.bind(getOrDefault(metadata, "artist", DEFAULT_ARTIST));
                    album.bind(getOrDefault(metadata, "album", DEFAULT_ALBUM));
                    durationFormated.bind(Bindings.createStringBinding(() -> formatTime(media.getDuration())));

                    AudioLoader.Info info = JMusicPlayerFX.getInstance().getAudioLoader().getInfo();

                    Artist artist = new Artist(artistProperty().get());
                    Album album = new Album(albumProperty().get(), artist);


                    if (!albumProperty().get().equals(DEFAULT_ALBUM)) {
                        album.getSongs().add(AudioFile.this);
                        artist.getAlbums().add(album);
                        info.addAlbum(album);
                    } else {
                        artist.getSingles().add(AudioFile.this);
                    }

                    info.addArtist(artist);
                }
            }

            private StringBinding getOrDefault(Map<String, Object> map, String key, String defaultText) {
                return Bindings.createStringBinding(() -> {
                    if (!map.containsKey(key)) return defaultText;
                    String value = (String) map.get(key);
                    return value != null ? value : defaultText;
                });
            }
        });
    }

    @Override
    public String toString() {
        return getTitle() + " - " + getArtist();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

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

    public String getDurationFormated() {
        return durationFormated.get();
    }

    public StringProperty durationFormatedProperty() {
        return durationFormated;
    }

    public static String[] EXTENSIONS = new String[]{
            "mp3", "wav"
    };

    public static final String DEFAULT_TITLE = "Unknown";
    public static final String DEFAULT_ARTIST = "Unknown artist";
    public static final String DEFAULT_ALBUM = "Unknown";
}