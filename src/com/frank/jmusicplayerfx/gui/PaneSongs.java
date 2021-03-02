package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.AudioLoader;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.element.extra.NoFoundPane;
import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.Playlist;
import com.frank.jmusicplayerfx.util.Util;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;
import java.util.List;

public class PaneSongs {
    @FXML private TableView<AudioFile> musicTable;
    @FXML private TableColumn<AudioFile, String> titleColumn;
    @FXML private TableColumn<AudioFile, String> artistColumn;
    @FXML private TableColumn<AudioFile, String> albumColumn;
    @FXML private TableColumn<AudioFile, String> durationColumn;

    @FXML private void initialize() {
        musicTable.setPlaceholder(new NoFoundPane("songs"));

        AudioLoader audioLoader = JMusicPlayerFX.getInstance().getAudioLoader();

        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        albumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationFormattedProperty());

        musicTable.getColumns().forEach(col -> col.setStyle("-fx-alignment: center-left;"));
        titleColumn.setStyle(titleColumn.getStyle() + "-fx-padding: 0 0 0 15px;");

        titleColumn.setSortType(TableColumn.SortType.ASCENDING);
        musicTable.getSortOrder().add(titleColumn);

        SortedList<AudioFile> sortedAllAudioFiles = new SortedList<>(audioLoader.getAllMediaList());
        sortedAllAudioFiles.setComparator(Comparator.comparing(AudioFile::getTitle));

        Util.initMusicTable(new Playlist("All songs", sortedAllAudioFiles), musicTable);

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
                        titleColumn.setPrefWidth(getPercentWidth(width, 32));
                        artistColumn.setPrefWidth(getPercentWidth(width, 30));
                        albumColumn.setPrefWidth(getPercentWidth(width, 30));
                        durationColumn.setPrefWidth(getPercentWidth(width, 20));
                    }
                    case 3 -> {
                        titleColumn.setPrefWidth(getPercentWidth(width, 45));
                        artistColumn.setPrefWidth(getPercentWidth(width, 40));
                        durationColumn.setPrefWidth(getPercentWidth(width, 15));
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