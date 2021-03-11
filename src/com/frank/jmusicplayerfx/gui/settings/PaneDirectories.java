package com.frank.jmusicplayerfx.gui.settings;

import com.frank.jmusicplayerfx.AudioLoader;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.MainGUI;
import com.frank.jmusicplayerfx.gui.element.settings.DirectoryElement;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public final class PaneDirectories {
    @FXML
    private Pane directoriesContainer;

    private MainGUI mainGUI;
    private AudioLoader loader;
    private Settings settings;

    @FXML
    private void initialize() {
        this.mainGUI = JMusicPlayerFX.getInstance().getMainGUI();
        this.loader = JMusicPlayerFX.getInstance().getAudioLoader();

        loader.getDirectories().addListener(this::updateDirectoriesEvent);
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    private void updateDirectoriesEvent(ListChangeListener.Change<? extends AudioLoader.Directory> change) {
        while (change.next()) {
            List<? extends AudioLoader.Directory> addedDirectories = change.getAddedSubList();
            List<? extends AudioLoader.Directory> removedDirectories = change.getRemoved();

            addedDirectories.forEach(added -> {
                DirectoryElement directoryElement = new DirectoryElement(added);

                if (!getDirectoryElements().contains(directoryElement)) {
                    directoriesContainer.getChildren().add(directoryElement);
                }
            });

            List<DirectoryElement> toRemove = getDirectoryElements().stream().filter(element -> removedDirectories.contains(element.getDirectory())).collect(Collectors.toList());

            directoriesContainer.getChildren().removeAll(toRemove);
        }
    }

    public List<DirectoryElement> getDirectoryElements() {
        synchronized (directoriesContainer.getChildren()) {
            return directoriesContainer.getChildren().stream().filter(node -> node instanceof DirectoryElement).map(node -> (DirectoryElement) node).collect(Collectors.toList());
        }
    }

    @FXML private void close() {
        mainGUI.closeSettings();
        returnToSettings();
    }

    @FXML private void returnToSettings() {
        settings.closeDirectoriesPane();
    }

    @FXML private void openDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File selectedDirectory = directoryChooser.showDialog(JMusicPlayerFX.getInstance().getMainStage());

        if (selectedDirectory != null) {
            AudioLoader.Directory newDirectory = loader.addNewDirectory(selectedDirectory);

            if (newDirectory != null) {
                loader.loadDirectoryMedia(newDirectory);
            }
        }
    }
}