package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.BackgroundTasker;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class MainGUI {
    @FXML private Pane leftMenu;
    @FXML private Pane leftMenuSections;
    @FXML private Button btnHideMenu;
    @FXML private Label lblTitleContent;
    @FXML private ScrollPane scrollpaneContent;

    private boolean leftMenuMinimize;
    private Timeline sizeTimeline;

    private final SVGPath buttonLeftSVG;
    private final SVGPath buttonRightSVG;

    private Parent musicPane;
    private final Task<Pane> musicPaneLoaderTask;

    public MainGUI() {
        buttonLeftSVG = new SVGPath();
        buttonRightSVG = new SVGPath();

        String leftPath = "M10.6,12.707a1,1,0,0,1,0-1.414l4.585-4.586a1," +
                "1,0,0,0-1.414-1.414L9.189,9.879a3,3,0,0,0,0,4.242l4.586," +
                "4.586a1,1,0,0,0,1.414-1.414Z";

        String rightPath = "M15.4,9.879,10.811,5.293A1,1,0,0,0,9.4,6.707l4.586," +
                "4.586a1,1,0,0,1,0,1.414L9.4,17.293a1,1,0,0,0,1.415," +
                "1.414L15.4,14.121A3,3,0,0,0,15.4,9.879Z";

        buttonLeftSVG.setContent(leftPath);
        buttonRightSVG.setContent(rightPath);

        musicPaneLoaderTask = new Task<>() {
            @Override
            protected Pane call() throws IOException {
                return FXMLLoader.load(getClass().getResource("/resources/fxml/songs_pane.fxml"));
            }
        };
    }

    @FXML private void initialize() {
        sizeTimeline = new Timeline();

        btnHideMenu.setShape(buttonLeftSVG);
        leftMenuMinimize = false;
        leftMenu.setPrefWidth(leftMenu.getMaxWidth());

        setContentView("Loading...", null);

        BackgroundTasker.executeTask(musicPaneLoaderTask, e -> {
            musicPane = musicPaneLoaderTask.getValue();
            setContentView(TITLE_SONGS, musicPane);
            selectFirstButton();
        });
    }

    @FXML private void minimizeLeftMenu() {
        KeyFrame frame;
        KeyValue value;
        Duration duration = Duration.millis(75);
        SVGPath shape;

        sizeTimeline.stop();
        sizeTimeline.getKeyFrames().clear();
        DoubleProperty writableValue = leftMenu.prefWidthProperty();

        if (leftMenuMinimize) {
            value = new KeyValue(writableValue, leftMenu.getMaxWidth());
            frame = new KeyFrame(duration, value);

            configureButtonsContentDisplay(ContentDisplay.LEFT);
            shape = buttonLeftSVG;

            leftMenuMinimize = false;
        } else {
            value = new KeyValue(writableValue, leftMenu.getMinWidth());
            frame = new KeyFrame(duration, e -> configureButtonsContentDisplay(ContentDisplay.GRAPHIC_ONLY) , value);
            shape = buttonRightSVG;
            leftMenuMinimize = true;
        }
        btnHideMenu.setShape(shape);
        sizeTimeline.getKeyFrames().add(frame);
        sizeTimeline.play();
    }

    private void configureButtonsContentDisplay(ContentDisplay contentDisplay) {
        for (Node node : leftMenuSections.getChildren()) {
            if (node instanceof Button) {
                ((Button) node).setContentDisplay(contentDisplay);
            }
        }
    }

    private List<Node> getButtons() {
        return leftMenuSections.getChildren();
    }

    @FXML private void openSection(ActionEvent evt) {
        if (evt.getSource() instanceof Button) {
            Button btn = (Button) evt.getSource();

            getButtons().forEach(node -> node.getStyleClass().removeAll("button_selected"));
            btn.getStyleClass().add("button_selected");

            switch (btn.getId()) {
                case "btn_songs"        -> setContentView(TITLE_SONGS, musicPane);
                case "btn_artists"      -> setContentView(TITLE_ARTISTS, null);
                case "btn_albums"       -> setContentView(TITLE_ALBUMS, null);
                case "btn_playlists"    -> setContentView(TITLE_PLAYLISTS, null);
                case "btn_favorites"    -> setContentView(TITLE_FAVORITES, null);
                default                 -> setContentView(TITLE_UNKNOWN, null);
            }
        }
    }

    private void selectFirstButton() {
        getButtons().stream()
            .filter(btn -> btn.getId().equals("btn_songs"))
            .findFirst().ifPresent(btn -> btn.getStyleClass().add("button_selected"));
    }

    private void setContentView(String title, Parent content) {
        lblTitleContent.setText(title);
        scrollpaneContent.setContent(content);
        scrollpaneContent.setFitToWidth(true);
        scrollpaneContent.setFitToHeight(true);
    }

    @FXML private void openAuthorLink() {
        JMusicPlayerFX.getInstance().getHostServices().showDocument("https://twitter.com/FrankGV42");
    }

    private final static String TITLE_SONGS     = "Songs";
    private final static String TITLE_ARTISTS   = "Artists";
    private final static String TITLE_ALBUMS    = "Albums";
    private final static String TITLE_PLAYLISTS = "Playlists";
    private final static String TITLE_FAVORITES = "Favorites";
    private final static String TITLE_UNKNOWN   = "UNKNOWN";
}