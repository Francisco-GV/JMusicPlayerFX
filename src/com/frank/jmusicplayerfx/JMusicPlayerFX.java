package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.media.AudioLoader;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

public class JMusicPlayerFX extends Application {
    private final String APP_TITLE = "JMusicPlayerFX";

    private Stage mainStage;
    private AudioLoader audioLoader;
    private GlobalPlayer globalPlayer;
    private Task<Scene> mainGUILoaderTask;

    @Override
    public void init() {
        audioLoader = new AudioLoader();
        globalPlayer = new GlobalPlayer();

        File musicDirFile = new File(System.getProperty("user.home").concat("\\Music"));
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
        this.mainStage = mainStage;

        //mainStage.widthProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue));

        mainStage.addEventHandler(WindowEvent.WINDOW_SHOWN, windowEvent ->
                BackgroundTasker.executeGUITaskOnce(mainGUILoaderTask, workerStateEvent -> {
                    Scene scene = mainGUILoaderTask.getValue();
                    Pane pane = (Pane) scene.getRoot();
                    mainStage.setMinWidth(pane.getMinWidth() + 40);
                    mainStage.setMinHeight(pane.getMinHeight() + 40);

                    scene.setFill(Color.rgb(33, 33, 33));

                    mainStage.setScene(scene);
                }));

        BackgroundFill fill = new BackgroundFill(Color.rgb(20, 20, 20), null, null);

        FlowPane pane = new FlowPane();
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setBackground(new Background(fill));
        pane.setPadding(new Insets(10, 10, 10, 10));

        Label label = new Label("Loading...");
        label.setContentDisplay(ContentDisplay.TOP);
        label.setTextFill(Color.WHITE);

        pane.getChildren().add(label);
        Scene scene = new Scene(pane);

        final Image ICON24x24 = new Image(getClass()
                .getResourceAsStream("/resources/images/music_24x24.png"), 24, 24, true, true);
        final Image ICON16x16 = new Image(getClass()
                .getResourceAsStream("/resources/images/music_16x16.png"), 16, 16, true, true);

        mainStage.getIcons().addAll(ICON24x24, ICON16x16);

        mainStage.setTitle(APP_TITLE);
        mainStage.setScene(scene);

        mainStage.setWidth(975);
        mainStage.setHeight(650);
        mainStage.centerOnScreen();
        mainStage.show();
    }

    public void setTitle(String text) {
        mainStage.setTitle(text + "  |  " + APP_TITLE);
    }

    public void resetTitle() {
        mainStage.setTitle(APP_TITLE);
    }

    public AudioLoader getAudioLoader() {
        return audioLoader;
    }

    public GlobalPlayer getGlobalPlayer() {
        return globalPlayer;
    }

    private static JMusicPlayerFX instance;

    public static void main(String[] args) {
        Application.launch(args);
    }

    public static JMusicPlayerFX getInstance() {
        return instance;
    }
    /*
        To access te Application object created by JavaFX Runtime, I decided to use a singleton pattern, but
        this class must have a public constructor to create that object. So I will throw a exception to prevent
        further attempts to create an object. It's synchronized to prevent that many threads attempt to access
        to this constructor. Although due the way that JavaFX works, probably this never will be necessary.
        But, hey, it's ok make sure of that. Certainly this won't avoid the creation of new objects but throw
        the exception will avoid the override of the first object.
        .
        TL;DR: Weird shit that probably never will be necessary, just ignore it.
    */
    public JMusicPlayerFX() {
        synchronized (JMusicPlayerFX.class) {
            if (instance != null)
                throw new UnsupportedOperationException(getClass() +
                        " uses a singleton pattern and there's already an instance created.");
            instance = this;
        }
    }
}