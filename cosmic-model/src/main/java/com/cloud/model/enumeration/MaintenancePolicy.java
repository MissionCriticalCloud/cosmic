package com.cloud.model.enumeration;

public enum MaintenancePolicy {
    LiveMigrate,
    ShutdownAndStart;

    public static MaintenancePolicy getMaintenancePolicy(final String maintenancePolicy) {
        if (maintenancePolicy == null) {
            return MaintenancePolicy.LiveMigrate;
        }
        if (maintenancePolicy.equalsIgnoreCase("ShutdownAndStart")) {
            return MaintenancePolicy.ShutdownAndStart;
        } else {
            return MaintenancePolicy.LiveMigrate;
        }
    }
}
