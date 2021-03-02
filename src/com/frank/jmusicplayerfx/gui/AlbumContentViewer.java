package com.frank.jmusicplayerfx.gui;

import com.frank.jmusicplayerfx.gui.element.album.AlbumView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class AlbumContentViewer extends VBox {
    private final HBox topBox;
    private final Button btnExit;

    private final MainGUI mainGUI;

    public AlbumContentViewer(MainGUI mainGUI) {
        topBox = new HBox();
        btnExit = new Button();

        this.mainGUI = mainGUI;

        init();
    }

    private void init() {
        BackgroundFill fill = new BackgroundFill(Color.rgb(0, 0, 0, 0.4), null, null);

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(15, 15, 0, 15));
        this.setSpacing(15);
        this.setFillWidth(true);
        this.setBackground(new Background(fill));

        btnExit.getStylesheets().add("/resources/css/element/custom_button.css");
        btnExit.getStylesheets().add("/resources/css/icons.css");
        btnExit.getStyleClass().addAll("exit", "default_button");
        btnExit.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnExit.setPadding(new Insets(0));

        double btnSize = 12.0;

        btnExit.setPrefSize(btnSize, btnSize);
        btnExit.setMinSize(btnSize, btnSize);
        btnExit.setOnAction(e -> {
            mainGUI.getCenterStackPane().getChildren().remove(this);
            mainGUI.blurDefaultPane(false);
        });

        topBox.setMaxWidth(720);
        topBox.setAlignment(Pos.CENTER_RIGHT);
        topBox.getChildren().add(btnExit);

        this.getChildren().add(topBox);
        this.getChildren().add(new Group());
    }


    public void showAlbumView(AlbumView albumView) {
        this.getChildren().set(1, albumView.getRoot());
        albumView.getTableSongs().sort();

    }
}