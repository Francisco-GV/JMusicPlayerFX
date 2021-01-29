package com.frank.jmusicplayerfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
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

    public static void initSlider(Slider slider, double defaultValue,
                                  String backgroundColor, String progressColor) {
        slider.valueProperty().addListener(new ChangeListener<>() {
            private StackPane track;
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (track == null) {
                    findTrack();
                }
                double porcent = slider.getValue() * 100 / slider.getMax();
                String style = "-fx-background-color: linear-gradient(to right, " + progressColor + "  0%, " +
                        progressColor + " " + porcent + "%, " + backgroundColor + " " + porcent + "%, " +
                        backgroundColor + " 100%)";
                if (track != null) track.setStyle(style);
            }

            private void findTrack() {
                for (Node subnode : slider.getChildrenUnmodifiable()) {
                    if (subnode.getStyleClass().toString().equals("track")) {
                        track = (StackPane) subnode;
                        break;
                    }
                }
            }
        });
        slider.setValue(defaultValue);
    }
}