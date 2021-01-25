package com.frank.jmusicplayerfx.media;

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

    private static final FilenameFilter extensionFilter;
    private static final FileFilter directoryFilter;
    private final Timer loaderTimer;

    static {
        extensionFilter = (dir, name) -> {
            for (String extension : AudioFile.EXTENSIONS) {
                if (name.toLowerCase().endsWith(extension.toLowerCase())) {
                    return true;
                }
            }
            return false;
        };

        directoryFilter = File::isDirectory;
    }

    public AudioLoader() {
        directories = new ArrayList<>();
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

    public static class Directory {
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
                    audioList.add(new AudioFile(file));
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

        public List<AudioFile> getAudioList() {
            return audioList;
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
}