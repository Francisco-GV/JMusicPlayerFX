package com.frank.jmusicplayerfx.gui.container;

import com.frank.jmusicplayerfx.Data.Artist;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.gui.element.artist.ArtistElement;
import com.frank.jmusicplayerfx.gui.element.extra.NoFoundPane;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PaneArtists extends VBox {
    private final ListProperty<Artist> artistsList;

    private final ScrollPane scrollPane;
    private final FlowPane artistElementContainer;
    private final NoFoundPane noFoundPane;

    public PaneArtists() {
        scrollPane = new ScrollPane();
        artistElementContainer = new FlowPane(Orientation.HORIZONTAL);
        noFoundPane = new NoFoundPane("artists");

        artistsList = JMusicPlayerFX.getInstance().getAudioLoader().getInfo().getArtists();
        initPaneArtists();
    }

    private void initPaneArtists() {
        scrollPane.getStylesheets().add("/resources/css/main_gui_style.css");
        scrollPane.setId("scroll_pane_content");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        artistElementContainer.setColumnHalignment(HPos.CENTER);
        artistElementContainer.setRowValignment(VPos.CENTER);
        artistElementContainer.setHgap(15);
        artistElementContainer.setVgap(15);
        artistElementContainer.setAlignment(Pos.TOP_CENTER);

        scrollPane.setContent(artistElementContainer);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        VBox.setMargin(noFoundPane, new Insets(25, 0, 0, 0));

        this.setPadding(new Insets(15));
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(noFoundPane, scrollPane);

        noFoundPane.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                (artistsList.size() == 0), artistsList.sizeProperty()));
        noFoundPane.managedProperty().bind(noFoundPane.visibleProperty());

        addAllArtist();
        artistsList.addListener(this::updateArtistsEvent);
    }

    private void updateArtistsEvent(ListChangeListener.Change<? extends Artist> change) {
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

            artistElementContainer.getChildren().removeAll(toRemove);
        }

        sortArtists();
    }

    private void addArtistElement(ArtistElement artistElement) {
        Platform.runLater(() -> {
            ArtistContentViewer artistContentViewer = new ArtistContentViewer(artistElement.getArtist());

            BackgroundTasker.executeInOtherThread(() -> {
                Pane artistPane = null;
                try {
                    artistPane = Loader.loadRoot("/resources/fxml/artist_content_viewer.fxml", artistContentViewer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                artistElement.setArtistContent(artistPane);
                artistContentViewer.setGradient(artistElement.getGradient());

                Platform.runLater(() -> {
                    if (!artistElementContainer.getChildren().contains(artistElement)) {
                        artistElementContainer.getChildren().add(artistElement);
                    }
                });
            });
        });
    }

    private void addAllArtist() {
        artistElementContainer.getChildren().clear();

        if (!artistsList.isEmpty()) {
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

        Platform.runLater(() -> artistElementContainer.getChildren().setAll(artistElements));
    }

    private List<ArtistElement> getArtistsElements() {
        synchronized (artistElementContainer.getChildrenUnmodifiable()) {
            return artistElementContainer.getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof ArtistElement)
                    .map(node -> (ArtistElement) node)
                    .collect(Collectors.toList());
        }
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