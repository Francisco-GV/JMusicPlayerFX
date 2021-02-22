package com.frank.jmusicplayerfx.util;

import com.frank.jmusicplayerfx.GlobalPlayer;
import com.frank.jmusicplayerfx.media.AudioFile;
import static com.frank.jmusicplayerfx.JMusicPlayerFX.getInstance;

import com.frank.jmusicplayerfx.media.Playlist;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class Util {
    public static String formatTime(Duration duration) {
        double totalSeconds = duration.toSeconds();
        int minutes = (int) totalSeconds / 60;
        int seconds = (int) totalSeconds % 60;

        String stringMinutes = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
        String stringSeconds = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);

        return stringMinutes + ":" + stringSeconds;
    }

    public static Image getImage(String path, double width, double height) {
        return new Image(Util.class.getResourceAsStream(path), width, height, true, true);
    }

    public static void initMusicTable(Playlist playlist, TableView<AudioFile> tableView) {
        tableView.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setReorderable(false);
        });

        tableView.setRowFactory(new Callback<>() {
            private final GlobalPlayer globalPlayer = getInstance().getGlobalPlayer();

            @Override
            public TableRow<AudioFile> call(TableView<AudioFile> param) {
                TableRow<AudioFile> row = new TableRow<>();

                row.itemProperty()
                        .addListener((obs, old, newItem) -> styleRow(row));

                globalPlayer.currentAudioProperty()
                        .addListener((obs, old, newAudio) -> styleRow(row));

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        selectFile(row.getItem());
                    }
                });
                row.selectedProperty().addListener((obs, old, isSelected) -> {
                    if (isSelected) {
                        Platform.runLater(row::requestFocus);
                    }
                });
                row.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        selectFile(row.getItem());
                    }
                });
                return row;
            }

            private void styleRow(TableRow<AudioFile> row) {
                AudioFile currentAudio = globalPlayer.currentAudioProperty().get();

                if (currentAudio == row.getItem()) {
                    row.getStyleClass().add("playing");
                } else {
                    row.getStyleClass().removeAll("playing");
                }
            }

            private void selectFile(AudioFile audioFile) {
                globalPlayer.initNewAudio(playlist, audioFile, globalPlayer::play);
            }
        });

        SortedList<AudioFile> sortedList = new SortedList<>(playlist.getAudioFiles());
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
        tableView.sort();
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

    public static LinearGradient randomGradient() {
        Color color1 = Util.randomPastelColor();
        Color color2 = Util.randomPastelColor();

        Stop[] stops = new Stop[] {
                new Stop(0, color1), new Stop(1, color2)
        };

        return createGradient(0, 0, 1, 1, stops);
    }

    public static LinearGradient createGradient(int startX, int startY, int endX, int endY, Stop... stops) {
        return new LinearGradient(startX, startY, endX, endY, true, CycleMethod.NO_CYCLE, stops);
    }

    public static LinearGradient createGradient(int startX, int startY, int endX, int endY, List<Stop> stops) {
        return new LinearGradient(startX, startY, endX, endY, true, CycleMethod.NO_CYCLE, stops);
    }

    public static LinearGradient invertGradientDarker(LinearGradient gradient) {
        List<Stop> newStops = new ArrayList<>();
        int size = gradient.getStops().size();
        for (int i = 0; i < size; i++) {
            Stop stop = gradient.getStops().get(i);
            newStops.add(new Stop(i, stop.getColor().deriveColor(0, 1.0, 0.25, 1.0)));
        }

        return createGradient(1, 1, 0, 0, newStops);
    }
}