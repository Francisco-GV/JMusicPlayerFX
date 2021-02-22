package com.frank.jmusicplayerfx.gui.element;

import com.frank.jmusicplayerfx.gui.MainGUI;
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
import javafx.scene.shape.SVGPath;

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
        BackgroundFill fill = new BackgroundFill(Color.rgb(100, 100, 100, 0.1), null, null);

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(15, 15, 0, 15));
        this.setSpacing(15);
        this.setFillWidth(true);
        this.setBackground(new Background(fill));

        SVGPath exitPath = new SVGPath();

        exitPath.setContent("M 19.061,7.061 16.939,4.939 12,9.879 7.061,4.939 4.939,7.061 " +
                "9.879,12 4.939,16.939 7.061,19.061 12,14.121 16.939,19.061 19.061,16.939 " +
                "14.121,12 Z");

        btnExit.setShape(exitPath);
        btnExit.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnExit.setPadding(new Insets(0));

        double btnSize = 12.0;

        btnExit.setPrefSize(btnSize, btnSize);
        btnExit.setMinSize(btnSize, btnSize);
        btnExit.setStyle("-fx-background-color: white");
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
    }
}