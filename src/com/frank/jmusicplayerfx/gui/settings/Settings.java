package com.frank.jmusicplayerfx.gui.settings;

import com.frank.jmusicplayerfx.AudioLoader;
import com.frank.jmusicplayerfx.AudioLoader.Directory;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.MainGUI;
import com.frank.jmusicplayerfx.gui.element.extra.NoFoundPane;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.Loader;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;

import java.io.IOException;

public class Settings {
    @FXML private StackPane settingsStackPane;
    @FXML private GridPane rootContainer;
    @FXML private VBox root;
    @FXML private ToggleSwitch switchHideCollabs;
    @FXML private ListView<Directory> directoriesList;

    private Pane paneDirectories;

    private MainGUI mainGUI;

    @FXML private void initialize() {
        this.mainGUI = JMusicPlayerFX.getInstance().getMainGUI();
        AudioLoader loader = JMusicPlayerFX.getInstance().getAudioLoader();

        directoriesList.setPlaceholder(new NoFoundPane("directories"));
        directoriesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Directory directory, boolean empty) {
                super.updateItem(directory, empty);

                setText(!empty ? directory.getPath() : null);
            }
        });

        directoriesList.prefHeightProperty().bind(Bindings.size(loader.getDirectories()).multiply(26));
        directoriesList.setItems(loader.getDirectories());

        switchHideCollabs.selectedProperty().addListener((obs, old, newValue) ->
                mainGUI.getPaneArtists().hideCollabs(newValue));


        Task<Loader.FXMLObject<Pane, PaneDirectories>> paneDirectoriesTask = new Task<>() {
            @Override
            protected Loader.FXMLObject<Pane, PaneDirectories> call() throws IOException {
                return Loader.loadFXMLObject("/resources/fxml/settings/pane_directories.fxml");

            }
        };

        BackgroundTasker.executeGUITaskOnce(paneDirectoriesTask, e -> {
            var object = paneDirectoriesTask.getValue();
            object.getController().setSettings(this);
            paneDirectories = object.getRoot();
        });
    }

    @FXML private void close() {
        mainGUI.closeSettings();
    }

    @FXML private void openDirectoriesPane() {
        settingsStackPane.getChildren().add(paneDirectories);
    }

    public void closeDirectoriesPane() {
        settingsStackPane.getChildren().remove(paneDirectories);
    }

}