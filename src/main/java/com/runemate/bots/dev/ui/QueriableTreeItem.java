package com.runemate.bots.dev.ui;

import java.util.*;
import java.util.concurrent.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Created by I on 1/23/2016.
 */
public abstract class QueriableTreeItem<T> extends TreeItem<T> {

    private static ExecutorService EXECUTOR_SERVICE;

    private boolean queried;
    private Node queryingGraphic;

    public QueriableTreeItem() {
        this(null);
    }

    public QueriableTreeItem(final T value) {
        super(value);
        this.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (queryingGraphic == null) {
                ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
                progressIndicator.setPrefSize(16, 16);
                progressIndicator.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                progressIndicator.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                queryingGraphic = new VBox(progressIndicator);
            }
            queryingGraphic.setVisible(true);
            setGraphic(queryingGraphic);
            final Runnable runnable = () -> {
                try {
                    if (newValue) {
                        this.getChildren().setAll(query());
                    } else {
                        this.getChildren().clear();
                    }
                    queried = newValue;
                } catch (Exception e) {
                    System.err.println("Point A Thread: " + Thread.currentThread().getName());
                    e.printStackTrace();
                } finally {
                    queryingGraphic.setVisible(false);
                    setGraphic(null);
                }
            };
            if (EXECUTOR_SERVICE != null) {
                EXECUTOR_SERVICE.submit(runnable);
            } else {
                runnable.run();
            }
        });
    }

    public static ExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    public static void setExecutorService(final ExecutorService executorService) {
        EXECUTOR_SERVICE = executorService;
    }

    public abstract List<TreeItem<T>> query();

    @Override
    public boolean isLeaf() {
        return getValue() == null || queried && super.isLeaf();
    }
}
