package com.frank.jmusicplayerfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class JMusicPlayerFX extends Application {

    @Override
    public void start(Stage mainStage) throws IOException {
        Pane parent = FXMLLoader.load(getClass().getResource("/resources/fxml/main_gui.fxml"));

        Scene scene = new Scene(parent);
        mainStage.setScene(scene);

        mainStage.setTitle("JMusicPlayerFX - Version Pre-Alpha");
        mainStage.setMinWidth(parent.getMinWidth());
        mainStage.setMinHeight(parent.getMinHeight() + 38);

        mainStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}