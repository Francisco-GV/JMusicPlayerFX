package com.frank.jmusicplayerfx.gui.element.artist;

import com.frank.jmusicplayerfx.Data.Artist;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.LinearGradient;

import static com.frank.jmusicplayerfx.JMusicPlayerFX.getInstance;

public class ArtistElement extends VBox {
    private final Artist artist;

    private final BorderPane pictureContainer;
    private final Label lblPicture;
    private final Label lblName;

    private final BooleanProperty defaultPicture;

    public ArtistElement(Artist artist) {
        setId("artist_element");
        getStylesheets().add("/resources/css/artists/artist_element_style.css");

        this.artist = artist;
        pictureContainer = new BorderPane();
        lblPicture = new Label();
        lblName = new Label();

        defaultPicture = new SimpleBooleanProperty(false);

        defaultPicture.addListener((observable, oldValue, isDefault) -> {
            ObservableList<String> classes = lblPicture.getStyleClass();
            if (isDefault) {
                classes.removeAll("picture");
                classes.add("default_picture");
            } else {
                classes.removeAll("default_picture");
                classes.add("picture");
            }
        });

        initializeElement();
    }

    private void initializeElement() {
        defaultPicture.set(true);

        pictureContainer.setId("picture_container");
        pictureContainer.setCenter(lblPicture);

        lblName.setId("lbl_name");
        lblName.setText(artist.getName());

        this.getChildren().addAll(pictureContainer, lblName);
    }

    public void setArtistContent(Pane artistContent) {
        this.setOnMouseReleased(e -> getInstance().getMainGUI().setCenterPane(artistContent));
    }

    public void setGradient(LinearGradient gradient) {
        BorderStroke borderStroke = new BorderStroke(gradient, BorderStrokeStyle.SOLID,
                new CornerRadii(50, true), new BorderWidths(2));
        pictureContainer.setBorder(new Border(borderStroke));
        lblPicture.setBackground(new Background(new BackgroundFill(gradient, null, null)));
    }

    public Label getLblName() {
        return lblName;
    }

    public Artist getArtist() {
        return artist;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArtistElement)) return false;

        Artist objArtist = ((ArtistElement) obj).getArtist();

        return objArtist.equals(this.getArtist());
    }
}