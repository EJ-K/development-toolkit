package com.runemate.bots.dev.ui.overlay;

import static javafx.scene.paint.Color.HOTPINK;
import static javafx.scene.paint.Color.YELLOWGREEN;

import com.runemate.bots.dev.DevelopmentToolkit;
import com.runemate.game.api.hybrid.entities.Entity;
import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.entities.Npc;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.details.Renderable;
import com.runemate.game.api.hybrid.local.Screen;
import com.runemate.game.api.hybrid.local.hud.Model;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.annotation.Nonnull;

public class DevelopmentToolkitOverlay {

    private final DevelopmentToolkit bot;

    private Overlay impl;

    private Rectangle lastBounds;
    private Point lastLoc;

    public DevelopmentToolkitOverlay(final DevelopmentToolkit bot) {
        this.bot = bot;
    }

    private static double[] convert(@Nonnull int[] ints) {
        double[] doubles = new double[ints.length];
        for (int i = 0; i < ints.length; i++) {
            doubles[i] = (double) ints[i];
        }
        return doubles;
    }

    public void start() {
        Platform.runLater(() -> {
            impl = new Overlay();
            impl.start();
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            impl.close();
            impl = null;
        });
    }

    private class Overlay extends Stage {

        private final ObservableMap<Integer, Shape> shapes =
            FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());
        private final DoubleProperty fpsProperty = new SimpleDoubleProperty(0L);
        private long lastTime = 0L;

        public void start() {
            final Group group = new Group();
            shapes.addListener((MapChangeListener<Integer, Shape>) change -> {
                if (change.wasRemoved()) {
                    group.getChildren().remove(change.getValueRemoved());
                }
                if (change.wasAdded()) {
                    group.getChildren().add(change.getValueAdded());
                }
            });
            final Label fpsLabel = new Label();
            fpsLabel.setFont(new Font(12));
            fpsLabel.setTextFill(Color.BLACK);
            fpsLabel.textProperty().bind(fpsProperty.asString("FPS: %.2f"));
            fpsLabel.setLayoutX(10);
            fpsLabel.setLayoutY(40);
            group.getChildren().add(fpsLabel);
            final Scene scene = new Scene(group, 600, 400, Color.TRANSPARENT);

            setScene(scene);
            initStyle(StageStyle.TRANSPARENT);
            setAlwaysOnTop(true);
            show();

            new RenderLoop().start();
        }

        private void fps() {
            long thisTime = System.currentTimeMillis();
            double delta = thisTime - lastTime;
            double fps = 1.0 / delta * 1000;
            lastTime = thisTime;
            fpsProperty.set(fps);
        }

        private class RenderLoop extends AnimationTimer {

            @Override
            public void handle(final long now) {
                try {
                    final List<Renderable> renderables = new ArrayList<>(bot.getRenderables());
                    final List<Integer> updatedHashes = new ArrayList<>();
                    for (final Renderable renderable : renderables) {
                        if (renderable instanceof Entity) {
                            final int hash = renderable.hashCode();
                            final Polygon model = bot.getPlatform().invokeAndWait(() -> {
                                final Model m = ((Entity) renderable).getModel();
                                return m == null ? null : m.projectConvexHull();
                            });
                            if (model == null) {
                                continue;
                            }
                            final Paint color;
                            if (renderable instanceof Npc) {
                                color = Color.DODGERBLUE;
                            } else if (renderable instanceof Player) {
                                color = Color.BLUE;
                            } else if (renderable instanceof GroundItem) {
                                color = Color.GREEN;
                            } else {
                                color = Color.RED;
                            }
                            updatedHashes.add(hash);
                            poly(hash, model, color);
                        } else if (renderable instanceof InterfaceComponent) {
                            final int hash = renderable.hashCode();
                            final InterfaceComponent component = (InterfaceComponent) renderable;
                            final Rectangle bounds =
                                bot.getPlatform().invokeAndWait(component::getBounds);
                            if (bounds == null) {
                                continue;
                            }
                            updatedHashes.add(hash);
                            rect(hash, bounds, HOTPINK);
                        } else if (renderable instanceof SpriteItem) {
                            final int hash = renderable.hashCode();
                            final SpriteItem component = (SpriteItem) renderable;
                            final Rectangle bounds =
                                bot.getPlatform().invokeAndWait(component::getBounds);
                            if (bounds == null) {
                                continue;
                            }
                            updatedHashes.add(hash);
                            rect(hash, bounds, YELLOWGREEN);
                        }
                    }
                    shapes.keySet().retainAll(updatedHashes);

                    final Rectangle bounds = bot.getPlatform().invokeAndWait(Screen::getBounds);
                    final Point loc = bot.getPlatform().invokeAndWait(Screen::getLocation);
                    if (bounds != null) {
                        if (bounds.width != (int) getWidth()) {
                            setWidth(bounds.width);
                        }
                        if (bounds.height != (int) getHeight()) {
                            setHeight(bounds.height);
                        }
                    }
                    if (loc != null) {
                        if (loc.x != (int) getX()) {
                            setX(loc.x);
                        }
                        if (loc.y != (int) getY()) {
                            setY(loc.y);
                        }
                    }

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                fps();
            }

            private void rect(int hash, Rectangle bounds, Paint color) {
                final Shape existing = shapes.get(hash);
                if (existing != null) {
                    final javafx.scene.shape.Rectangle r = (javafx.scene.shape.Rectangle) existing;
                    r.setX(bounds.getX());
                    r.setY(bounds.getY());
                    r.setWidth(bounds.getWidth());
                    r.setHeight(bounds.getHeight());
                    return;
                }

                final javafx.scene.shape.Rectangle shape =
                    new javafx.scene.shape.Rectangle(
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height
                    );

                shape.setStroke(color);
                shape.setFill(Color.TRANSPARENT);
                shape.setStrokeWidth(2.0);
                shapes.put(hash, shape);
            }

            private void poly(int hash, Polygon poly, Paint color) {
                final javafx.scene.shape.Polygon shape = toFxPoly(poly);

                final Shape existing = shapes.get(hash);
                if (existing != null) {
                    final javafx.scene.shape.Polygon epoly = (javafx.scene.shape.Polygon) existing;
                    epoly.getPoints().setAll(shape.getPoints());
                    return;
                }

                shape.setStroke(color);
                shape.setFill(Color.TRANSPARENT);
                shape.setStrokeWidth(2.0);
                shapes.put(hash, shape);
            }

            private javafx.scene.shape.Polygon toFxPoly(Polygon polygon) {
                final double[] points = new double[polygon.npoints * 2];
                for (int i = 0, j = 0; i < polygon.npoints; i++) {
                    points[j++] = polygon.xpoints[i];
                    points[j++] = polygon.ypoints[i];
                }
                return new javafx.scene.shape.Polygon(points);
            }
        }
    }

}
