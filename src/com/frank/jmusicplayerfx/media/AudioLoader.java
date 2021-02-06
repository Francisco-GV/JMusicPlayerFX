package com.frank.jmusicplayerfx.media;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class AudioLoader {
    private final List<Directory> directories;
    private final Info info;
    private final Timer loaderTimer;

    public AudioLoader() {
        directories = new ArrayList<>();
        info = new Info();
        loaderTimer = new Timer(true);
    }

    public void addNewDirectory(File file) {
        if (file.exists() && file.isDirectory()) {
            Directory newDirectory = new Directory(file.getAbsolutePath());
            if (!directories.contains(newDirectory)) {
                directories.add(newDirectory);
                loadDirectoryMedia(newDirectory);
            }
        }
    }

    public List<AudioFile> getAllMedia() {
        List<AudioFile> list = new ArrayList<>();
        for (Directory directory : directories) {
            list.addAll(directory.getAllMedia());
        }
        return list;
    }

    public void loadDirectoryMedia(Directory directory) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                directory.searchAudioFiles();
            }
        };
        loaderTimer.schedule(task, 0);
    }

    public void reloadAllMedia() {
        directories.forEach(this::loadDirectoryMedia);
    }

    public Info getInfo() {
        return info;
    }

    private static class Directory {
        private final String path;
        private final List<AudioFile> audioList;
        private final List<Directory> innerDirectoriesList;
        public Directory(String path) {
            this.path = path;

            audioList = new CopyOnWriteArrayList<>();
            innerDirectoriesList = new CopyOnWriteArrayList<>();
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

        public List<AudioFile> getAllMedia() {
            List<AudioFile> list = new ArrayList<>(audioList);

            for (Directory directory : innerDirectoriesList) {
                list.addAll(directory.getAllMedia());
            }

            return list;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Directory) {
                return this.path.toLowerCase().equals(((Directory) obj).path);
            } else return false;
        }
    }

    @SuppressWarnings("unused")
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

        public synchronized void addArtist(Artist artist) {
            if (!artists.contains(artist)) {
                artists.add(artist);
                //System.out.println(artist + " added.");
            }  //System.err.println(artist + " already added.");

        }

        public synchronized void addAlbum(Album album) {
            if (!albums.contains(album)) {
                albums.add(album);
                //System.out.println(album + " added.");
            }  //System.err.println(album + " already added.");

        }

        public static final class Artist {
            private final String name;
            private final ObservableList<Album> albums;
            private final ObservableList<AudioFile> singles;

            public Artist(String name) {
                this.name = name;
                albums = FXCollections.observableArrayList();
                singles = FXCollections.observableArrayList();
            }

            public ObservableList<Album> getAlbums() {
                return albums;
            }

            public ObservableList<AudioFile> getSingles() {
                return singles;
            }

            public String getName() {
                return name;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof Artist)) return false;
                return (((Artist) obj).getName().equals(this.name));
            }

            @Override
            public String toString() {
                return "[Artist]: " + name;
            }
        }
        public static final class Album {
            private final String name;
            private final Artist artist;
            private final ObservableList<AudioFile> songs;
            public Album(String name, Artist artist) {
                this.name = name;
                this.artist = artist;

                songs = FXCollections.observableArrayList();
            }

            public ObservableList<AudioFile> getSongs() {
                return songs;
            }

            public String getName() {
                return name;
            }

            public Artist getArtist() {
                return artist;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof Album)) return false;
                Album album = (Album) obj;
                return (album.getName().equals(this.name))
                        && album.getArtist().equals(this.artist);
            }

            @Override
            public String toString() {
                return "[Album]: " + name + " by " + artist.getName();
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