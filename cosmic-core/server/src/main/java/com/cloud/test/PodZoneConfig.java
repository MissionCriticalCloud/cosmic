package com.cloud.test;

public class PodZoneConfig {
    public static long getPodId(final String pod, final long dcId) {
        final String selectSql = "SELECT * FROM `cloud`.`host_pod_ref` WHERE name = \"" + pod + "\" AND data_center_id = \"" + dcId + "\"";
        final String errorMsg = "Could not read pod ID fro mdatabase. Please contact Cloud Support.";
        return DatabaseConfig.getDatabaseValueLong(selectSql, "id", errorMsg);
    }

    public static boolean validPod(final String pod, final String zone) {
        return (getPodId(pod, zone) != -1);
    }

    public static long getPodId(final String pod, final String zone) {
        final long dcId = getZoneId(zone);
        final String selectSql = "SELECT * FROM `cloud`.`host_pod_ref` WHERE name = \"" + pod + "\" AND data_center_id = \"" + dcId + "\"";
        final String errorMsg = "Could not read pod ID fro mdatabase. Please contact Cloud Support.";
        return DatabaseConfig.getDatabaseValueLong(selectSql, "id", errorMsg);
    }

    public static long getZoneId(final String zone) {
        final String selectSql = "SELECT * FROM `cloud`.`data_center` WHERE name = \"" + zone + "\"";
        final String errorMsg = "Could not read zone ID from database. Please contact Cloud Support.";
        return DatabaseConfig.getDatabaseValueLong(selectSql, "id", errorMsg);
    }

    public static boolean validZone(final String zone) {
        return (getZoneId(zone) != -1);
    }
}
