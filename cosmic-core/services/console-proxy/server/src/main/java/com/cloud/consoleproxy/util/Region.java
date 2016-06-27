package com.cloud.consoleproxy.util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Region {
    private final Rectangle bound;
    private final List<Rectangle> rectList;

    public Region() {
        bound = new Rectangle(0, 0, 0, 0);
        rectList = new ArrayList<>();
    }

    public Region(final Rectangle rect) {
        bound = new Rectangle(rect.x, rect.y, rect.width, rect.height);
        rectList = new ArrayList<>();
        rectList.add(rect);
    }

    public Rectangle getBound() {
        return bound;
    }

    public void clearBound() {
        assert (rectList.size() == 0);
        bound.x = bound.y = bound.width = bound.height = 0;
    }

    public List<Rectangle> getRectangles() {
        return rectList;
    }

    public boolean add(final Rectangle rect) {
        if (bound.isEmpty()) {
            assert (rectList.size() == 0);
            bound.x = rect.x;
            bound.y = rect.y;
            bound.width = rect.width;
            bound.height = rect.height;

            rectList.add(rect);
            return true;
        }

        final Rectangle rcInflated = new Rectangle(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2);
        if (!bound.intersects(rcInflated)) {
            return false;
        }

        for (final Rectangle r : rectList) {
            if (r.intersects(rcInflated)) {
                if (!r.contains(rect)) {
                    enlargeBound(rect);
                    rectList.add(rect);
                    return true;
                }
            }
        }
        return false;
    }

    private void enlargeBound(final Rectangle rect) {
        final int boundLeft = Math.min(bound.x, rect.x);
        final int boundTop = Math.min(bound.y, rect.y);
        final int boundRight = Math.max(bound.x + bound.width, rect.x + rect.width);
        final int boundBottom = Math.max(bound.y + bound.height, rect.y + rect.height);

        bound.x = boundLeft;
        bound.y = boundTop;
        bound.width = boundRight - boundLeft;
        bound.height = boundBottom - boundTop;
    }
}
