package com.frank.jmusicplayerfx.gui.element;

import com.frank.jmusicplayerfx.AudioLoader.Info.Album;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.ImageScaler;
import com.frank.jmusicplayerfx.util.Util;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Rectangle;

import static com.frank.jmusicplayerfx.JMusicPlayerFX.getInstance;

public class AlbumElement extends VBox {
    private final Album album;

    private final FlowPane pictureContainer;
    private final Label lblPicture;
    private final Label lblName;
    private final Label lblArtist;
    private final Label lblYear;

    private final LinearGradient gradient;

    private final int borderRadius;

    public AlbumElement(Album album) {
        this.album = album;
        pictureContainer = new FlowPane();
        lblPicture = new Label();
        lblName = new Label();
        lblArtist = new Label();
        lblYear = new Label();

        borderRadius = 20;
        gradient = Util.randomGradient();

        initializeElement();
    }

    public void setAlbumView(AlbumView albumView) {
        this.setOnMouseReleased(e -> getInstance().getMainGUI().viewAlbumContent(albumView));
    }

    private void initializeElement() {
        setId("album_element");
        getStylesheets().add("/resources/css/albums/album_element_style.css");

        pictureContainer.setId("picture_container");
        pictureContainer.setAlignment(Pos.CENTER);
        pictureContainer.getChildren().add(lblPicture);

        lblName.setId("lbl_name");
        lblName.setText(album.getName());
        lblName.setPadding(new Insets(10, 0, 0, 0));

        lblYear.setId("lbl_year");
        lblYear.setText(album.yearProperty().getValue().toString());

        lblArtist.setId("lbl_artist");
        lblArtist.setText(album.getAlbumArtist().getName());

        setCoverListener();

        this.getChildren().addAll(pictureContainer, lblName, lblYear, lblArtist);
    }

    public void setCoverListener() {
        album.coverProperty().addListener((obs, old, newCover) -> showCover(newCover != null));
        showCover(album.coverProperty().get() != null);
    }

    public void showCover(boolean show) {
        ObservableList<String> classes = lblPicture.getStyleClass();
        if (show) {
            classes.removeAll("default_picture");
            classes.add("picture");

            pictureContainer.setBorder(null);

            Rectangle rectangle = new Rectangle(125, 125);
            rectangle.setFill(new ImagePattern(album.coverProperty().get(), 0, 0, 125, 125, true));
            lblPicture.setGraphic(rectangle);

            Task<Image> imageTask = new Task<>() {
                @Override
                protected Image call() {
                    return ImageScaler.scaleImage(album.coverProperty().get(), 125, 125);
                }
            };

            BackgroundTasker.executeGUITaskOnce(imageTask, evt -> {
                ImagePattern imgPattern = new ImagePattern(imageTask.getValue());

                Rectangle rectCover = new Rectangle(125, 125);
                rectCover.setArcHeight(borderRadius);
                rectCover.setArcWidth(borderRadius);
                rectCover.setFill(imgPattern);
                rectCover.setEffect(new DropShadow(10, 3, 3, Color.grayRgb(15)));

                lblPicture.setBackground(null);
                lblPicture.setGraphic(rectCover);
            });
        } else {
            classes.removeAll("picture");
            classes.add("default_picture");

            BorderStroke borderStroke = new BorderStroke(gradient, BorderStrokeStyle.SOLID,
                    new CornerRadii(borderRadius, false), new BorderWidths(1));
            pictureContainer.setBorder(new Border(borderStroke));

            lblPicture.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        }
    }

    public Label getLblName() {
        return lblName;
    }

    public Album getAlbum() {
        return album;
    }

    public Label getLblArtist() {
        return lblArtist;
    }

    public Label getLblYear() {
        return lblYear;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AlbumElement)) return false;

        Album objAlbum = ((AlbumElement) obj).getAlbum();

        return objAlbum.equals(this.getAlbum());
    }

    public LinearGradient getGradient() {
        return gradient;
    }
}