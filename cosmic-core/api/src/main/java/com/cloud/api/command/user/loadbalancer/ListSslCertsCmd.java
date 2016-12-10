package com.cloud.api.command.user.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SslCertResponse;
import com.cloud.context.CallContext;
import com.cloud.network.lb.CertService;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listSslCerts", description = "Lists SSL certificates", responseObject = SslCertResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListSslCertsCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteSslCertCmd.class.getName());

    private static final String s_name = "listsslcertsresponse";

    @Inject
    CertService _certService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.CERTIFICATE_ID, type = CommandType.UUID, entityType = SslCertResponse.class, required = false, description = "ID of SSL certificate")
    private Long certId;

    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, required = false, description = "Account ID")
    private Long accountId;

    @Parameter(name = ApiConstants.LBID, type = CommandType.UUID, entityType = FirewallRuleResponse.class, required = false, description = "Load balancer rule ID")
    private Long lbId;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, required = false, description = "Project that owns the SSL certificate")
    private Long projectId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getCertId() {
        return certId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getLbId() {
        return lbId;
    }

    public Long getProjectId() {
        return projectId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {

        try {
            final List<SslCertResponse> certResponseList = _certService.listSslCerts(this);
            final ListResponse<SslCertResponse> response = new ListResponse<>();

            response.setResponses(certResponseList);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } catch (final Exception e) {
            throw new CloudRuntimeException(e);
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }
}
