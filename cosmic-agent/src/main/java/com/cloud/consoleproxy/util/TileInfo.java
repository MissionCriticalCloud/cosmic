package com.cloud.consoleproxy.util;

import java.awt.Rectangle;

public class TileInfo {
    private int row;
    private int col;
    private Rectangle tileRect;

    public TileInfo(final int row, final int col, final Rectangle tileRect) {
        this.row = row;
        this.col = col;
        this.tileRect = tileRect;
    }

    public int getRow() {
        return row;
    }

    public void setRow(final int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(final int col) {
        this.col = col;
    }

    public Rectangle getTileRect() {
        return tileRect;
    }

    public void setTileRect(final Rectangle tileRect) {
        this.tileRect = tileRect;
    }
}
