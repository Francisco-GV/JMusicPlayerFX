package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioFile;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.util.Duration;

public final class Data {
    private final ListProperty<Artist> artists;
    private final ListProperty<Album> albums;

    public Data() {
        ObservableList<Artist> artists = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        ObservableList<Album> albums = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

        this.artists = new ReadOnlyListWrapper<>(artists);
        this.albums = new ReadOnlyListWrapper<>(albums);
    }

    public ListProperty<Artist> getArtists() {
        return artists;
    }

    public ListProperty<Album> getAlbums() {
        return albums;
    }

    public Artist getOrAddArtist(String artistName) {
        synchronized(artists) {
            Artist artistObject = artists.stream().filter(
                    artist -> artist.getName().equals(artistName))
                    .findFirst().orElse(null);

            if (artistObject != null) return artistObject;

            artistObject = new Artist(artistName);
            artists.add(artistObject);
            return artistObject;
        }
    }

    public Album getOrAddAlbum(String albumName, Artist albumArtist) {
        synchronized (albums) {
            Album albumObject = albums.stream().filter(
                    album -> album.getName().equals(albumName)
                            && album.getAlbumArtist().equals(albumArtist))
                    .findFirst().orElse(null);

            if (albumObject != null) return albumObject;

            albumObject = new Album(albumName, albumArtist);
            albumArtist.addAlbum(albumObject);
            albums.add(albumObject);
            return albumObject;
        }
    }

    public void registerArtist(Artist artist) {
        if (!artists.contains(artist)) {
            artists.add(artist);
        }
    }

    public void registerAlbum(Album album) {
        if (!albums.contains(album)) {
            albums.add(album);
        }
    }

    public final class Artist {
        private final String name;
        private final ListProperty<Album> artistAlbums;
        private final ObjectProperty<Image> picture;
        private boolean collab;

        public Artist(String name) {
            this.name = name;

            picture = new SimpleObjectProperty<>();

            ObservableList<Album> observableList = FXCollections.observableArrayList();
            ObservableList<Album> synchronizedList = FXCollections.synchronizedObservableList(observableList);

            artistAlbums = new ReadOnlyListWrapper<>(synchronizedList);

            artistAlbums.sizeProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.intValue() == 0 && oldValue.intValue() != 0) {
                    artists.remove(this);
                    System.out.println(this + " was deleted");
                }
            });

            setCollab(collabCharacterList.stream().anyMatch(name::contains));
        }

        public void addAlbum(Album album) {
            if (!artistAlbums.contains(album)) {
                artistAlbums.add(album);
            }
        }

        public void removeAlbum(Album album) {
            artistAlbums.remove(album);
        }

        public ObservableList<Album> getArtistAlbums() {
            return artistAlbums;
        }

        public ObjectProperty<Image> pictureProperty() {
            return picture;
        }

        public String getName() {
            return name;
        }

        public boolean isCollab() {
            return collab;
        }

        public void setCollab(boolean collab) {
            this.collab = collab;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Artist)) return false;

            String name1 = this.getName();
            String name2 = ((Artist) obj).getName();

            return name1.equals(name2);
        }

        @Override
        public String toString() {
            return "[Artist]: " + name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public final class Album {
        private final String name;
        private final Artist albumArtist;
        private final ListProperty<AudioFile> songs;

        private final ObjectProperty<Type> type;
        private final ObjectProperty<Image> cover;
        private final IntegerProperty year;
        private final ObjectProperty<Duration> totalDuration;

        public Album(String name, Artist artist) {
            this.name = name;
            this.albumArtist = artist;

            type = new ReadOnlyObjectWrapper<>(Type.EMPTY);
            cover = new ReadOnlyObjectWrapper<>();
            year = new ReadOnlyIntegerWrapper();
            totalDuration = new ReadOnlyObjectWrapper<>(Duration.ZERO);

            ObservableList<AudioFile> observableList = FXCollections.observableArrayList();
            ObservableList<AudioFile> synchronizedList = FXCollections.synchronizedObservableList(observableList);

            songs = new ReadOnlyListWrapper<>(synchronizedList);

            songs.sizeProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.intValue() == 0 && oldValue.intValue() != 0) {
                    albums.remove(this);
                    albumArtist.removeAlbum(this);
                    System.out.println(this + " was deleted");
                }
            });

            songs.addListener((ListChangeListener<AudioFile>) change -> {
                Duration durationChange = Duration.ZERO;
                while (change.next()) {
                    for (AudioFile songAdded : change.getAddedSubList()) {
                        songAdded.coverProperty().bind(this.coverProperty());
                        Duration duration = songAdded.getDuration();
                        durationChange = durationChange.add(duration);
                    }

                    for (AudioFile songDeleted : change.getRemoved()) {
                        songDeleted.coverProperty().unbind();
                        Duration duration = songDeleted.getDuration();
                        durationChange = durationChange.subtract(duration);
                    }
                }

                totalDuration.set(totalDuration.get().add(durationChange));

                Duration duration = totalDuration.get();
                Duration thirty = Duration.minutes(30);
                int size = songs.size();

                if (songs.isEmpty()) {
                    changeType(Type.EMPTY);
                } else if (size <= 3) {
                    if (totalDuration.get().lessThan(thirty)) {

                        boolean moreTenMinutes = false;
                        for (AudioFile song : songs) {
                            if (song.getDuration().greaterThanOrEqualTo(Duration.minutes(10))) {
                                moreTenMinutes = true;
                            }
                        }
                        changeType(moreTenMinutes ? Type.EP : Type.SINGLE);
                    }
                } else if (size < 6 && duration.lessThanOrEqualTo(thirty)) {
                    changeType(Type.EP);
                } else if (size >= 6) {
                    changeType(Type.ALBUM);
                }
            });
        }

        private void changeType(Type type) {
            if (typeProperty().get() != type) {
                typeProperty().set(type);
            }
        }

        public ListProperty<AudioFile> songsListProperty() {
            return songs;
        }

        public String getName() {
            return name;
        }

        public Artist getAlbumArtist() {
            return albumArtist;
        }

        public void addSong(AudioFile song) {
            synchronized (songs.get()) {
                if (!songs.contains(song)) {
                    songs.add(song);
                }
            }
        }

        public ObjectProperty<Image> coverProperty() {
            return cover;
        }

        public IntegerProperty yearProperty() {
            return year;
        }

        public ObjectProperty<Type> typeProperty() {
            return type;
        }

        public ObjectProperty<Duration> totalDurationProperty() {
            return totalDuration;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Album)) return false;

            Album album = (Album) obj;
            return (album.getName().equals(this.getName()));
        }

        @Override
        public String toString() {
            return "[Album]: " + name + " by " + albumArtist.getName();
        }
    }

    public static final ObservableList<String> collabCharacterList;

    static {
        collabCharacterList = FXCollections.observableArrayList();

        collabCharacterList.addAll("; ", "/", " - ");
    }

    public enum Type {
        EMPTY, SINGLE, EP, ALBUM
    }
}