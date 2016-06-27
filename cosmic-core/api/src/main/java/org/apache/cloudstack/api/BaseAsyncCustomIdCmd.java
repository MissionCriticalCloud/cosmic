package org.apache.cloudstack.api;

import org.apache.cloudstack.acl.RoleType;

public abstract class BaseAsyncCustomIdCmd extends BaseAsyncCmd {
    @Parameter(name = ApiConstants.CUSTOM_ID,
            type = CommandType.STRING,
            description = "an optional field, in case you want to set a custom id to the resource. Allowed to Root Admins only", since = "4.4", authorized = {RoleType.Admin})
    private String customId;

    public String getCustomId() {
        return customId;
    }

    public abstract void checkUuid();
}
