package com.runemate.bots.dev.ui.overlay;

import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.HOTPINK;
import static javafx.scene.paint.Color.YELLOWGREEN;

import com.runemate.bots.dev.DevelopmentToolkit;
import com.runemate.client.game.open.*;
import com.runemate.game.api.hybrid.entities.Entity;
import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.entities.Npc;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.details.Renderable;
import com.runemate.game.api.hybrid.geom.*;
import com.runemate.game.api.hybrid.local.Screen;
import com.runemate.game.api.hybrid.local.hud.Model;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.runemate.game.api.hybrid.location.*;
import com.runemate.game.api.hybrid.web.vertex.*;
import com.runemate.game.api.osrs.entities.*;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.*;
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

    private final BooleanProperty showing = new SimpleBooleanProperty(false);

    public DevelopmentToolkitOverlay(final DevelopmentToolkit bot) {
        this.bot = bot;
        showing.addListener((obs, old, show) -> {
            if (show) {
                start();
            } else {
                close();
            }
        });
    }

    private static double[] convert(@Nonnull int[] ints) {
        double[] doubles = new double[ints.length];
        for (int i = 0; i < ints.length; i++) {
            doubles[i] = (double) ints[i];
        }
        return doubles;
    }

    public BooleanProperty showingProperty() {
        return showing;
    }

    public boolean isShowing() {
        return showing.get();
    }

    private void start() {
        Platform.runLater(() -> {
            impl = new Overlay();
            impl.start();
        });
    }

    public void close() {
        Platform.runLater(() -> {
            if (impl != null) {
                impl.close();
                impl = null;
            }
        });
    }

    public void destroy() {
        Platform.runLater(() -> showing.set(false));
    }

    private class Overlay extends Stage {

        private final ObservableMap<Integer, Shape> shapes = FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());
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
                    final List<Object> renderables = new ArrayList<>(bot.getRenderables());
                    final List<Integer> updatedHashes = new ArrayList<>();
                    for (final Object renderable : renderables) {
                        render(renderable, updatedHashes);
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
                } catch (Exception e) {
                    e.printStackTrace();
                    stop();
                    close();
                    return;
                }
                fps();
            }

            private void render(Object renderable, List<Integer> updatedHashes) {
                try {
                    if (renderable instanceof Entity) {
                        if (renderable instanceof OSRSEntity) {
                            final int hash = renderable.hashCode();
                            final var model = bot.getPlatform().invokeAndWait(() -> OpenHull.lookup(((OSRSEntity) renderable).uid));
                            if (model == null) {
                                return;
                            }
                            final Paint color;
                            if (renderable instanceof Npc) {
                                color = Color.DODGERBLUE;
                            } else if (renderable instanceof Player) {
                                color = BLUE;
                            } else if (renderable instanceof GroundItem) {
                                color = Color.GREEN;
                            } else {
                                color = Color.RED;
                            }
                            updatedHashes.add(hash);
                            shape(hash, model, color);
                        } else {
                            final int hash = renderable.hashCode();
                            final Polygon model = bot.getPlatform().invokeAndWait(() -> {
                                final Model m = ((Entity) renderable).getModel();
                                return m == null ? null : m.projectConvexHull();
                            });
                            if (model == null) {
                                return;
                            }
                            final Paint color;
                            if (renderable instanceof Npc) {
                                color = Color.DODGERBLUE;
                            } else if (renderable instanceof Player) {
                                color = BLUE;
                            } else if (renderable instanceof GroundItem) {
                                color = Color.GREEN;
                            } else {
                                color = Color.RED;
                            }
                            updatedHashes.add(hash);
                            poly(hash, model, color);
                        }
                    } else if (renderable instanceof InterfaceComponent) {
                        final int hash = renderable.hashCode();
                        final InterfaceComponent component = (InterfaceComponent) renderable;
                        final Rectangle bounds = bot.getPlatform().invokeAndWait(component::getBounds);
                        if (bounds == null) {
                            return;
                        }
                        updatedHashes.add(hash);
                        rect(hash, bounds, HOTPINK);
                    } else if (renderable instanceof SpriteItem) {
                        final int hash = renderable.hashCode();
                        final SpriteItem component = (SpriteItem) renderable;
                        final Rectangle bounds = bot.getPlatform().invokeAndWait(component::getBounds);
                        if (bounds == null) {
                            return;
                        }
                        updatedHashes.add(hash);
                        rect(hash, bounds, YELLOWGREEN);
                    } else if (renderable instanceof Vertex) {
                        final var vertex = (Vertex) renderable;
                        final var position = bot.getPlatform().invokeAndWait(vertex::getPosition);
                        if (position == null) {
                            return;
                        }
                        render(position, updatedHashes);
                    } else if (renderable instanceof Coordinate) {
                        final var vertex = (Coordinate) renderable;
                        final var hash = vertex.hashCode();
                        final var position = bot.getPlatform().invokeAndWait(() -> vertex.getBounds());
                        if (position == null) {
                            return;
                        }
                        updatedHashes.add(hash);
                        poly(hash, position, BLUE);
                    }
                } catch (Exception ignored) {

                }
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

            private void shape(int hash, SimplePolygon poly, Paint color) {
                var points = new double[poly.getPoints().size() * 2];
                var i = 0;
                for (var point : poly.getPoints()) {
                    points[i++] = point.getX();
                    points[i++] = point.getY();
                }
                var shape = new javafx.scene.shape.Polygon(points);
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
