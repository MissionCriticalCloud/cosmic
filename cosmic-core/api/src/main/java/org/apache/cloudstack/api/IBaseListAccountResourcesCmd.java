package org.apache.cloudstack.api;

public interface IBaseListAccountResourcesCmd extends IBaseListDomainResourcesCmd {
    String getAccountName();

    Boolean getDisplay();
}
