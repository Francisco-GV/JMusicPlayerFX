package com.frank.jmusicplayerfx.gui.container;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.element.album.AlbumElement;
import com.frank.jmusicplayerfx.gui.element.album.AlbumView;
import com.frank.jmusicplayerfx.gui.element.extra.NoFoundPane;
import com.frank.jmusicplayerfx.Data.Album;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.Loader;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PaneAlbums extends VBox {
    private final ListProperty<Album> albumsList;

    private final ScrollPane scrollPane;
    private final FlowPane albumElementContainer;
    private final NoFoundPane noFoundPane;

    public PaneAlbums() {
        scrollPane = new ScrollPane();
        noFoundPane = new NoFoundPane("albums");
        albumElementContainer = new FlowPane(Orientation.HORIZONTAL);

        albumsList = JMusicPlayerFX.getInstance().getAudioLoader().getInfo().getAlbums();
        initPaneArtists();
    }

    private void initPaneArtists() {
        scrollPane.getStylesheets().add("/resources/css/main_gui_style.css");
        scrollPane.setId("scroll_pane_content");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        albumElementContainer.setColumnHalignment(HPos.CENTER);
        albumElementContainer.setRowValignment(VPos.CENTER);
        albumElementContainer.setHgap(15);
        albumElementContainer.setVgap(15);
        albumElementContainer.setAlignment(Pos.TOP_CENTER);

        scrollPane.setContent(albumElementContainer);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        VBox.setMargin(noFoundPane, new Insets(25, 0, 0, 0));

        this.setPadding(new Insets(15));
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(noFoundPane, scrollPane);

        noFoundPane.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                (albumsList.size() == 0), albumsList.sizeProperty()));
        noFoundPane.managedProperty().bind(noFoundPane.visibleProperty());

        addAlbums();
        albumsList.addListener(this::updateAlbumsEvent);
    }

    private void updateAlbumsEvent(ListChangeListener.Change<? extends Album> change) {
        while (change.next()) {
            List<? extends Album> addedAlbums = change.getAddedSubList();
            List<? extends Album> removedAlbums = change.getRemoved();

            addedAlbums.forEach(added -> {
                AlbumElement artistElement = new AlbumElement(added);
                if (!getAlbumsElements().contains(artistElement)) {
                    addAlbumElement(artistElement);
                }
            });

            List<AlbumElement> toRemove = getAlbumsElements().stream()
                    .filter(element -> removedAlbums.contains(element.getAlbum()))
                    .collect(Collectors.toList());

            albumElementContainer.getChildren().removeAll(toRemove);
        }
        sortAlbums();
    }

    private void addAlbumElement(AlbumElement albumElement) {
        Platform.runLater(() -> {
            albumElement.getChildren().remove(albumElement.getLblYear());
            AlbumView albumView = new AlbumView(albumElement.getAlbum());
            albumView.setGradient(albumElement.getGradient());

            BackgroundTasker.executeInOtherThread(() -> {
                try {
                    Loader.loadRoot("/resources/fxml/album_view.fxml", albumView);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                albumElement.setAlbumView(albumView);

                Platform.runLater(() -> {
                    if (!albumElementContainer.getChildren().contains(albumElement)) {
                        albumElementContainer.getChildren().add(albumElement);
                    }
                });
            });
        });
    }

    private void addAlbums() {
        albumElementContainer.getChildren().clear();

        if (!albumsList.isEmpty()) {
            albumsList.forEach(album -> {
                AlbumElement albumElement = new AlbumElement(album);
                addAlbumElement(albumElement);
            });

            sortAlbums();
        }
    }

    private void sortAlbums() {
        Platform.runLater(() -> {
            List<AlbumElement> albumsElements = getAlbumsElements();

            albumsElements.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    Label lblo1 = o1.getLblName();
                    Label lblo2 = o2.getLblName();

                    return lblo1.getText().compareTo(lblo2.getText());
                } else return 0;
            });
            albumElementContainer.getChildren().setAll(albumsElements);
        });
    }

    private List<AlbumElement> getAlbumsElements() {
        synchronized (albumElementContainer.getChildrenUnmodifiable()) {
            return albumElementContainer.getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof AlbumElement)
                    .map(node -> (AlbumElement) node)
                    .collect(Collectors.toList());
        }
    }
}