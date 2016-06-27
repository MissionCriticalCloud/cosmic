package com.cloud.consoleproxy.util;

import java.awt.Rectangle;
import java.util.List;

public interface ITileScanListener {
    boolean onTileChange(Rectangle rowMergedRect, int row, int col);

    void onRegionChange(List<Region> regionList);
}
