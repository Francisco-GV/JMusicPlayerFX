package com.frank.jmusicplayerfx;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class BackgroundTasker {
    public static void executeTask(Task<?> task, EventHandler<WorkerStateEvent> onSucceeded) {
        task.setOnSucceeded(onSucceeded);

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);

        backgroundThread.start();
    }

    public static void executeInOtherThread(Runnable runnable) {
        Thread backgroundThread = new Thread(runnable);
        backgroundThread.setDaemon(true);

        backgroundThread.start();
    }
}