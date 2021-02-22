package com.frank.jmusicplayerfx.gui.element;

import com.frank.jmusicplayerfx.AudioLoader.Info.Album;
import com.frank.jmusicplayerfx.media.AudioFile;
import com.frank.jmusicplayerfx.media.Playlist;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.ImageScaler;
import com.frank.jmusicplayerfx.util.Util;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Rectangle;

@SuppressWarnings("unused")
public class AlbumView {
    @FXML private Pane root;

    @FXML private HBox coverContainer;
    @FXML private Label lblCover;
    @FXML private Label lblAlbumName;
    @FXML private Label lblArtistName;
    @FXML private Label lblYear;
    @FXML private Label lblNSongs;
    @FXML private Label lblDuration;

    @FXML private TableView<AudioFile> tableSongs;
    @FXML private TableColumn<AudioFile, String> numberColumn;
    @FXML private TableColumn<AudioFile, String> titleColumn;
    @FXML private TableColumn<AudioFile, String> durationColumn;

    private final Album album;

    private LinearGradient gradient;

    public AlbumView(Album album) {
        this.album = album;
    }

    @FXML private void initialize() {
        setCoverListener();

        lblAlbumName.setText(album.getName());
        lblArtistName.setText(album.getAlbumArtist().getName());
        lblYear.textProperty().bind(album.yearProperty().asString());
        lblNSongs.textProperty().bind(album.getSongs().sizeProperty().asString("%d songs"));
        lblDuration.textProperty().bind(album.totalDurationFormattedProperty());

        numberColumn.setCellValueFactory(cellData -> cellData.getValue().trackNumberProperty().asString("%d.-"));
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationFormattedProperty());

        numberColumn.setStyle("-fx-alignment: center-right");
        titleColumn.setStyle("-fx-alignment: center-left");
        durationColumn.setStyle("-fx-alignment: center-left");

        tableSongs.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() <= 275) {
                titleColumn.setPrefWidth(newValue.doubleValue() - 60);
                durationColumn.setVisible(false);
            } else {
                titleColumn.setPrefWidth(newValue.doubleValue() - 100);
                durationColumn.setVisible(true);
            }
        });

        tableSongs.getColumns().forEach(column -> column.setReorderable(false));
        numberColumn.setSortType(TableColumn.SortType.ASCENDING);
        tableSongs.getSortOrder().add(numberColumn);
        Util.initMusicTable(new Playlist(album.getName(), album.getSongs().get()), tableSongs);
    }

    public void setCoverListener() {
        album.coverProperty().addListener((obs, old, newCover) -> showCover(newCover != null));
        showCover(album.coverProperty().get() != null);
    }

    public void showCover(boolean show) {
        ObservableList<String> classes = lblCover.getStyleClass();
        if (show) {
            classes.removeAll("default_picture");
            classes.add("picture");

            coverContainer.setBorder(null);

            Task<Image> imageTask = new Task<>() {
                @Override
                protected Image call() {
                    return ImageScaler.scaleImage(album.coverProperty().get(), 180, 180);
                }
            };

            BackgroundTasker.executeGUITaskOnce(imageTask, evt -> {
                ImagePattern imgPattern = new ImagePattern(imageTask.getValue());

                Rectangle rectCover = new Rectangle(180, 180);
                rectCover.setFill(imgPattern);
                rectCover.setEffect(new DropShadow(10, 3, 3, Color.grayRgb(15)));

                lblCover.setBackground(null);
                lblCover.setGraphic(rectCover);
            });
        } else {
            classes.removeAll("picture");
            classes.add("default_picture");

            BorderStroke borderStroke = new BorderStroke(gradient, BorderStrokeStyle.SOLID,
                    null, new BorderWidths(2));
            coverContainer.setBorder(new Border(borderStroke));

            lblCover.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        }
    }

    public Pane getRoot() {
        return root;
    }

    public void setGradient(LinearGradient gradient) {
        this.gradient = gradient;
    }
}