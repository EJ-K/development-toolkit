package com.runemate.bots.ui.util;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public final class VerticalDragResizerUtil {

    public static final int DEFAULT_RESIZE_MARGIN = 5;

    public static void makeResizable(final Region region) {
        makeResizable(region, DEFAULT_RESIZE_MARGIN);
    }

    public static void makeResizable(final Region region, final int resizeMargin) {
        final VerticalDragResizer resizer = new VerticalDragResizer(region, resizeMargin);

        region.setOnMousePressed(resizer::mousePressed);
        region.setOnMouseDragged(resizer::mouseDragged);
        region.setOnMouseMoved(resizer::mouseOver);
        region.setOnMouseReleased(resizer::mouseReleased);
        region.setOnMouseExited(resizer::mouseExited);
    }

    /**
     * {@link VerticalDragResizer} can be used to add mouse listeners to a {@link Region}
     * and make it resizable by the user by clicking and dragging the border in the
     * same way as a window.
     * <p>
     * Only height resizing is currently implemented. Usage: <pre>VerticalDragResizer.makeResizable(myAnchorPane);</pre>
     */
    private static final class VerticalDragResizer {

        /**
         * The margin around the control that a user can click in to start resizing
         * the region.
         */
        private final int resizeMargin;

        private final Region region;

        private double y;

        private boolean initMinHeight;

        private boolean dragging;

        private VerticalDragResizer(Region region, int resizeMargin) {
            this.region = region;
            this.resizeMargin = resizeMargin;
        }

        private void mouseReleased(MouseEvent event) {
            dragging = false;
            region.setCursor(Cursor.DEFAULT);
        }

        private void mouseOver(MouseEvent event) {
            if (isInDraggableZone(event) || dragging) {
                region.setCursor(Cursor.S_RESIZE);
            } else {
                region.setCursor(Cursor.DEFAULT);
            }
        }

        private boolean isInDraggableZone(MouseEvent event) {
            return event.getY() > (region.getHeight() - resizeMargin);
        }

        private void mouseDragged(MouseEvent event) {
            if (!dragging) {
                return;
            }

            double mousey = event.getY();
            double newHeight = region.getMinHeight() + (mousey - y);
            region.setMinHeight(newHeight);
            y = mousey;
        }

        private void mousePressed(MouseEvent event) {
            // ignore clicks outside of the draggable margin
            if (!isInDraggableZone(event)) {
                return;
            }
            dragging = true;

            // make sure that the minimum height is set to the current height once,
            // setting a min height that is smaller than the current height will
            // have no effect
            if (!initMinHeight) {
                region.setMinHeight(region.getHeight());
                initMinHeight = true;
            }
            y = event.getY();
        }

        private void mouseExited(MouseEvent event) {
            if (dragging) {
                return;
            }
            region.setCursor(Cursor.DEFAULT);
        }
    }
}
