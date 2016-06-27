package com.cloud.alert;

import com.cloud.utils.component.Manager;
import org.apache.cloudstack.alert.AlertService;
import org.apache.cloudstack.framework.config.ConfigKey;

public interface AlertManager extends Manager, AlertService {

    static final ConfigKey<Double> StorageCapacityThreshold = new ConfigKey<>(Double.class, "cluster.storage.capacity.notificationthreshold", "Alert", "0.75",
            "Percentage (as a value between 0 and 1) of storage utilization above which alerts will be sent about low storage available.", true, ConfigKey.Scope.Cluster,
            null);
    static final ConfigKey<Double> CPUCapacityThreshold = new ConfigKey<>(Double.class, "cluster.cpu.allocated.capacity.notificationthreshold", "Alert", "0.75",
            "Percentage (as a value between 0 and 1) of cpu utilization above which alerts will be sent about low cpu available.", true, ConfigKey.Scope.Cluster, null);
    static final ConfigKey<Double> MemoryCapacityThreshold = new ConfigKey<>(Double.class, "cluster.memory.allocated.capacity.notificationthreshold", "Alert",
            "0.75", "Percentage (as a value between 0 and 1) of memory utilization above which alerts will be sent about low memory available.", true,
            ConfigKey.Scope.Cluster, null);
    static final ConfigKey<Double> StorageAllocatedCapacityThreshold = new ConfigKey<>(Double.class, "cluster.storage.allocated.capacity.notificationthreshold",
            "Alert", "0.75", "Percentage (as a value between 0 and 1) of allocated storage utilization above which alerts will be sent about low storage available.", true,
            ConfigKey.Scope.Cluster, null);

    void clearAlert(AlertType alertType, long dataCenterId, long podId);

    void recalculateCapacity();

    void sendAlert(AlertType alertType, long dataCenterId, Long podId, String subject, String body);
}
