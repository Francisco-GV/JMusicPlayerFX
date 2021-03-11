package com.frank.jmusicplayerfx;

import com.frank.jmusicplayerfx.gui.MainGUI;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.Loader;
import com.frank.jmusicplayerfx.util.Loader.FXMLObject;
import com.frank.jmusicplayerfx.util.Transition;
import com.frank.jmusicplayerfx.util.Util;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class JMusicPlayerFX extends Application {
    private final String APP_TITLE = "JMusicPlayerFX";
    private String APP_TITLE_DEBUG_TEXT;

    private Stage mainStage;
    private AudioLoader audioLoader;
    private GlobalPlayer globalPlayer;
    private MainGUI mainGUI;

    private List<String> argDirectories;

    @Override
    public void init() {
        audioLoader = new AudioLoader();
        globalPlayer = new GlobalPlayer();
    }

    @Override
    public void start(Stage mainStage) {
        List<String> args = getParameters().getRaw();

        if (!args.isEmpty()) {
            APP_TITLE_DEBUG_TEXT = args.get(0);
            if (args.size() > 1) {
                argDirectories = args.subList(1, args.size());
            }
        }

        this.mainStage = mainStage;

        final Image ICON24x24 = Util.getImage("/resources/images/music_24x24.png", 24, 24);
        final Image ICON16x16 = Util.getImage("/resources/images/music_16x16.png", 16, 16);
        mainStage.getIcons().addAll(ICON24x24, ICON16x16);

        Scene scene = new Scene(new Group());
        scene.setFill(Color.rgb(33, 33, 33));
        mainStage.setScene(scene);

        mainStage.setTitle(APP_TITLE + " - " + APP_TITLE_DEBUG_TEXT);
        mainStage.setWidth(975);
        mainStage.setHeight(650);
        mainStage.centerOnScreen();
        mainStage.show();

        prepareMainGUILoading();
    }

    public void prepareMainGUILoading() {
        Task<Pane> mainGUIScene = new Task<>() {
            @Override
            protected Pane call() throws IOException {
                FXMLObject<Pane, MainGUI> object = Loader.loadFXMLObject("/resources/fxml/main_gui.fxml");
                mainGUI = object.getController();
                return object.getRoot();
            }
        };

        BackgroundTasker.executeGUITaskOnce(mainGUIScene, event -> {
            Pane pane = mainGUIScene.getValue();
            Transition.fade(3000, 750, pane);

            mainStage.setMinWidth(pane.getMinWidth() + 40);
            mainStage.setMinHeight(pane.getMinHeight() + 40);

            Scene scene = new Scene(pane);
            scene.setFill(Color.rgb(33, 33, 33));

            mainStage.setScene(scene);

            BackgroundTasker.executeInOtherThread(() -> {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (argDirectories != null) {
                    audioLoader.addNewDirectories(argDirectories);
                }
                audioLoader.loadAllMedia();
            });
        });
    }

    public void setTitle(String text) {
        mainStage.setTitle(text + "  |  " + APP_TITLE + " - " + APP_TITLE_DEBUG_TEXT);
    }

    public void resetTitle() {
        mainStage.setTitle(APP_TITLE + " - " + APP_TITLE_DEBUG_TEXT);
    }

    public AudioLoader getAudioLoader() {
        return audioLoader;
    }

    public GlobalPlayer getGlobalPlayer() {
        return globalPlayer;
    }

    public MainGUI getMainGUI() {
        return mainGUI;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private static JMusicPlayerFX instance;

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