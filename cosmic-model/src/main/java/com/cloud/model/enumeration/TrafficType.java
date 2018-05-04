package com.cloud.model.enumeration;

public enum TrafficType {
    None,
    Public,
    Guest,
    Storage,
    Management,
    Control,
    Vpn;

    public static boolean isSystemNetwork(final TrafficType trafficType) {
        if (Storage.equals(trafficType) || Management.equals(trafficType) || Control.equals(trafficType)) {
            return true;
        }
        return false;
    }

    public static TrafficType getTrafficType(final String type) {
        if ("Public".equals(type)) {
            return Public;
        } else if ("Guest".equals(type)) {
            return Guest;
        } else if ("Storage".equals(type)) {
            return Storage;
        } else if ("Management".equals(type)) {
            return Management;
        } else if ("Control".equals(type)) {
            return Control;
        } else if ("Vpn".equals(type)) {
            return Vpn;
        } else {
            return None;
        }
    }
}
