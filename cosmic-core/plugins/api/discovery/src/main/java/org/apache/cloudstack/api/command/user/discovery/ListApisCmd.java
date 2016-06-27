package org.apache.cloudstack.api.command.user.discovery;

import com.cloud.user.User;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ApiDiscoveryResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.discovery.ApiDiscoveryService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listApis",
        responseObject = ApiDiscoveryResponse.class,
        description = "lists all available apis on the server, provided by the Api Discovery plugin",
        since = "4.1.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class ListApisCmd extends BaseCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(ListApisCmd.class.getName());
    private static final String s_name = "listapisresponse";

    @Inject
    ApiDiscoveryService _apiDiscoveryService;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "API name")
    private String name;

    @Override
    public void execute() throws ServerApiException {
        if (_apiDiscoveryService != null) {
            final User user = CallContext.current().getCallingUser();
            final ListResponse<ApiDiscoveryResponse> response = (ListResponse<ApiDiscoveryResponse>) _apiDiscoveryService.listApis(user, name);
            if (response == null) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Api Discovery plugin was unable to find an api by that name or process any apis");
            }
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        // no owner is needed for list command
        return 0;
    }
}
