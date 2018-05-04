package com.cloud.consoleproxy.util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class RegionClassifier {
    private final List<Region> regionList;

    public RegionClassifier() {
        regionList = new ArrayList<>();
    }

    public void add(final Rectangle rect) {
        boolean newRegion = true;
        final Rectangle rcInflated = new Rectangle(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2);
        for (final Region region : regionList) {
            if (region.getBound().intersects(rcInflated)) {
                newRegion = false;
                break;
            }
        }

        if (newRegion) {
            regionList.add(new Region(rect));
        } else {
            for (final Region region : regionList) {
                if (region.add(rect)) {
                    return;
                }
            }
            regionList.add(new Region(rect));
        }
    }

    public List<Region> getRegionList() {
        return regionList;
    }

    public void clear() {
        regionList.clear();
    }
}
