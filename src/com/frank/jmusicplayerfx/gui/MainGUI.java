package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.container.AlbumContentViewer;
import com.frank.jmusicplayerfx.gui.container.PaneAlbums;
import com.frank.jmusicplayerfx.gui.container.PaneArtists;
import com.frank.jmusicplayerfx.gui.element.album.AlbumView;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.Loader;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainGUI {
    @FXML private StackPane root;

    @FXML private Pane leftMenu;
    @FXML private Pane leftMenuSections;
    @FXML private Label graphicBtnHideMenu;

    @FXML private BorderPane guiRoot;

    private Pane paneSongs;
    private Pane paneArtists;
    private Pane paneAlbums;
    private Pane panePlaylists;
    private Pane paneFavorites;

    private Pane settingsPane;

    @FXML private StackPane centerStackPane;

    private boolean leftMenuMinimize;
    private Timeline sizeTimeline;

    private final SVGPath buttonLeftSVG;
    private final SVGPath buttonRightSVG;

    private AlbumContentViewer albumContentViewer;

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
        albumContentViewer = new AlbumContentViewer(this);

        sizeTimeline = new Timeline();

        graphicBtnHideMenu.setShape(buttonLeftSVG);
        leftMenuMinimize = false;
        leftMenu.setPrefWidth(leftMenu.getMaxWidth());

        BackgroundTasker.executeInOtherThread(() -> {
            try {
                paneSongs = Loader.loadRoot("/resources/fxml/pane_songs.fxml");
                setCenterPane(paneSongs);
                selectFirstButton();
            } catch (IOException e) {
                e.printStackTrace();
            }

            paneArtists = new PaneArtists();
            paneAlbums = new PaneAlbums();
            panePlaylists = null;
            paneFavorites = null;

            try {
                settingsPane = Loader.loadRoot("/resources/fxml/settings/settings.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }

            centerStackPane.setViewOrder(5);
        });
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

                Label label = (Label) pane.getChildren().get(1);
                if (hide) {
                    label.setVisible(false);
                    label.setManaged(false);
                } else {
                    label.setVisible(true);
                    label.setManaged(true);
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
            case "btn_songs"     -> setCenterContent(paneSongs);
            case "btn_artists"   -> setCenterContent(paneArtists);
            case "btn_albums"    -> setCenterContent(paneAlbums);
            case "btn_playlists" -> setCenterContent(panePlaylists);
            case "btn_favorites" -> setCenterContent(paneFavorites);
        }
    }

    private void selectFirstButton() {
        getSectionButtons().stream()
            .filter(btn -> btn.getId().equals("btn_songs"))
            .findFirst().ifPresent(btn -> btn.getStyleClass().add("selected"));
    }

    private void setCenterContent(Pane centerContent) {
        resetCenterDefaultPane();
        setCenterPane(centerContent);
    }

    private List<Node> lastCenterStackPanes;
    public void setCenterPane(Pane pane) {
        if (pane != null) {
            lastCenterStackPanes = new ArrayList<>(centerStackPane.getChildren());
            Platform.runLater(() -> centerStackPane.getChildren().setAll(pane));
        }
    }

    public void resetCenterDefaultPane() {
        blurDefaultPane(false, null);
        centerStackPane.getChildren().setAll(lastCenterStackPanes);
    }

    public void viewAlbumContent(AlbumView albumView) {
        albumContentViewer.showAlbumView(albumView);

        if (!centerStackPane.getChildren().contains(albumContentViewer)) {
            centerStackPane.getChildren().add(albumContentViewer);
            blurDefaultPane(true, albumContentViewer);
        }
    }

    public void blurDefaultPane(boolean doBlur, Node except) {
        Effect blur = null;
        if (doBlur) blur = new GaussianBlur(30);

        synchronized (centerStackPane.getChildrenUnmodifiable()) {
            Effect finalBlur = blur;
            centerStackPane.getChildrenUnmodifiable().stream()
                    .filter(node -> node != except)
                    .forEach(node -> node.setEffect(finalBlur));
        }
    }

    @FXML private void openAuthorLink() {
        JMusicPlayerFX.getInstance().getHostServices().showDocument("https://twitter.com/FrankGV42");
    }

    @FXML private void openSettings() {
        Effect colorAdjust = new GaussianBlur(30);
        guiRoot.setEffect(colorAdjust);

        if (!root.getChildren().contains(settingsPane)) {
            root.getChildren().add(settingsPane);
        }
    }

    public void closeSettings() {
        guiRoot.setEffect(null);

        root.getChildren().remove(settingsPane);
    }

    public StackPane getCenterStackPane() {
        return centerStackPane;
    }

    public PaneArtists getPaneArtists() {
        return (PaneArtists) paneArtists;
    }
}