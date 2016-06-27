package com.cloud.vm;

import java.util.Map;

public class VmWorkMigrateWithStorage extends VmWork {
    private static final long serialVersionUID = -5626053872453569165L;

    long srcHostId;
    long destHostId;
    Map<Long, Long> volumeToPool;

    public VmWorkMigrateWithStorage(final long userId, final long accountId, final long vmId, final String handlerName, final long srcHostId,
                                    final long destHostId, final Map<Long, Long> volumeToPool) {

        super(userId, accountId, vmId, handlerName);

        this.srcHostId = srcHostId;
        this.destHostId = destHostId;
        this.volumeToPool = volumeToPool;
    }

    public long getSrcHostId() {
        return srcHostId;
    }

    public long getDestHostId() {
        return destHostId;
    }

    public Map<Long, Long> getVolumeToPool() {
        return volumeToPool;
    }
}
