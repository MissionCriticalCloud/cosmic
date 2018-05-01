package com.cloud.api.command.user.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SslCertResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.NetworkRuleConflictException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.lb.CertService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "uploadSslCert", group = APICommandGroup.LoadBalancerService, description = "Upload a certificate to CloudStack", responseObject = SslCertResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UploadSslCertCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UploadSslCertCmd.class.getName());

    private static final String s_name = "uploadsslcertresponse";

    @Inject
    CertService _certService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.CERTIFICATE, type = CommandType.STRING, required = true, description = "SSL certificate", length = 16384)
    private String cert;

    @Parameter(name = ApiConstants.PRIVATE_KEY, type = CommandType.STRING, required = true, description = "Private key", length = 16384)
    private String key;

    @Parameter(name = ApiConstants.CERTIFICATE_CHAIN, type = CommandType.STRING, description = "Certificate chain of trust", length = 2097152)
    private String chain;

    @Parameter(name = ApiConstants.PASSWORD, type = CommandType.STRING, description = "Password for the private key")
    private String password;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "account that will own the SSL certificate")
    private String accountName;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "an optional project for the SSL certificate")
    private Long projectId;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "domain ID of the account owning the SSL certificate")
    private Long domainId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getCert() {
        return cert;
    }

    public String getKey() {
        return key;
    }

    public String getChain() {
        return chain;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getProjectId() {
        return projectId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {

        try {
            final SslCertResponse response = _certService.uploadSslCert(this);
            setResponseObject(response);
            response.setResponseName(getCommandName());
        } catch (final Exception e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
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
