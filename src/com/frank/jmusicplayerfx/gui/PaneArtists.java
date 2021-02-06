package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.JMusicPlayerFX;
import com.frank.jmusicplayerfx.Util;
import com.frank.jmusicplayerfx.media.AudioLoader.Info.Artist;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.io.IOException;
import java.util.Comparator;

public class PaneArtists {
    @FXML private Pane root;

    private ObservableList<Artist> artistsList;
    @FXML private void initialize() {
        artistsList = JMusicPlayerFX.getInstance().getAudioLoader().getInfo().getArtists();

        artistsList.addListener(new ListChangeListener<>() {
            @Override
            public void onChanged(Change<? extends Artist> change) {
                root.getChildren().clear();

                artistsList.forEach(artist -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("/resources/fxml/artist_element.fxml"));
                        loader.setController(new ArtistElement(artist));
                        Pane pane = loader.load();

                        root.getChildren().add(pane);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                sortArtists();
            }
        });
    }

    private void sortArtists() {
        ObservableList<Node> sortedList =
                FXCollections.observableArrayList(root.getChildren());

        sortedList.sort(new Comparator<>() {
            @Override
            public int compare(Node o1, Node o2) {
                Label lblo1 = getLblName((Pane) o1);
                Label lblo2 = getLblName((Pane) o2);

                return lblo1.getText().compareTo(lblo2.getText());
            }

            private Label getLblName(Pane pane) {
                return (Label) pane.getChildren().stream()
                        .filter(node -> node.getId().equals("lbl_name"))
                        .findFirst().orElse(null);
            }
        });

        root.getChildren().setAll(sortedList);
    }

    @SuppressWarnings("unused")
    public static class ArtistElement {
        private final Artist artist;
        @FXML private Label lblPicture;
        @FXML private Label lblName;

        @FXML private Pane root;
        @FXML private Pane picPane;

        public ArtistElement(Artist artist) {
            this.artist = artist;
        }

        @FXML private void initialize() {
            lblName.setText(artist.getName());

            Color color1 = Util.randomPastelColor();
            Color color2 = Util.randomPastelColor();

            Stop[] stops = new Stop[] {
                    new Stop(0, color1), new Stop(1, color2)
            };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

            BorderStroke borderStroke = new BorderStroke(gradient, BorderStrokeStyle.SOLID,
                    new CornerRadii(50, true), new BorderWidths(2));
            picPane.setBorder(new Border(borderStroke));
            lblPicture.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        }
    }
}