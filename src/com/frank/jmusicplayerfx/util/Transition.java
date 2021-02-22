package com.frank.jmusicplayerfx.util;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

public class Transition {
    public static void fade(long millis, long delay, Node node) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(node);
        fadeTransition.setDuration(new Duration(millis));

        node.setOpacity(0);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                fadeTransition.play();
            }
        }, delay);
    }
}