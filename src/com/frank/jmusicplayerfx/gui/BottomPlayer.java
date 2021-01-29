package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.GlobalPlayer;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import static com.frank.jmusicplayerfx.Util.formatTime;
import static com.frank.jmusicplayerfx.Util.initSlider;

public class BottomPlayer {
    private GlobalPlayer globalPlayer;
    private ObjectProperty<MediaPlayer> currentMediaPlayer;

    @FXML private Label lblCurrentTime;
    @FXML private Label lblTotalTime;
    @FXML private Slider sliderProgress;
    @FXML private Label graphicPlayButton;

    private final SVGPath playSVG;
    private final SVGPath pauseSVG;
    private Duration temporalDuration;

    public BottomPlayer() {
        playSVG = new SVGPath();
        pauseSVG = new SVGPath();

        String playPath = "M7,6.82 L7,17.18 C7,17.97 7.87,18.45 8.54," +
                "18.02 L16.68,12.84 C17.3,12.45 17.3,11.55 16.68," +
                "11.15 L8.54,5.98 C7.87,5.55 7,6.03 7,6.82 Z";

        String pausePath = "M8,19 C9.1,19 10,18.1 10,17 L10,7 C10,5.9 9.1," +
                "5 8,5 C6.9,5 6,5.9 6,7 L6,17 C6,18.1 6.9,19 8,19 Z M14," +
                "7 L14,17 C14,18.1 14.9,19 16,19 C17.1,19 18,18.1 18," +
                "17 L18,7 C18,5.9 17.1,5 16,5 C14.9,5 14,5.9 14,7 Z";

        playSVG.setContent(playPath);
        pauseSVG.setContent(pausePath);
    }

    @FXML private void initialize() {
        globalPlayer = JMusicPlayerFX.getInstance().getGlobalPlayer();
        currentMediaPlayer = globalPlayer.mediaPlayerProperty();

        showTimeLabels(false);

        globalPlayer.mediaPlayerProperty().addListener((playerObserver, oldPlayer, newPlayer) -> {
            if (newPlayer != null) {
                showTimeLabels(true);
                newPlayer.statusProperty().addListener((obs, oldStatus, newStatus) ->
                        graphicPlayButton.setShape(newStatus == Status.PLAYING
                    ? pauseSVG
                    : playSVG));
                Duration totalDuration = newPlayer.getTotalDuration();

                sliderProgress.setMin(0);
                sliderProgress.setMax(totalDuration.toSeconds());
                String totalTime = formatTime(totalDuration);
                lblTotalTime.setText(totalTime);

                newPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!sliderProgress.isPressed()) {
                        String currentTime = formatTime(newTime);
                        lblCurrentTime.setText(currentTime);
                        sliderProgress.setValue((int) newTime.toSeconds());
                    }
                });
            } else {
                showTimeLabels(false);
            }
        });

        sliderProgress.valueProperty().addListener((obs, old, newValue) -> {
            temporalDuration = new Duration(newValue.intValue() * 1000);
            if (sliderProgress.isPressed()) {
                String currentTime = formatTime(temporalDuration);
                lblCurrentTime.setText(currentTime);
            }
        });

        initSlider(sliderProgress, 0, "white", "#d03636");

        graphicPlayButton.setShape(playSVG);
    }

    private void showTimeLabels(boolean show) {
        lblCurrentTime.setVisible(show);
        lblTotalTime.setVisible(show);
        sliderProgress.setDisable(!show);
    }

    @FXML private void nextSong() {
        if (currentMediaPlayer.get() != null) {
            globalPlayer.next();
            globalPlayer.play();
        }
    }

    @FXML private void previousSong() {
        if (currentMediaPlayer.get() != null) {
            globalPlayer.previous();
            globalPlayer.play();
        }
    }

    @FXML private void play() {
        MediaPlayer player = currentMediaPlayer.get();
        if (player != null) {
            switch (player.getStatus()) {
                case PLAYING -> globalPlayer.pause();
                case PAUSED -> globalPlayer.play();
            }
        }
    }
}