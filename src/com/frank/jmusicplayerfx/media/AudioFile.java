package com.frank.jmusicplayerfx.media;

import com.frank.jmusicplayerfx.Data;
import com.frank.jmusicplayerfx.Data.Album;
import com.frank.jmusicplayerfx.Data.Artist;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
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
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioFile {
    private final StringProperty title;
    private final ObjectProperty<Artist> artist;
    private final ObjectProperty<Artist> albumArtist;
    private final ObjectProperty<Album> album;
    private final StringProperty genre;
    private final IntegerProperty year;
    private final IntegerProperty trackNumber;
    private final ObjectProperty<Duration> duration;
    private final ObjectProperty<Image> cover;

    private Media media;
    private final File file;

    public AudioFile(File file) throws FileNotFoundException {
        if (file == null || !file.exists())
            throw new FileNotFoundException(file + " null or doesn't exists.");

        this.file = file;

        title       = new ReadOnlyStringWrapper(UNKNOWN_TITLE);
        artist      = new ReadOnlyObjectWrapper<>(null);
        albumArtist = new ReadOnlyObjectWrapper<>(null);
        album       = new ReadOnlyObjectWrapper<>(null);
        genre       = new ReadOnlyStringWrapper(UNKNOWN_GENRE);
        year        = new ReadOnlyIntegerWrapper(0);
        trackNumber = new ReadOnlyIntegerWrapper(1);
        duration    = new ReadOnlyObjectWrapper<>(Duration.ZERO);
        cover       = new ReadOnlyObjectWrapper<>(null);

        loadMetadata();
    }

    private void loadMetadata() {
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

            genre.set(id3v2.getGenreDescription() != null && !id3v2.getGenreDescription().isBlank()
                    ? id3v2.getGenreDescription() : UNKNOWN_GENRE);

            year.set(id3v2.getYear() != null ? Integer.parseInt(id3v2.getYear()) : 0);

            trackNumber.set(id3v2.getTrack() != null ? Integer.parseInt(id3v2.getTrack()) : 1);

            createDataObjects(id3v2);
        }
    }

    private void loadCover(ID3v2 id3v2) {
        byte[] imageData = id3v2.getAlbumImage();
        if (imageData != null) {
            Image img = new Image(new ByteArrayInputStream(imageData));
            cover.set(img);
        }
    }

    private void createDataObjects(ID3v2 id3v2) {
        Data data = JMusicPlayerFX.getInstance().getAudioLoader().getInfo();

        String id3v2Artist = id3v2.getArtist();
        String artistName = id3v2Artist == null || id3v2Artist.isBlank()
                ? UNKNOWN_ARTIST : id3v2Artist.trim();

        String id3v2Album = id3v2.getAlbum();
        String albumName = id3v2Album == null || id3v2Album.isBlank()
                ? UNKNOWN_ALBUM : id3v2Album.trim();

        String id3v2AlbumArtist = id3v2.getAlbumArtist();
        String albumArtistName = id3v2AlbumArtist == null || id3v2AlbumArtist.isBlank()
                ? UNKNOWN_ARTIST : id3v2AlbumArtist.trim();


        Artist artist = data.getOrAddArtist(artistName);
        if (albumArtistName.equals(UNKNOWN_ARTIST)) {
            albumArtistName = artistName.split(";")[0];
        }
        Artist albumArtist = data.getOrAddArtist(albumArtistName);
        Album album = data.getOrAddAlbum(albumName, albumArtist);

        artistProperty().set(artist);
        albumArtistProperty().set(albumArtist);
        albumProperty().set(album);

        if (!albumName.equals(UNKNOWN_ALBUM)) {
            if (album.yearProperty().get() == 0) {
                int year = yearProperty().get();
                if (year != 0) {
                    album.yearProperty().set(year);
                }
            }
            if (album.coverProperty().get() == null) {
                loadCover(id3v2);
                if (getCover() != null) {
                    album.coverProperty().set(getCover());
                }
            }
        }

        album.addSong(this);
    }

    public void delete() {
        albumProperty().get().songsListProperty().remove(this);
        albumProperty().set(null);

    }

    public Media getMedia() {
        if (this.media == null) {
            this.media = new Media(file.toURI().toString());
        }
        return this.media;
    }

    public String getTitle() {
        return title.get();
    }

    public Image getCover() {
        return cover.get();
    }

    public Duration getDuration() {
        return duration.get();
    }

    public StringProperty titleProperty() { return title; }
    public IntegerProperty trackNumberProperty() { return trackNumber; }
    public IntegerProperty yearProperty() { return year; }
    public ObjectProperty<Artist> artistProperty() { return artist; }
    public ObjectProperty<Artist> albumArtistProperty() { return albumArtist; }
    public ObjectProperty<Album> albumProperty() { return album; }
    public ObjectProperty<Image> coverProperty() { return cover; }
    public ObjectProperty<Duration> durationProperty() { return duration; }

    @Override
    public String toString() {
        return getTitle() + " - " + artistProperty().get().getName();
    }

    public static String[] EXTENSIONS = new String[]{"mp3"};

    public static final String UNKNOWN_ARTIST = "Unknown artist";
    public static final String UNKNOWN_ALBUM = "Unknown album";
    public static final String UNKNOWN_TITLE = "Unknown title";
    public static final String UNKNOWN_GENRE = "Unknown genre";
}