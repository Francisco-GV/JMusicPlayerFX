package com.frank.jmusicplayerfx.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class MainGUI {
    @FXML private Pane leftMenu;
    @FXML private Pane leftMenuSections;
    @FXML private Button btnHideMenu;

    private boolean leftMenuMinimize;
    private Timeline sizeTimeline;
    private final String buttonMenuLeftPath = "M10.6,12.707a1,1,0,0,1,0-1.414l4.585-4.586a1," +
            "1,0,0,0-1.414-1.414L9.189,9.879a3,3,0,0,0,0,4.242l4.586," +
            "4.586a1,1,0,0,0,1.414-1.414Z";

    private final String buttonMenuRightPath = "M15.4,9.879,10.811,5.293A1,1,0,0,0,9.4,6.707l4.586," +
            "4.586a1,1,0,0,1,0,1.414L9.4,17.293a1,1,0,0,0,1.415," +
            "1.414L15.4,14.121A3,3,0,0,0,15.4,9.879Z";

    @FXML private void initialize() {
        sizeTimeline = new Timeline();

        SVGPath leftShape = new SVGPath();
        leftShape.setContent(buttonMenuLeftPath);
        btnHideMenu.setShape(leftShape);

        leftMenuMinimize = false;
        leftMenu.setPrefWidth(leftMenu.getMaxWidth());
    }

    @FXML private void minimizeLeftMenu() {
        KeyFrame frame;
        KeyValue value;
        Duration duration = Duration.millis(100);
        SVGPath buttonShape = new SVGPath();

        sizeTimeline.stop();
        sizeTimeline.getKeyFrames().clear();
        DoubleProperty writableValue = leftMenu.prefWidthProperty();

        if (leftMenuMinimize) {
            value = new KeyValue(writableValue, leftMenu.getMaxWidth());
            frame = new KeyFrame(duration, value);

            configureButtonsContentDisplay(ContentDisplay.LEFT);
            buttonShape.setContent(buttonMenuLeftPath);

            leftMenuMinimize = false;
        } else {
            value = new KeyValue(writableValue, leftMenu.getMinWidth());
            frame = new KeyFrame(duration, e -> configureButtonsContentDisplay(ContentDisplay.GRAPHIC_ONLY) , value);

            buttonShape.setContent(buttonMenuRightPath);

            leftMenuMinimize = true;
        }
        btnHideMenu.setShape(buttonShape);
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

    @FXML private void openSection(ActionEvent evt) {
        if (evt.getSource() instanceof Button) {
            Button btn = (Button) evt.getSource();
            System.out.println(btn.getId());

            switch (btn.getId()) {
                case "btn_songs":
                    break;
                case "btn_artists":
                    break;
                case "btn_albums":
                    break;
                case "btn_playlists":
                    break;
                case "btn_favorites":
                    break;
            }
        }
    }
}