package com.frank.jmusicplayerfx.gui.element.settings;

import com.frank.jmusicplayerfx.AudioLoader.Directory;
import com.frank.jmusicplayerfx.JMusicPlayerFX;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class DirectoryElement extends GridPane {

    private final Label lblPath;
    private final Button btnDelete;

    private final Directory directory;

    public DirectoryElement(Directory directory) {
        this.directory = directory;

        lblPath = new Label();
        btnDelete = new Button();

        init();
    }

    private void init() {
        int width = 400;
        int height = 40;
        this.setMinWidth(width);
        this.setPrefWidth(width);
        this.setMaxWidth(width);
        this.setPrefHeight(height);
        this.setMinHeight(height);

        this.setPadding(new Insets(10));
        this.setBackground(new Background(new BackgroundFill(Color.rgb(15, 15, 15), null, null)));

        lblPath.setTextFill(Color.WHITE);
        lblPath.setText(directory.getPath());
        lblPath.setFont(Font.font(14));
        lblPath.setWrapText(true);
        HBox.setHgrow(lblPath, Priority.ALWAYS);

        int size = 15;
        btnDelete.setMinSize(size, size);
        btnDelete.setPrefSize(size, size);
        btnDelete.setMaxSize(size, size);
        btnDelete.getStylesheets().addAll("/resources/css/icons.css", "/resources/css/element/custom_button.css");
        btnDelete.getStyleClass().addAll("delete", "red_button");

        btnDelete.setOnAction(e ->
            JMusicPlayerFX.getInstance().getAudioLoader().getDirectories().remove(directory));

        final double COMPUTED = USE_COMPUTED_SIZE;
        final double PREF = USE_PREF_SIZE;

        var columnConstraints1 = new ColumnConstraints(COMPUTED, COMPUTED, COMPUTED, Priority.ALWAYS, HPos.LEFT, true);
        var columnConstraints2 = new ColumnConstraints(PREF, 20, PREF, Priority.NEVER, HPos.RIGHT, true);

        var rowConstraint = new RowConstraints(10, 30, COMPUTED, Priority.ALWAYS, VPos.CENTER, true);

        this.getColumnConstraints().addAll(columnConstraints1, columnConstraints2);
        this.getRowConstraints().add(rowConstraint);

        this.add(lblPath, 0, 0, 1, 1);
        this.add(btnDelete, 1, 0, 1, 1);
    }

    public Directory getDirectory() {
        return directory;
    }
}