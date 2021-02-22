package com.frank.jmusicplayerfx.util;

import com.frank.jmusicplayerfx.gui.MainGUI;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class Loader {

    public static <R> R loadRoot(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(Loader.class.getResource(path));
        return loader.load();
    }

    public static <R, C> R loadRoot(String path, C controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(Loader.class.getResource(path));
        loader.setController(controller);
        return loader.load();
    }

    public static <C> C loadController(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(Loader.class.getResource(path));
        loader.load();

        return loader.getController();
    }

    public static <R, C> FXMLObject<R, C> loadFXMLObject(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Loader.class.getResource(path));

        R root = loader.load();
        C controller = loader.getController();

        return new FXMLObject<>(root, controller);
    }

    public static <R, C> FXMLObject<R, C> loadFXMLObject(String path, C controller) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Loader.class.getResource(path));
        loader.setController(controller);

        R root = loader.load();
        return new FXMLObject<>(root, controller);
    }

    /**
     * A class that contains the root and controller of an FXML file
     * @param <R> Represents the root of the FXML Element
     * @param <C> Represents the controller class
     */
    public static final class FXMLObject<R, C> {
        private final R root;
        private final C controller;

        public FXMLObject(R root, C controller) {
            this.root = root;
            this.controller = controller;
        }

        public R getRoot() {
            return root;
        }

        public C getController() {
            return controller;
        }
    }
}