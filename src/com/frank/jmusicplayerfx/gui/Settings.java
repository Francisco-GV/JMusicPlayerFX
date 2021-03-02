package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import javafx.fxml.FXML;
import org.controlsfx.control.ToggleSwitch;

public class Settings {

    @FXML private ToggleSwitch switchHideCollabs;

    private MainGUI mainGUI;

    @FXML private void initialize() {
        this.mainGUI = JMusicPlayerFX.getInstance().getMainGUI();

        switchHideCollabs.selectedProperty().addListener((obs, old, newValue) ->
                mainGUI.getPaneArtists().hideCollabs(newValue));
    }

    @FXML private void close() {
        mainGUI.closeSettings();
    }
}