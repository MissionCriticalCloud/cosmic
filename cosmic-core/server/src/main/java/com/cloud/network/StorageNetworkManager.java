package com.cloud.network;

import com.cloud.dc.StorageNetworkIpAddressVO;
import com.cloud.utils.component.Manager;
import com.cloud.vm.SecondaryStorageVmVO;

import java.util.List;

public interface StorageNetworkManager extends Manager {
    StorageNetworkIpAddressVO acquireIpAddress(long podId);

    void releaseIpAddress(String ip);

    boolean isStorageIpRangeAvailable(long zoneId);

    List<SecondaryStorageVmVO> getSSVMWithNoStorageNetwork(long zoneId);

    boolean isAnyStorageIpInUseInZone(long zoneId);
}
