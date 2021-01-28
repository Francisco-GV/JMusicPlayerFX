package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.BackgroundTasker;
import com.frank.jmusicplayerfx.GlobalPlayer;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.AudioLoader;
import com.frank.jmusicplayerfx.media.PlayList;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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

        musicTable.setRowFactory(table -> {
            TableRow<AudioFile> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                globalPlayer.currentAudioProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == row.getItem()) {
                        row.getStyleClass().add("playing");
                    } else {
                        row.getStyleClass().removeAll("playing");
                    }
                });

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
        });

        playList.setAudioFiles(musicTable.getItems());
        musicTable.widthProperty().addListener((obs, old, newWidth) -> {
            albumColumn.setVisible(!(newWidth.doubleValue() < 600));
            List<TableColumn<AudioFile, ?>> columns = musicTable.getColumns();

            int i = (int) columns.stream().filter(TableColumn::isVisible).count();
            columns.forEach(column -> column.setPrefWidth(musicTable.getWidth() / i));
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
        globalPlayer.setCurrentPlayList(playList);
        int index = musicTable.getItems().indexOf(audio);
        globalPlayer.initNewAudio(index);
    }
}