package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.AudioLoader;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.Playlist;
import com.frank.jmusicplayerfx.util.Util;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class PaneSongs {
    @FXML private TableView<AudioFile> musicTable;
    @FXML private TableColumn<AudioFile, String> titleColumn;
    @FXML private TableColumn<AudioFile, String> artistColumn;
    @FXML private TableColumn<AudioFile, String> albumColumn;
    @FXML private TableColumn<AudioFile, String> durationColumn;

    @FXML private void initialize() {
        AudioLoader audioLoader = JMusicPlayerFX.getInstance().getAudioLoader();

        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        albumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationFormattedProperty());

        ObservableList<AudioFile> allAudioFiles = audioLoader.getAllMediaList();

        titleColumn.setSortType(TableColumn.SortType.ASCENDING);
        musicTable.getSortOrder().add(titleColumn);

        Util.initMusicTable(new Playlist("All songs", allAudioFiles), musicTable);

        musicTable.widthProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number old, Number newWidth) {
                albumColumn.setVisible(!(newWidth.doubleValue() <= 600));
                durationColumn.setVisible(!(newWidth.doubleValue() <= 450));
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
    }
}