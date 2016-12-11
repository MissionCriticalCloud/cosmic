package com.cloud.api.command.admin.domain;

import com.cloud.api.APICommand;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.response.DomainResponse;
import com.cloud.domain.Domain;

@APICommand(name = "listDomains", description = "Lists domains and provides detailed information for listed domains", responseObject = DomainResponse.class, responseView =
        ResponseView.Full, entityType = {Domain.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListDomainsCmdByAdmin extends ListDomainsCmd {
}
