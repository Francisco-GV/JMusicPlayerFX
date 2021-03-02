package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.element.album.AlbumElement;
import com.frank.jmusicplayerfx.gui.element.album.AlbumView;
import com.frank.jmusicplayerfx.gui.element.extra.NoFoundPane;
import com.frank.jmusicplayerfx.Data.Album;
import com.frank.jmusicplayerfx.util.BackgroundTasker;
import com.frank.jmusicplayerfx.util.Loader;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PaneAlbums extends FlowPane {
    private final ObservableList<Album> albumsList;

    private final NoFoundPane noFoundPane;

    public PaneAlbums() {
        noFoundPane = new NoFoundPane("albums");
        albumsList = JMusicPlayerFX.getInstance().getAudioLoader().getInfo().getAlbums();
        initPaneArtists();
    }

    private void initPaneArtists() {
        this.setPadding(new Insets(15));
        this.setHgap(15);
        this.setVgap(15);
        this.setColumnHalignment(HPos.CENTER);
        this.setRowValignment(VPos.TOP);
        this.setAlignment(Pos.TOP_CENTER);

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

            this.getChildren().removeAll(toRemove);

            if (change.getList().isEmpty()) {
                getChildren().setAll(noFoundPane);
            } else {
                getChildren().remove(noFoundPane);
            }
            sortAlbums();
        }
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
            });
            if (!getChildren().contains(albumElement)) getChildren().add(albumElement);
        });
    }

    private void addAlbums() {
        this.getChildren().clear();

        if (albumsList.isEmpty()) {
            this.getChildren().setAll(noFoundPane);
        } else {
            this.getChildren().remove(noFoundPane);
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
            this.getChildren().setAll(albumsElements);
        });
    }

    private List<AlbumElement> getAlbumsElements() {
        synchronized (this.getChildrenUnmodifiable()) {
            return this.getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof AlbumElement)
                    .map(node -> (AlbumElement) node)
                    .collect(Collectors.toList());
        }
    }
}