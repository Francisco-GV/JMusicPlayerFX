package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.Data.Album;
import com.frank.jmusicplayerfx.Data.Artist;
import com.frank.jmusicplayerfx.gui.element.album.AlbumElement;
import com.frank.jmusicplayerfx.gui.element.album.AlbumView;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.ImageScaler;
import com.frank.jmusicplayerfx.util.Loader;
import com.frank.jmusicplayerfx.util.Util;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.frank.jmusicplayerfx.JMusicPlayerFX.getInstance;

@SuppressWarnings("unused")
public class ArtistContentViewer {
    @FXML private Pane topPane;
    @FXML private Button btnReturn;
    @FXML private Label lblArtistName;
    @FXML private Label lblArtistPicture;
    @FXML private Pane picturePane;

    @FXML private Pane albumContainer;
    @FXML private Pane epContainer;
    @FXML private Pane singleContainer;

    @FXML private Pane albumsPane;
    @FXML private Pane epsPane;
    @FXML private Pane singlesPane;

    private final Artist artist;

    private LinearGradient gradient;

    public ArtistContentViewer(Artist artist) {
        this.artist = artist;
    }

    @FXML private void initialize() {
        addPaneListener(albumsPane, albumContainer);
        addPaneListener(epsPane, epContainer);
        addPaneListener(singlesPane, singleContainer);

        btnReturn.setOnAction(e -> backDefaultPane());
        lblArtistName.setText(artist.getName());
        setPictureListener();
        addAlbums();
        artist.getAlbums().addListener(this::updateArtistContentEvent);
    }

    private void updateArtistContentEvent(ListChangeListener.Change<? extends Album> change) {
        while (change.next()) {
            List<? extends Album> addedAlbums = change.getAddedSubList();
            List<? extends Album> removedAlbums = change.getRemoved();

            addedAlbums.forEach(added -> {
                AlbumElement albumElement = new AlbumElement(added);
                addAlbumElement(albumElement);
            });

            List<AlbumElement> toRemove = getAllElements().stream()
                    .filter(element -> removedAlbums.contains(element.getAlbum()))
                    .collect(Collectors.toList());

            removeAlbumElements(toRemove);
        }
    }

    private void addPaneListener(Pane container, Pane root) {
        root.managedProperty().bind(root.visibleProperty());
        container.getChildren().addListener((ListChangeListener<Node>) change ->
                hideRoot(container, root));
        hideRoot(container, root);
    }

    private void hideRoot(Pane container, Pane root) {
        root.setVisible(!container.getChildren().isEmpty());
    }

    public void setPictureListener() {
        artist.pictureProperty().addListener((obs, old, newPicture) -> showCover(newPicture != null));
        showCover(artist.pictureProperty().get() != null);
    }

    public void showCover(boolean show) {
        ObservableList<String> classes = lblArtistPicture.getStyleClass();
        if (show) {
            classes.removeAll("default_picture");
            classes.add("picture");

            picturePane.setBorder(null);

            Rectangle rectangle = new Rectangle(100, 100);
            rectangle.setFill(new ImagePattern(artist.pictureProperty().get(), 0, 0, 100, 100, true));
            lblArtistPicture.setGraphic(rectangle);

            Task<Image> imageTask = new Task<>() {
                @Override
                protected Image call() {
                    return ImageScaler.scaleImage(artist.pictureProperty().get(), 100, 100);
                }
            };

            BackgroundTasker.executeGUITaskOnce(imageTask, evt -> {
                ImagePattern imgPattern = new ImagePattern(imageTask.getValue());

                Circle rectCover = new Circle(50);
                rectCover.setFill(imgPattern);
                rectCover.setEffect(new DropShadow(10, 3, 3, Color.grayRgb(15)));

                lblArtistPicture.setBackground(null);
                lblArtistPicture.setGraphic(rectCover);
            });
        } else {
            classes.removeAll("picture");
            classes.add("default_picture");

            lblArtistPicture.setGraphic(null);

            BorderStroke borderStroke = new BorderStroke(gradient, BorderStrokeStyle.SOLID,
                    new CornerRadii(50, true), new BorderWidths(2));
            picturePane.setBorder(new Border(borderStroke));
            lblArtistPicture.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        }
    }

    private void addAlbumElement(AlbumElement albumElement) {
        ObjectProperty<Album.Type> type = albumElement.getAlbum().typeProperty();

        type.addListener((obs, old, newType) -> {
            removeAlbumElementForAll(albumElement);
            addAlbumElement(albumElement);
        });

        switch (type.get()) {
            case SINGLE -> singlesPane.getChildren().add(albumElement);
            case EP     -> epsPane.getChildren().add(albumElement);
            case ALBUM  -> albumsPane.getChildren().add(albumElement);
        }

        Platform.runLater(() -> {
            albumElement.getChildren().remove(albumElement.getLblArtist());
            AlbumView albumView = new AlbumView(albumElement.getAlbum());
            albumView.setGradient(albumElement.getGradient());

            BackgroundTasker.executeInOtherThread(() -> {
                try {
                    Loader.loadRoot("/resources/fxml/album_view.fxml", albumView);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                albumElement.setAlbumView(albumView);
            });
        });
    }

    private void removeAlbumElements(Collection<AlbumElement> albumElements) {
        singlesPane.getChildren().removeAll(albumElements);
        epsPane.getChildren().addAll(albumElements);
        albumsPane.getChildren().addAll(albumElements);
    }

    private void removeAlbumElementForAll(AlbumElement albumElement) {
        singlesPane.getChildren().remove(albumElement);
        epsPane.getChildren().remove(albumElement);
        albumsPane.getChildren().remove(albumElement);
    }

    private void addAlbums() {
        albumsPane.getChildren().clear();

        artist.getAlbums().forEach(album -> {
            AlbumElement albumElement = new AlbumElement(album);
            addAlbumElement(albumElement);
        });
    }

    @FXML private void backDefaultPane() {
        getInstance().getMainGUI().resetCenterDefaultPane();
    }

    public void setGradient(LinearGradient gradient) {
        this.gradient = gradient;
        setTopPaneBackground();
        if (artist.pictureProperty().get() == null) {
            showCover(false);
        }
    }

    private void setTopPaneBackground() {
        if (gradient != null) {
            LinearGradient gradientInverted = Util.invertGradientDarker(gradient);
            BackgroundFill fill = new BackgroundFill(gradientInverted, null, null);
            Background background = new Background(fill);
            topPane.setBackground(background);
        }
    }

    private List<AlbumElement> getAlbumElements() {
        return albumContainer.getChildren().stream()
                .filter(node -> node instanceof AlbumElement)
                .map(node -> (AlbumElement) node)
                .collect(Collectors.toList());
    }

    private List<AlbumElement> getEPElements() {
        return epContainer.getChildren().stream()
                .filter(node -> node instanceof AlbumElement)
                .map(node -> (AlbumElement) node)
                .collect(Collectors.toList());
    }

    private List<AlbumElement> getSingleElements() {
        return singleContainer.getChildren().stream()
                .filter(node -> node instanceof AlbumElement)
                .map(node -> (AlbumElement) node)
                .collect(Collectors.toList());
    }

    private List<AlbumElement> getAllElements() {
        List<AlbumElement> allList = new ArrayList<>();

        allList.addAll(getAlbumElements());
        allList.addAll(getEPElements());
        allList.addAll(getSingleElements());

        return allList;
    }
}