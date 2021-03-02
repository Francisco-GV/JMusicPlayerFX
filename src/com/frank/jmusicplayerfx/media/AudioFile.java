package com.frank.jmusicplayerfx.media;

import com.frank.jmusicplayerfx.Data;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.frank.jmusicplayerfx.Data.Album;
import static com.frank.jmusicplayerfx.Data.Artist;
import static com.frank.jmusicplayerfx.util.Util.formatTime;

@SuppressWarnings("unused")
public class AudioFile {
    private final StringProperty title;
    private final StringProperty artist;
    private final StringProperty album;
    private final StringProperty albumArtist;
    private final StringProperty genre;
    private final StringProperty durationFormatted;
    private final IntegerProperty year;
    private final IntegerProperty trackNumber;
    private final ObjectProperty<Duration> duration;
    private final ObjectProperty<Image> cover;

    private Media media;
    private final File file;

    public AudioFile(File file) {
        this.file = file;

        title = new ReadOnlyStringWrapper(UNKNOWN_TITLE);
        artist = new ReadOnlyStringWrapper(UNKNOWN_ARTIST);
        albumArtist = new ReadOnlyStringWrapper(UNKNOWN_ARTIST);
        album = new ReadOnlyStringWrapper(UNKNOWN_ALBUM);
        genre = new ReadOnlyStringWrapper(UNKNOWN_GENRE);
        year = new ReadOnlyIntegerWrapper(0);
        trackNumber = new ReadOnlyIntegerWrapper(1);
        duration = new ReadOnlyObjectWrapper<>(Duration.ZERO);
        durationFormatted = new ReadOnlyStringWrapper("00:00");
        cover = new ReadOnlyObjectWrapper<>(null);
        durationFormatted.bind(Bindings.createStringBinding(() -> formatTime(duration.get()), durationProperty()));

        ID3v2 id3v2 = null;
        try {
            Mp3File mp3File = new Mp3File(file);

            duration.set(new Duration(mp3File.getLengthInMilliseconds()));

            if (mp3File.hasId3v2Tag()) {
                id3v2 = mp3File.getId3v2Tag();
            } else if (mp3File.hasId3v1Tag()) {
                id3v2 = (ID3v2) mp3File.getId3v1Tag();
            }
        } catch (IOException | InvalidDataException | UnsupportedTagException e) {
            e.printStackTrace();
        }

        if (id3v2 != null) {
            title.set(id3v2.getTitle() != null && !id3v2.getTitle().isBlank()
                    ? id3v2.getTitle() : UNKNOWN_TITLE);
            artist.set(id3v2.getArtist() != null && !id3v2.getArtist().isBlank()
                    ? id3v2.getArtist().trim() : UNKNOWN_ARTIST);
            albumArtist.set(id3v2.getAlbumArtist() != null && !id3v2.getAlbumArtist().isBlank()
                    ? id3v2.getAlbumArtist().trim() : UNKNOWN_ARTIST);
            album.set(id3v2.getAlbum() != null && !id3v2.getAlbum().isBlank()
                    ? id3v2.getAlbum().trim() : UNKNOWN_ALBUM);
            genre.set(id3v2.getGenreDescription() != null && !id3v2.getGenreDescription().isBlank()
                    ? id3v2.getGenreDescription() : UNKNOWN_GENRE);
            year.set(id3v2.getYear() != null
                    ? Integer.parseInt(id3v2.getYear()) : 0);
            trackNumber.set(id3v2.getTrack() != null
                    ? Integer.parseInt(id3v2.getTrack()) : 1);
        }

        createArtistAndAlbum();

        if (id3v2 != null) {
            Data data = JMusicPlayerFX.getInstance().getAudioLoader().getInfo();

            Artist albumArtist = data.getOrReturnArtist(getAlbumArtist());
            Album album = data.getOrReturnAlbum(getAlbum(), albumArtist);

            if (album.coverProperty().get() == null) {
                loadCover(id3v2);
                album.coverProperty().set(getCover());
            }
        }
    }

    private void loadCover(ID3v2 id3v2) {
        byte[] imageData = id3v2.getAlbumImage();
        if (imageData != null) {
            Image img = new Image(new ByteArrayInputStream(imageData));
            cover.set(img);
        }
    }

    private void createArtistAndAlbum() {
        Data data = JMusicPlayerFX.getInstance().getAudioLoader().getInfo();

        Artist artist = data.getOrReturnArtist(getArtist());
        Artist albumArtist = data.getOrReturnArtist(getAlbumArtist());

        if (getAlbumArtist().equals(UNKNOWN_ARTIST)) {
            String artistName = getArtist().split(";")[0];
            albumArtist = data.getOrReturnArtist(artistName);
        }

        Album album = data.getOrReturnAlbum(getAlbum(), albumArtist);

        if (getCover() != null && album.coverProperty().get() == null) {
            album.coverProperty().set(getCover());
        }
        if (getYear() != 0 && album.yearProperty().get() == 0) {
            album.yearProperty().set(getYear());
        }

        if (!getAlbum().equals(UNKNOWN_ALBUM)) {
            album.addSong(this);
            data.registerAlbum(album);
            artist.addAlbum(album);
        }

        data.registerArtist(artist);
    }

    @Override
    public String toString() {
        return getTitle() + " - " + getArtist();
    }

    public Media getMedia() {
        if (media == null) {
            this.media = new Media(file.toURI().toString());
        }

        return media;
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

    public String getAlbumArtist() {
        return albumArtist.get();
    }

    public StringProperty albumArtistProperty() {
        return albumArtist;
    }

    public String getAlbum() {
        return album.get();
    }

    public StringProperty albumProperty() {
        return album;
    }

    public String getGenre() {
        return genre.get();
    }

    public StringProperty genreProperty() {
        return genre;
    }

    public Image getCover() {
        return cover.get();
    }

    public ObjectProperty<Image> coverProperty() {
        return cover;
    }

    public String getDurationFormatted() {
        return durationFormatted.get();
    }

    public StringProperty durationFormattedProperty() {
        return durationFormatted;
    }

    public int getTrackNumber() {
        return trackNumber.get();
    }

    public IntegerProperty trackNumberProperty() {
        return trackNumber;
    }

    public int getYear() {
        return year.get();
    }

    public IntegerProperty yearProperty() {
        return year;
    }

    public Duration getDuration() {
        return duration.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public static String[] EXTENSIONS = new String[]{"mp3"};

    public static final String UNKNOWN_ARTIST = "Unknown artist";
    public static final String UNKNOWN_ALBUM = "Unknown album";
    public static final String UNKNOWN_TITLE = "Unknown title";
    public static final String UNKNOWN_GENRE = "Unknown genre";
}