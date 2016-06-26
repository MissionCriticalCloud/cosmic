package com.cloud.network;

import com.cloud.dc.StorageNetworkIpRange;
import org.apache.cloudstack.api.command.admin.network.CreateStorageNetworkIpRangeCmd;
import org.apache.cloudstack.api.command.admin.network.DeleteStorageNetworkIpRangeCmd;
import org.apache.cloudstack.api.command.admin.network.ListStorageNetworkIpRangeCmd;
import org.apache.cloudstack.api.command.admin.network.UpdateStorageNetworkIpRangeCmd;

import java.sql.SQLException;
import java.util.List;

public interface StorageNetworkService {
    StorageNetworkIpRange createIpRange(CreateStorageNetworkIpRangeCmd cmd) throws SQLException;

    void deleteIpRange(DeleteStorageNetworkIpRangeCmd cmd);

    List<StorageNetworkIpRange> listIpRange(ListStorageNetworkIpRangeCmd cmd);

    StorageNetworkIpRange updateIpRange(UpdateStorageNetworkIpRangeCmd cmd);
}
