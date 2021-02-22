package com.frank.jmusicplayerfx.util;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundTasker {
    public static final Timer timerTasker;

    static {
        timerTasker = new Timer(true);
    }

    public static void executeGUITaskOnce(Task<?> task, EventHandler<WorkerStateEvent> onSucceeded) {
        task.setOnSucceeded(onSucceeded);
        executeInOtherThread(task);
    }

    public static void executeInOtherThread(Runnable runnable) {
        Thread backgroundThread = new Thread(runnable);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    public static void executePeriodically(TimerTask task, long delay, long period) {
        timerTasker.schedule(task, delay, period);
    }
}