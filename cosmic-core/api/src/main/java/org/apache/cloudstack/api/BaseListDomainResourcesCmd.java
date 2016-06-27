package org.apache.cloudstack.api;

import org.apache.cloudstack.api.response.DomainResponse;

public abstract class BaseListDomainResourcesCmd extends BaseListCmd implements IBaseListDomainResourcesCmd {

    @Parameter(name = ApiConstants.LIST_ALL, type = CommandType.BOOLEAN, description = "If set to false, "
            + "list only resources belonging to the command's caller; if set to true - list resources that the caller is authorized to see. Default value is false")
    private Boolean listAll;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "list only resources belonging to the domain specified")
    private Long domainId;

    @Parameter(name = ApiConstants.IS_RECURSIVE, type = CommandType.BOOLEAN, description = "defaults to false,"
            + " but if true, lists all resources from the parent specified by the domainId till leaves.")
    private Boolean recursive;

    @Override
    public boolean listAll() {
        return listAll == null ? false : listAll;
    }

    @Override
    public boolean isRecursive() {
        return recursive == null ? false : recursive;
    }

    @Override
    public Long getDomainId() {
        return domainId;
    }
}
