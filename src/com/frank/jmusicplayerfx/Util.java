package com.frank.jmusicplayerfx;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class Util {
    public static String formatTime(Duration duration) {
        double totalSeconds = duration.toSeconds();
        int minutes = (int) totalSeconds / 60;
        int seconds = (int) totalSeconds % 60;

        String stringMinutes = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
        String stringSeconds = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);

        return stringMinutes + ":" + stringSeconds;
    }

    public static void initSlider(Slider slider, double defaultValue, String backgroundColor, String progressColor) {
        ObservableList<Node> childs = slider.getChildrenUnmodifiable();

        childs.addListener(new ListChangeListener<>() {
            private StackPane track;
            private boolean styleApplied;

            @Override
            public void onChanged(Change<? extends Node> change) {
                if (track == null && !styleApplied) {
                    findTrack();

                    if (track != null) {
                        setListener();
                    }
                } else if (!styleApplied) {
                    setListener();
                } else {
                    childs.removeListener(this);
                }
            }

            private void setListener() {
                styleApplied = true;
                slider.valueProperty().addListener((observable, oldValue, newValue) -> {
                    double porcent = newValue.doubleValue() * 100 / slider.getMax();

                    if (porcent >= 0 && porcent <= 100) {
                        String style = "-fx-background-color: linear-gradient(to right, "
                                + progressColor + " " + porcent + "%, "
                                + backgroundColor + " " + porcent + "%)";
                        track.setStyle(style);
                    }
                });

                slider.valueProperty().set(defaultValue);
            }

            private void findTrack() {
                for (Node subnode : childs) {
                    if (subnode.getStyleClass().toString().equals("track")) {
                        track = (StackPane) subnode;
                        break;
                    }
                }
            }
        });
    }

    public static void initProgresiveButton(Button button, DoubleProperty volume) {
        volume.addListener((observable, oldValue, newValue) -> {
            double currentPorcent = 50.0 + (volume.doubleValue() * 100) / 2;
            String style = "-fx-background-color: linear-gradient(to right, "
                    + "white" + " " + currentPorcent + "%, "
                    + "rgba(127, 127, 127, 0.25)" + " " + currentPorcent + "%)";

            button.setStyle(style);
        });
    }

    // Colors blend with grey as the saturation decreases and become more washed out depending on how low the saturation gets.
    // Colors blend with white - and become more "pastel" as lightness increases above 50%.
    public static Color randomPastelColor() {
        double hue = Math.random() * 360;
        double saturation = (Math.random() * 70.0 + 25) / 100.0;
        double brightness = (Math.random() * 10.0 + 85) / 100.0;

        return Color.hsb(hue, saturation, brightness);
    }
}