package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.BackgroundTasker;
import com.frank.jmusicplayerfx.GlobalPlayer;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.AudioLoader;
import com.frank.jmusicplayerfx.media.PlayList;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.List;
import java.util.TimerTask;

public class SongsPane {
    @FXML private TableView<AudioFile> musicTable;
    @FXML private TableColumn<AudioFile, String> titleColumn;
    @FXML private TableColumn<AudioFile, String> artistColumn;
    @FXML private TableColumn<AudioFile, String> albumColumn;
    @FXML private TableColumn<AudioFile, String> durationColumn;

    private AudioLoader audioLoader;
    private GlobalPlayer globalPlayer;
    private PlayList playList;

    @SuppressWarnings("FieldCanBeLocal")
    private TimerTask tableSorter;

    @FXML private void initialize() {
        playList = new PlayList("songs");

        tableSorter = new TimerTask(){
            @Override
            public void run() {
                updateSongs();
                Platform.runLater(() -> {
                    PlayList playerPlaylist = globalPlayer.getCurrentPlayList();

                    if (playerPlaylist != null) {
                        AudioFile audioFile = null;
                        if (!playerPlaylist.getAudioFiles().equals(playList.getAudioFiles())) {
                            audioFile = globalPlayer.currentAudioProperty().get();
                        }

                        musicTable.sort();

                        if (audioFile != null) {
                            int index = playList.getAudioFiles().indexOf(audioFile);
                            globalPlayer.setCurrentIndex(index);
                        }
                    } else {
                        musicTable.sort();
                    }
                });
            }
        };
        musicTable.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setReorderable(false);
        });
        audioLoader = JMusicPlayerFX.getInstance().getAudioLoader();
        globalPlayer = JMusicPlayerFX.getInstance().getGlobalPlayer();

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationFormated"));

        musicTable.setRowFactory(new Callback<>() {
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

            private void styleRow(TableRow<AudioFile> tableRow) {
                styleRow(tableRow, tableRow.getItem());
            }

            private void styleRow(TableRow<AudioFile> row, AudioFile rowFile) {
                AudioFile currentAudio = globalPlayer.currentAudioProperty().get();

                if (currentAudio == rowFile) {
                    row.getStyleClass().add("playing");
                } else {
                    row.getStyleClass().removeAll("playing");
                }
            }
        });

        playList.setAudioFiles(musicTable.getItems());
        musicTable.widthProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number old, Number newWidth) {
                albumColumn.setVisible(!(newWidth.doubleValue() < 600));
                durationColumn.setVisible(!(newWidth.doubleValue() < 450));
                List<TableColumn<AudioFile, ?>> columns = musicTable.getColumns();

                int num = (int) columns.stream().filter(TableColumn::isVisible).count();
                double width = newWidth.doubleValue();
                switch (num) {
                    case 4 -> {
                        titleColumn.setPrefWidth(getPercentWidth(width, 28));
                        artistColumn.setPrefWidth(getPercentWidth(width, 28));
                        albumColumn.setPrefWidth(getPercentWidth(width, 28));
                        durationColumn.setPrefWidth(getPercentWidth(width, 16));
                    }
                    case 3 -> {
                        titleColumn.setPrefWidth(getPercentWidth(width, 40));
                        artistColumn.setPrefWidth(getPercentWidth(width, 40));
                        durationColumn.setPrefWidth(getPercentWidth(width, 20));
                    }
                    default -> columns.forEach(col -> col.setPrefWidth(width / num));
                }
            }

            private double getPercentWidth(double width, double percent) {
                return width / 100.0 * percent;
            }
        });

        titleColumn.setSortType(TableColumn.SortType.ASCENDING);
        musicTable.getSortOrder().add(titleColumn);

        BackgroundTasker.executePeriodically(tableSorter, 0, 2500);
    }

    private void updateSongs() {
        audioLoader.getAllMedia().forEach(this::addToTable);
    }

    private void addToTable(AudioFile audioFile) {
        ObservableList<AudioFile> list = musicTable.getItems();
        if (!list.contains(audioFile)) {
            list.add(audioFile);
        }
    }

    private void selectFile(AudioFile audio) {
        globalPlayer.initNewAudio(playList, audio);
        globalPlayer.play();
    }
}