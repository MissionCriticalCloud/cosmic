package com.cloud.storage;

import com.cloud.utils.exception.CloudRuntimeException;

public enum DataStoreRole {
    Primary("primary"), Image("image"), ImageCache("imagecache"), Backup("backup");

    private final String role;

    DataStoreRole(final String type) {
        role = type;
    }

    public static DataStoreRole getRole(final String role) {
        if (role == null) {
            throw new CloudRuntimeException("role can't be empty");
        }
        if (role.equalsIgnoreCase("primary")) {
            return Primary;
        } else if (role.equalsIgnoreCase("image")) {
            return Image;
        } else if (role.equalsIgnoreCase("imagecache")) {
            return ImageCache;
        } else if (role.equalsIgnoreCase("backup")) {
            return Backup;
        } else {
            throw new CloudRuntimeException("can't identify the role");
        }
    }

    public boolean isImageStore() {
        return (role.equalsIgnoreCase("image") || role.equalsIgnoreCase("imagecache")) ? true : false;
    }
}
