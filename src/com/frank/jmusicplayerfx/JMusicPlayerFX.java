package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioLoader;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class JMusicPlayerFX extends Application {
    private static JMusicPlayerFX instance;
    private AudioLoader audioLoader;
    private Task<Scene> mainGUILoaderTask;

    @Override
    public void init() {
        instance = this;
        audioLoader = new AudioLoader();
        File musicDirFile = new File(System.getProperty("user.home").concat("\\Music\\Example"));
        audioLoader.addNewDirectory(musicDirFile);

        mainGUILoaderTask = new Task<>() {
            @Override
            protected Scene call() throws IOException {
                Parent parent = FXMLLoader.load(getClass().getResource("/resources/fxml/main_gui.fxml"));
                return new Scene(parent);
            }
        };
    }

    @Override
    public void start(Stage mainStage) {
        mainStage.addEventHandler(WindowEvent.WINDOW_SHOWING,
            windowEvent -> BackgroundTasker.executeTask(mainGUILoaderTask,
                    workerStateEvent -> {
                Scene scene = mainGUILoaderTask.getValue();
                Pane pane = (Pane) scene.getRoot();
                mainStage.setMinWidth(pane.getMinWidth());
                mainStage.setMinHeight(pane.getMinHeight() + 38);

                scene.setFill(Color.rgb(33, 33, 33));

                mainStage.setScene(scene);
            })
        );

        BackgroundFill fill = new BackgroundFill(Color.rgb(20, 20, 20), null, null);

        FlowPane pane = new FlowPane();
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setBackground(new Background(fill));

        Image imgLoad = new Image(getClass().getResourceAsStream("/resources/images/loading.gif"), 25, 25, true, true);
        Label label = new Label("Loading...", new ImageView(imgLoad));
        label.setContentDisplay(ContentDisplay.TOP);
        label.setTextFill(Color.WHITE);

        pane.getChildren().add(label);
        Scene scene = new Scene(pane);

        mainStage.setTitle("JMusicPlayerFX - Version Pre-Alpha");
        mainStage.setWidth(875);
        mainStage.setHeight(580);
        mainStage.setScene(scene);
        mainStage.show();
        mainStage.centerOnScreen();
    }

    public AudioLoader getAudioLoader() {
        return audioLoader;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    public static JMusicPlayerFX getInstance() {
        return instance;
    }
}