//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.cloud.info;

import com.cloud.host.Host;

import java.util.HashMap;
import java.util.Map;

public class RunningHostInfoAgregator {

    private final Map<Long, ZoneHostInfo> zoneHostInfoMap = new HashMap<>();

    public RunningHostInfoAgregator() {
    }

    public void aggregate(final RunningHostCountInfo countInfo) {
        if (countInfo.getCount() > 0) {
            final ZoneHostInfo zoneInfo = getZoneHostInfo(countInfo.getDcId());

            final Host.Type type = Enum.valueOf(Host.Type.class, countInfo.getHostType());
            if (type == Host.Type.Routing) {
                zoneInfo.setFlag(ZoneHostInfo.ROUTING_HOST_MASK);
            } else if (type == Host.Type.Storage || type == Host.Type.SecondaryStorage) {
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
