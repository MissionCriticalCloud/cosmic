package org.apache.cloudstack.api;

public abstract class BaseAsyncCreateCustomIdCmd extends BaseAsyncCreateCmd {
    @Parameter(name = ApiConstants.CUSTOM_ID,
            type = CommandType.STRING,
            description = "an optional field, in case you want to set a custom id to the resource. Allowed to Root Admins only")
    private String customId;

    public String getCustomId() {
        return customId;
    }
}
