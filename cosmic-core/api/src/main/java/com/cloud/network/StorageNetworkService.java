package com.cloud.network;

import com.cloud.api.command.admin.network.CreateStorageNetworkIpRangeCmd;
import com.cloud.api.command.admin.network.DeleteStorageNetworkIpRangeCmd;
import com.cloud.api.command.admin.network.ListStorageNetworkIpRangeCmd;
import com.cloud.api.command.admin.network.UpdateStorageNetworkIpRangeCmd;
import com.cloud.dc.StorageNetworkIpRange;

import java.sql.SQLException;
import java.util.List;

public interface StorageNetworkService {
    StorageNetworkIpRange createIpRange(CreateStorageNetworkIpRangeCmd cmd) throws SQLException;

    void deleteIpRange(DeleteStorageNetworkIpRangeCmd cmd);

    List<StorageNetworkIpRange> listIpRange(ListStorageNetworkIpRangeCmd cmd);

    StorageNetworkIpRange updateIpRange(UpdateStorageNetworkIpRangeCmd cmd);
}
