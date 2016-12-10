package com.cloud.api;

public interface IBaseListAccountResourcesCmd extends IBaseListDomainResourcesCmd {
    String getAccountName();

    Boolean getDisplay();
}
