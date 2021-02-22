package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.element.AlbumContentViewer;
import com.frank.jmusicplayerfx.gui.element.AlbumView;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.Loader;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
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

    @FXML private BorderPane guiRoot;
    @FXML private Pane leftMenu;
    @FXML private Pane leftMenuSections;
    @FXML private Label graphicBtnHideMenu;
    @FXML private Label lblTitleContent;

    @FXML private StackPane centerStackPane;
    @FXML private Pane centerDefaultPane;
    @FXML private ScrollPane scrollPaneContainer;

    private boolean leftMenuMinimize;
    private Timeline sizeTimeline;

    private final SVGPath buttonLeftSVG;
    private final SVGPath buttonRightSVG;

    private final List<Pane> panes;

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

        panes = new ArrayList<>(5);

        for (int i = 0; i < 5; i++) {
            panes.add(null);
        }
    }

    @FXML private void initialize() {
        albumContentViewer = new AlbumContentViewer(this);

        sizeTimeline = new Timeline();

        graphicBtnHideMenu.setShape(buttonLeftSVG);
        leftMenuMinimize = false;
        leftMenu.setPrefWidth(leftMenu.getMaxWidth());

        BackgroundTasker.executeInOtherThread(() -> Platform.runLater(() -> {
            addSection(SONGS, "/resources/fxml/pane_songs.fxml", () -> {
                setContent(SONGS);
                selectFirstButton();
            });
            addSection(ARTISTS, new PaneArtists());
            addSection(ALBUMS, new PaneAlbums());
            addSection(PLAYLISTS, (String) null);
            addSection(FAVORITES, (String) null);

            try {
                Loader.loadRoot("/resources/fxml/settings.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }

            centerStackPane.setViewOrder(5);
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
        resetCenterDefaultPane();

        lblTitleContent.setText(TITLES[index]);
        Pane content = panes.get(index);

        scrollPaneContainer.setContent(content);
    }

    public void setCenterPane(Pane pane) {
        centerStackPane.getChildren().setAll(pane);
    }

    public void resetCenterDefaultPane() {
        blurDefaultPane(false);
        setCenterPane(centerDefaultPane);
    }

    public void viewAlbumContent(AlbumView albumView) {
        albumContentViewer.showAlbumView(albumView);

        if (!centerStackPane.getChildren().contains(albumContentViewer)) {
            centerStackPane.getChildren().add(albumContentViewer);
            blurDefaultPane(true);
        }
    }

    public void blurDefaultPane(boolean doBlur) {
        Effect blur = null;
        if (doBlur) blur = new GaussianBlur(30);
        centerDefaultPane.setEffect(blur);
        centerStackPane.getChildren().get(0).setEffect(blur);
    }

    private void addSection(int index, String source, Runnable onFinish) {
        Task<Pane> paneTask = new Task<>() {
            @Override
            protected Pane call() throws Exception {
                return Loader.loadRoot(source);
            }
        };

        BackgroundTasker.executeGUITaskOnce(paneTask, event -> {
            Pane pane = paneTask.getValue();
            panes.set(index, pane);

            if (onFinish != null) onFinish.run();
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void addSection(int index, Pane pane) {
        BackgroundTasker.executeInOtherThread(() -> panes.set(index, pane));
    }

    @SuppressWarnings("SameParameterValue")
    private void addSection(int index, String source) {
        addSection(index, source, null);
    }

    @FXML private void openAuthorLink() {
        JMusicPlayerFX.getInstance().getHostServices().showDocument("https://twitter.com/FrankGV42");
    }

    @FXML private void openSettings() {
        Effect colorAdjust = new ColorAdjust(0, 0, -0.8, 0);

        guiRoot.setEffect(colorAdjust);
    }

    public StackPane getCenterStackPane() {
        return centerStackPane;
    }

    private final static String[] TITLES = new String[] {
            "Songs", "Artists", "Albums", "Playlists", "Favorites", "Unknown"
    };

    private static final int SONGS = 0;
    private static final int ARTISTS = 1;
    private static final int ALBUMS = 2;
    private static final int PLAYLISTS = 3;
    private static final int FAVORITES = 4;
    private static final int UNKNOWN = 5;
}