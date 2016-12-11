package com.cloud.network;

import com.cloud.api.command.admin.usage.AddTrafficMonitorCmd;
import com.cloud.api.command.admin.usage.DeleteTrafficMonitorCmd;
import com.cloud.api.command.admin.usage.ListTrafficMonitorsCmd;
import com.cloud.host.Host;
import com.cloud.utils.component.Manager;

import java.util.List;

public interface NetworkUsageService extends Manager {

    Host addTrafficMonitor(AddTrafficMonitorCmd cmd);

    boolean deleteTrafficMonitor(DeleteTrafficMonitorCmd cmd);

    List<? extends Host> listTrafficMonitors(ListTrafficMonitorsCmd cmd);
}
