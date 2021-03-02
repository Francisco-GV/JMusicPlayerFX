package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.element.artist.ArtistElement;
import com.frank.jmusicplayerfx.gui.element.extra.NoFoundPane;
import com.frank.jmusicplayerfx.util.Loader;
import com.frank.jmusicplayerfx.util.Util;
import com.frank.jmusicplayerfx.Data.Artist;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PaneArtists extends FlowPane {
    private final ObservableList<Artist> artistsList;

    private final NoFoundPane noFoundPane;

    public PaneArtists() {
        noFoundPane = new NoFoundPane("artists");
        artistsList = JMusicPlayerFX.getInstance().getAudioLoader().getInfo().getArtists();
        initPaneArtists();
    }

    private void initPaneArtists() {
        this.setPadding(new Insets(15));
        this.setHgap(15);
        this.setVgap(15);
        this.setColumnHalignment(HPos.CENTER);
        this.setRowValignment(VPos.TOP);
        this.setAlignment(Pos.TOP_CENTER);

        addAllArtist();
        artistsList.addListener(this::updateArtistsEvent);
    }

    private void updateArtistsEvent(ListChangeListener.Change<? extends Artist> change) {
        Platform.runLater(() -> {
            while (change.next()) {
                List<? extends Artist> addedArtists = change.getAddedSubList();
                List<? extends Artist> removedArtists = change.getRemoved();

                addedArtists.forEach(added -> {
                    ArtistElement artistElement = new ArtistElement(added);
                    if (!getArtistsElements().contains(artistElement)) {
                        addArtistElement(artistElement);
                    }
                });

                List<ArtistElement> toRemove = getArtistsElements().stream()
                        .filter(element -> removedArtists.contains(element.getArtist()))
                        .collect(Collectors.toList());

                this.getChildren().removeAll(toRemove);

                if (change.getList().isEmpty()) {
                    this.getChildren().setAll(noFoundPane);
                } else {
                    this.getChildren().remove(noFoundPane);
                }

                sortArtists();
            }
        });
    }

    private void addArtistElement(ArtistElement artistElement) {
        ArtistContentViewer artistContentViewer = new ArtistContentViewer(artistElement.getArtist());

        Pane artistPane = null;
        try {
            artistPane = Loader.loadRoot("/resources/fxml/artist_content_viewer.fxml", artistContentViewer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        artistElement.setArtistContent(artistPane);

        LinearGradient gradient = Util.randomGradient();
        artistElement.setGradient(gradient);
        artistContentViewer.setGradient(gradient);

        Platform.runLater(() -> getChildren().add(artistElement));
    }

    private void addAllArtist() {
        this.getChildren().clear();

        if (artistsList.isEmpty()) {
            this.getChildren().setAll(noFoundPane);
        } else {
            this.getChildren().remove(noFoundPane);
            artistsList.forEach(artist -> {
                ArtistElement artistElement = new ArtistElement(artist);
                addArtistElement(artistElement);
            });

            sortArtists();
        }
    }

    private void sortArtists() {
        List<ArtistElement> artistElements = getArtistsElements();

        artistElements.sort((o1, o2) -> {
            Label lblo1 = o1.getLblName();
            Label lblo2 = o2.getLblName();

            return lblo1.getText().compareTo(lblo2.getText());
        });

        getChildren().setAll(artistElements);
    }

    private List<ArtistElement> getArtistsElements() {
        return this.getChildren().stream()
                .filter(node -> node instanceof ArtistElement)
                .map(node -> (ArtistElement) node)
                .collect(Collectors.toList());
    }

    public void hideCollabs(boolean hide) {
        List<ArtistElement> collabs = getArtistsElements()
                .stream()
                .filter(element -> element.getArtist().isCollab())
                .collect(Collectors.toList());

        collabs.forEach(element -> {
            element.setVisible(!hide);
            element.setManaged(!hide);
        });
    }
}