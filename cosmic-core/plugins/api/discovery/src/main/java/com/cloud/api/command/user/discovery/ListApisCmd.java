package com.cloud.api.command.user.discovery;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ApiDiscoveryResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.context.CallContext;
import com.cloud.discovery.ApiDiscoveryService;
import com.cloud.user.User;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listApis", group = APICommandGroup.SystemService,
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
