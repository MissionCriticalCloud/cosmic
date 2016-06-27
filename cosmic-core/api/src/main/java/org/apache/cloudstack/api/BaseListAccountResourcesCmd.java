package org.apache.cloudstack.api;

public abstract class BaseListAccountResourcesCmd extends BaseListDomainResourcesCmd implements IBaseListAccountResourcesCmd {

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "list resources by account. Must be used with the domainId parameter.")
    private String accountName;

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public Boolean getDisplay() {
        return true;
    }
}
