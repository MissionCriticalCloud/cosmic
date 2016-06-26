package com.cloud.consoleproxy.vnc;

import com.cloud.consoleproxy.util.TileInfo;

import java.awt.Image;
import java.util.List;

public interface FrameBufferCanvas {
    Image getFrameBufferScaledImage(int width, int height);

    public byte[] getFrameBufferJpeg();

    public byte[] getTilesMergedJpeg(List<TileInfo> tileList, int tileWidth, int tileHeight);
}
