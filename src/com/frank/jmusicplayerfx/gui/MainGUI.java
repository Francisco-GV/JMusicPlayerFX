package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.BackgroundTasker;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGUI {
    @FXML private Pane leftMenu;
    @FXML private Pane leftMenuSections;
    @FXML private Label graphicBtnHideMenu;
    @FXML private Label lblTitleContent;
    @FXML private ScrollPane scrollPaneContainer;

    private boolean leftMenuMinimize;
    private Timeline sizeTimeline;

    private final SVGPath buttonLeftSVG;
    private final SVGPath buttonRightSVG;

    private List<Pane> panes;

    private final Map<String, Label> buttonLabelsMap = new HashMap<>();

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
    }

    @FXML private void initialize() {
        sizeTimeline = new Timeline();

        graphicBtnHideMenu.setShape(buttonLeftSVG);
        leftMenuMinimize = false;
        leftMenu.setPrefWidth(leftMenu.getMaxWidth());

        BackgroundTasker.executeInOtherThread(() -> Platform.runLater(() -> {
            panes = new ArrayList<>();
            try {
                panes.add(SONGS, (Pane) loadFXML("/resources/fxml/pane_songs.fxml"));
                panes.add(ARTISTS, (Pane) loadFXML("/resources/fxml/pane_artists.fxml"));
                panes.add(ALBUMS, null);
                panes.add(PLAYLISTS, null);
                panes.add(FAVORITES, null);

                setContent(SONGS);
                selectFirstButton();

                getSectionButtons().forEach(node -> {
                    HBox pane = (HBox) node;
                    pane.getChildren().forEach(children -> {
                        if (children.getStyleClass().contains("lbl_title")) {
                            buttonLabelsMap.put(node.getId(), (Label) children);
                        }
                    });
                });
            } catch(IOException ex) {
                ex.getStackTrace();
            }
        }));
    }

    @FXML private void minimizeLeftMenu() {
        KeyFrame frame;
        KeyValue value;
        Duration duration = Duration.millis(125);
        SVGPath shape;

        sizeTimeline.stop();
        sizeTimeline.getKeyFrames().clear();
        DoubleProperty writableValue = leftMenu.prefWidthProperty();

        if (leftMenuMinimize) {
            value = new KeyValue(writableValue, leftMenu.getMaxWidth());
            frame = new KeyFrame(duration, value);

            hideText(false);
            shape = buttonLeftSVG;

            leftMenuMinimize = false;
        } else {
            value = new KeyValue(writableValue, leftMenu.getMinWidth());
            frame = new KeyFrame(duration, e -> hideText(true) , value);
            shape = buttonRightSVG;
            leftMenuMinimize = true;
        }
        graphicBtnHideMenu.setShape(shape);
        sizeTimeline.getKeyFrames().add(frame);
        sizeTimeline.play();
    }

    private void hideText(boolean hide) {
        for (Node node : leftMenuSections.getChildren()) {
            HBox pane;
            if (node instanceof HBox) {
                pane = (HBox) node;

                Label label = buttonLabelsMap.get(pane.getId());
                if (hide) {
                    pane.getChildren().removeAll(label);
                } else {
                    if (!pane.getChildren().contains(label)) {
                        pane.getChildren().add(label);
                    }
                }
            }
        }
    }

    private List<Node> getSectionButtons() {
        return leftMenuSections.getChildren();
    }

    @FXML private void openSection(Event evt) {
        Node source = (Node) evt.getSource();

        String selected = "selected";
        getSectionButtons().forEach(node -> node.getStyleClass().removeAll(selected));
        source.getStyleClass().add(selected);

        switch (source.getId()) {
            case "btn_songs"     -> setContent(SONGS);
            case "btn_artists"   -> setContent(ARTISTS);
            case "btn_albums"    -> setContent(ALBUMS);
            case "btn_playlists" -> setContent(PLAYLISTS);
            case "btn_favorites" -> setContent(FAVORITES);
            default              -> setContent(UNKNOWN);
        }
    }

    private void selectFirstButton() {
        getSectionButtons().stream()
            .filter(btn -> btn.getId().equals("btn_songs"))
            .findFirst().ifPresent(btn -> btn.getStyleClass().add("selected"));
    }

    private void setContent(int index) {
        lblTitleContent.setText(TITLES[index]);
        Pane content = panes.get(index);

        scrollPaneContainer.setContent(content);
    }

    @FXML private void openAuthorLink() {
        JMusicPlayerFX.getInstance().getHostServices().showDocument("https://twitter.com/FrankGV42");
    }

    private Parent loadFXML(String path) throws IOException {
        return FXMLLoader.load(getClass().getResource(path));
    }

    private final static String[] TITLES = new String[] {
            "Songs", "Artists", "Albums", "Playlists", "Favorites", "Unknown"
    };

    private final static int SONGS = 0;
    private final static int ARTISTS = 1;
    private final static int ALBUMS = 2;
    private final static int PLAYLISTS = 3;
    private final static int FAVORITES = 4;
    private final static int UNKNOWN = 5;
}