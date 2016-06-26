package com.cloud.configuration;

public interface Resource {

    public static final short RESOURCE_UNLIMITED = -1;

    ResourceType getType();

    long getOwnerId();

    ResourceOwnerType getResourceOwnerType();

    public enum ResourceType { // Primary and Secondary storage are allocated_storage and not the physical storage.
        user_vm("user_vm", 0, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        public_ip("public_ip", 1, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        volume("volume", 2, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        snapshot("snapshot", 3, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        template("template", 4, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        project("project", 5, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        network("network", 6, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        vpc("vpc", 7, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        cpu("cpu", 8, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        memory("memory", 9, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        primary_storage("primary_storage", 10, ResourceOwnerType.Account, ResourceOwnerType.Domain),
        secondary_storage("secondary_storage", 11, ResourceOwnerType.Account, ResourceOwnerType.Domain);

        public static final long bytesToGiB = 1024 * 1024 * 1024;
        private final String name;
        private final ResourceOwnerType[] supportedOwners;
        private final int ordinal;

        ResourceType(final String name, final int ordinal, final ResourceOwnerType... supportedOwners) {
            this.name = name;
            this.supportedOwners = supportedOwners;
            this.ordinal = ordinal;
        }

        public String getName() {
            return name;
        }

        public ResourceOwnerType[] getSupportedOwners() {
            return supportedOwners;
        }

        public boolean supportsOwner(final ResourceOwnerType ownerType) {
            boolean success = false;
            if (supportedOwners != null) {
                final int length = supportedOwners.length;
                for (int i = 0; i < length; i++) {
                    if (supportedOwners[i].getName().equalsIgnoreCase(ownerType.getName())) {
                        success = true;
                        break;
                    }
                }
            }

            return success;
        }

        public int getOrdinal() {
            return ordinal;
        }
    }

    public static class ResourceOwnerType {

        public static final ResourceOwnerType Account = new ResourceOwnerType("Account");
        public static final ResourceOwnerType Domain = new ResourceOwnerType("Domain");

        private final String name;

        public ResourceOwnerType(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
