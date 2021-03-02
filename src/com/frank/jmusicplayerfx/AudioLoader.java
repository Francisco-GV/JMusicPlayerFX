package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class AudioLoader {
    private final ObservableList<Directory> directories;
    private final Data data;
    private final Timer loaderTimer;
    private final ObservableList<AudioFile> allAudioList;

    public AudioLoader() {
        data = new Data();
        loaderTimer = new Timer(true);
        allAudioList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
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

                dir.addListListeners(audioListListener, innerDirectoriesListener);
                dir.getInnerDirectoriesList().forEach(this::apply);
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

    public void addNewDirectories(List<String> paths) {
        for (String path : paths) {
            addNewDirectory(path);
        }
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

    public Data getInfo() {
        return data;
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
            BackgroundTasker.executeInOtherThread(() -> {
                File[] fileList = getAsFile().listFiles(extensionFilter);

                if (fileList != null) {
                    for (File file : fileList) {
                        AudioFile audioFile = new AudioFile(file);
                        audioList.add(audioFile);

                        try {
                            Thread.sleep((long) (Math.random() * 150));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                searchForInnerDirectories();
            });
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