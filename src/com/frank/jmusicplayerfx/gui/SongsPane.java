package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.AudioLoader;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SongsPane {
    @FXML private TableView<AudioFile> musicTable;
    @FXML private TableColumn<AudioFile, String> titleColumn;
    @FXML private TableColumn<AudioFile, String> artistColumn;
    @FXML private TableColumn<AudioFile, String> albumColumn;
    @FXML private TableColumn<AudioFile, String> durationColumn;

    private AudioLoader audioLoader;

    @FXML private void initialize() {

        musicTable.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setReorderable(false);
        });

        audioLoader = JMusicPlayerFX.getInstance().getAudioLoader();

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationFormated"));

        musicTable.setRowFactory(table -> {
            TableRow<AudioFile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    selectFile(row.getItem(), event);
                }
            });

            row.selectedProperty().addListener((obs, old, isSelected) -> {
                if (isSelected) {
                    Platform.runLater(row::requestFocus);
                }
            });

            row.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    selectFile(row.getItem(), event);
                }
            });
            return row;
        });


        musicTable.widthProperty().addListener((obs, old, newWidth) -> {
            albumColumn.setVisible(!(newWidth.doubleValue() < 600));

            List<TableColumn<AudioFile, ?>> columns = musicTable.getColumns();
            int i = (int) columns.stream().filter(TableColumn::isVisible).count();
            columns.forEach(column -> column.setPrefWidth(musicTable.getWidth() / i));
        });

        titleColumn.setSortType(TableColumn.SortType.ASCENDING);
        musicTable.getSortOrder().add(titleColumn);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updateSongs();
            }
        }, 0, 2000);
    }

    private void updateSongs() {
        audioLoader.getAllMedia().forEach(this::addToTable);
    }

    private void addToTable(AudioFile audioFile) {
        ObservableList<AudioFile> list = musicTable.getItems();
        if (!list.contains(audioFile)) {
            list.add(audioFile);
            musicTable.sort();
        }
    }

    private void selectFile(AudioFile file, Event event) {
        System.out.println(event.getEventType().getName()
                + ": " + file.getTitle() + " - " + file.getArtist());
    }
}