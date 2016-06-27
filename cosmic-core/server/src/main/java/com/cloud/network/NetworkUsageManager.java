package com.cloud.network;

import com.cloud.network.dao.IPAddressVO;
import com.cloud.utils.component.Manager;

import java.util.List;

public interface NetworkUsageManager extends Manager {

    List<IPAddressVO> listAllocatedDirectIps(long zoneId);
}
