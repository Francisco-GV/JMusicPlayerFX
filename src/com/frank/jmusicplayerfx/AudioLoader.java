package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.util.Util;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class AudioLoader {
    private final ObservableList<Directory> directories;
    private final Info info;
    private final Timer loaderTimer;
    private final ObservableList<AudioFile> allAudioList;

    public AudioLoader() {
        info = new Info();
        loaderTimer = new Timer(true);
        allAudioList = FXCollections.observableArrayList();
        directories = FXCollections.observableArrayList();

        directories.addListener((ListChangeListener<Directory>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::addDirectoryListeners);
                change.getRemoved().forEach(this::removeDirectoryListeners);
            }
        });
    }

    private void addDirectoryListeners(Directory directory) {
        ListChangeListener<AudioFile> audioListListener = change -> {
            while (change.next()) {
                allAudioList.addAll(change.getAddedSubList());
                allAudioList.removeAll(change.getRemoved());
            }
        };
        ListChangeListener<Directory> innerDirectoriesListener = change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::addDirectoryListeners);
                change.getRemoved().forEach(this::removeDirectoryListeners);
            }
        };

        Function<Directory, Void> search = new Function<>() {
            @Override
            public Void apply(Directory dir) {
                allAudioList.addAll(dir.getAudioList());
                dir.getInnerDirectoriesList().forEach(this::apply);
                dir.addListListeners(audioListListener, innerDirectoriesListener);
                return null;
            }
        };

        search.apply(directory);
    }

    public void addNewDirectory(File file) {
        if (file.exists() && file.isDirectory()) {
            Directory newDirectory = new Directory(file.getAbsolutePath());
            if (!directories.contains(newDirectory)) {
                directories.add(newDirectory);
            }
        }
    }

    @SuppressWarnings("unused")
    private void removeDirectory(Directory directory) {
        directories.remove(directory);
    }

    private void removeDirectoryListeners(Directory directory) {
        Function<Directory, Void> search = new Function<>() {
            @Override
            public Void apply(Directory dir) {
                allAudioList.removeAll(dir.getAudioList());
                dir.getInnerDirectoriesList().forEach(this::apply);
                dir.removeListListeners();
                return null;
            }
        };

        search.apply(directory);
    }

    public void addNewDirectory(String path) {
        File musicDirFile = new File(path);

        addNewDirectory(musicDirFile);
    }

    public ObservableList<AudioFile> getAllMediaList() {
        return allAudioList;
    }

    public void loadDirectoryMedia(Directory directory) {
        if (directories.contains(directory)) {
            System.out.println("loading \"" + directory + "\" content...");
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(directory::searchAudioFiles);
                }
            };
            loaderTimer.schedule(task, 0);
        }
    }

    public void loadAllMedia() {
        directories.forEach(this::loadDirectoryMedia);
    }

    public Info getInfo() {
        return info;
    }

    public static class Directory {
        private final String path;
        private final ObservableList<AudioFile> audioList;
        private final ObservableList<Directory> innerDirectoriesList;

        public Directory(String path) {
            this.path = path;

            audioList = FXCollections.observableArrayList();
            innerDirectoriesList = FXCollections.observableArrayList();
        }

        public File getAsFile() {
            return new File(path);
        }

        public void searchAudioFiles() {
            File[] fileList = getAsFile().listFiles(extensionFilter);

            if (fileList != null) {
                for (File file : fileList) {
                    AudioFile audioFile = new AudioFile(file);
                    audioList.add(audioFile);
                }
            }
            searchForInnerDirectories();
        }

        public void searchForInnerDirectories() {
            File[] innerDirectories = getAsFile().listFiles(directoryFilter);

            if (innerDirectories != null) {
                for (File dir : innerDirectories) {
                    Directory directory = new Directory(dir.getAbsolutePath());
                    directory.searchAudioFiles();
                    innerDirectoriesList.add(directory);
                }
            }
        }

        public ObservableList<AudioFile> getAudioList() {
            return audioList;
        }

        public ObservableList<Directory> getInnerDirectoriesList() {
            return innerDirectoriesList;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof Directory) {
                return this.path.equalsIgnoreCase(((Directory) obj).path);
            } else return false;
        }

        @Override
        public String toString() {
            return path;
        }

        private ListChangeListener<AudioFile> audioChangeListener;
        private ListChangeListener<Directory> innerDirectoriesChangeListener;

        private synchronized void addListListeners(ListChangeListener<AudioFile> audioChangeListener,
                                                   ListChangeListener<Directory> innerDirectoriesChangeListener) {
            this.audioChangeListener = audioChangeListener;
            this.innerDirectoriesChangeListener = innerDirectoriesChangeListener;

            audioList.addListener(audioChangeListener);
            innerDirectoriesList.addListener(innerDirectoriesChangeListener);
        }

        private synchronized void removeListListeners() {
            audioList.removeListener(audioChangeListener);
            innerDirectoriesList.removeListener(innerDirectoriesChangeListener);
        }
    }

    public static final class Info {
        private final ObservableList<Artist> artists;
        private final ObservableList<Album> albums;

        public Info() {
           artists = FXCollections.observableArrayList();
            albums = FXCollections.observableArrayList();
        }

        public ObservableList<Artist> getArtists() {
            return artists;
        }

        public ObservableList<Album> getAlbums() {
            return albums;
        }

        public Artist getOrReturnArtist(Artist artist) {
            if (artists.contains(artist)) {
                int index = artists.indexOf(artist);
                return artists.get(index);
            }
            return artist;
        }

        public Album getOrReturnAlbum(Album album) {
            if (albums.contains(album)) {
                int index = albums.indexOf(album);
                return albums.get(index);
            }
            return album;
        }

        public synchronized void registerArtist(Artist artist) {
            if (!artists.contains(artist)) {
                artists.add(artist);
            }
        }

        public synchronized void registerAlbum(Album album) {
            if (!albums.contains(album)) {
                albums.add(album);
            }
        }

        public static final class Artist {
            private final String name;
            private final ObservableList<Album> albums;
            private final ObjectProperty<Image> picture;

            public Artist(String name) {
                this.name = name;
                albums = FXCollections.observableArrayList();
                picture = new SimpleObjectProperty<>();
            }

            public ObservableList<Album> getAlbums() {
                return albums;
            }

            public ObjectProperty<Image> pictureProperty() {
                return picture;
            }

            public String getName() {
                return name;
            }

            public synchronized void addAlbum(Album album) {
                if (!albums.contains(album)) {
                    albums.add(album);
                }
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

        public static final class Album {
            private final String name;
            private final Artist albumArtist;
            private final ListProperty<AudioFile> songs;
            private final ObjectProperty<Type> type;
            private final ObjectProperty<Image> cover;
            private final IntegerProperty year;
            private final ObjectProperty<Duration> totalDuration;
            private final StringProperty totalDurationFormatted;

            public Album(String name, Artist artist) {
                this.name = name;
                this.albumArtist = artist;

                type = new ReadOnlyObjectWrapper<>(Type.EMPTY);
                cover = new ReadOnlyObjectWrapper<>();
                year = new ReadOnlyIntegerWrapper();
                totalDuration = new ReadOnlyObjectWrapper<>(Duration.ZERO);
                totalDurationFormatted = new ReadOnlyStringWrapper("00:00 minutes");

                totalDurationFormatted.bind(Bindings.createStringBinding(() -> Util.formatTime(totalDuration.get()) + " minutes", totalDuration));

                songs = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

                songs.addListener((ListChangeListener<AudioFile>) change -> {
                    while (change.next()) {
                        change.getAddedSubList().forEach(songAdded -> {
                            Duration duration = songAdded.getDuration();
                            totalDuration.set(totalDuration.get().add(duration));
                        });
                        change.getRemoved().forEach(songDeleted ->
                                totalDuration.set(totalDuration.get().subtract(songDeleted.getDuration())));
                    }

                    Duration duration = totalDuration.get();
                    Duration thirty = Duration.minutes(30);
                    int size = songs.size();

                    if (songs.isEmpty()) {
                        type.set(Type.EMPTY);
                    } else if (size <= 3) {
                        if (totalDuration.get().lessThan(thirty)) {

                            boolean moreTenMinutes = false;
                            for (AudioFile song : songs) {
                                if (song.getDuration().greaterThanOrEqualTo(Duration.minutes(10))) {
                                    moreTenMinutes = true;
                                }
                            }
                            type.set(moreTenMinutes ? Type.EP : Type.SINGLE);
                        }
                    } else if (size < 6 && duration.lessThanOrEqualTo(thirty)) {
                        type.set(Type.EP);
                    } else if (size >= 6) {
                        type.set(Type.ALBUM);
                    }
                });
            }

            public enum Type {
                EMPTY, SINGLE, EP, ALBUM
            }

            public ListProperty<AudioFile> getSongs() {
                return songs;
            }

            public String getName() {
                return name;
            }

            public Artist getAlbumArtist() {
                return albumArtist;
            }

            public synchronized void addSong(AudioFile song) {
                if (!songs.contains(song)) {
                    songs.add(song);
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

            public StringProperty totalDurationFormattedProperty() {
                return totalDurationFormatted;
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
    }

    private static final FileFilter directoryFilter;
    private static final FilenameFilter extensionFilter;

    static {
        directoryFilter = File::isDirectory;
        extensionFilter = (dir, name) -> {
            for (String extension : AudioFile.EXTENSIONS) {
                if (name.toLowerCase().endsWith(extension.toLowerCase())) {
                    return true;
                }
            }
            return false;
        };
    }
}