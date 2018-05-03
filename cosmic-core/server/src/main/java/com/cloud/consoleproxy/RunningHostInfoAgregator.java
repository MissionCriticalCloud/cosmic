package com.cloud.consoleproxy;

import com.cloud.info.RunningHostCountInfo;
import com.cloud.model.enumeration.HostType;

import java.util.HashMap;
import java.util.Map;

public class RunningHostInfoAgregator {

    private final Map<Long, ZoneHostInfo> zoneHostInfoMap = new HashMap<>();

    public RunningHostInfoAgregator() {
    }

    public void aggregate(final RunningHostCountInfo countInfo) {
        if (countInfo.getCount() > 0) {
            final ZoneHostInfo zoneInfo = getZoneHostInfo(countInfo.getDcId());

            final HostType type = Enum.valueOf(HostType.class, countInfo.getHostType());
            if (type == HostType.Routing) {
                zoneInfo.setFlag(ZoneHostInfo.ROUTING_HOST_MASK);
            } else if (type == HostType.Storage || type == HostType.SecondaryStorage) {
                zoneInfo.setFlag(ZoneHostInfo.STORAGE_HOST_MASK);
            }
        }
    }

    private ZoneHostInfo getZoneHostInfo(final long dcId) {
        if (zoneHostInfoMap.containsKey(dcId)) {
            return zoneHostInfoMap.get(dcId);
        }

        final ZoneHostInfo info = new ZoneHostInfo();
        info.setDcId(dcId);
        zoneHostInfoMap.put(dcId, info);
        return info;
    }

    public Map<Long, ZoneHostInfo> getZoneHostInfoMap() {
        return zoneHostInfoMap;
    }

    public static class ZoneHostInfo {
        public static final int ROUTING_HOST_MASK = 2;
        public static final int STORAGE_HOST_MASK = 4;
        public static final int ALL_HOST_MASK = ROUTING_HOST_MASK | STORAGE_HOST_MASK;

        private long dcId;

        // (1 << 1) : at least one routing host is running in the zone
        // (1 << 2) : at least one storage host is running in the zone
        private int flags = 0;

        public long getDcId() {
            return dcId;
        }

        public void setDcId(final long dcId) {
            this.dcId = dcId;
        }

        public void setFlag(final int flagMask) {
            flags |= flagMask;
        }

        public int getFlags() {
            return flags;
        }
    }
}
