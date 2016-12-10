package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.BaseListCmd;
import com.cloud.api.response.IsolationMethodResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.network.Networks;

import java.util.ArrayList;
import java.util.List;

@APICommand(name = "listNetworkIsolationMethods",
        description = "Lists supported methods of network isolation",
        responseObject = IsolationMethodResponse.class,
        since = "4.2.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class ListNetworkIsolationMethodsCmd extends BaseListCmd {

    private static final String s_name = "listnetworkisolationmethodsresponse";

    @Override
    public void execute() {
        final Networks.IsolationType[] methods = _ntwkModel.listNetworkIsolationMethods();

        final ListResponse<IsolationMethodResponse> response = new ListResponse<>();
        final List<IsolationMethodResponse> isolationResponses = new ArrayList<>();
        if (methods != null) {
            for (final Networks.IsolationType method : methods) {
                final IsolationMethodResponse isolationMethod = _responseGenerator.createIsolationMethodResponse(method);
                isolationResponses.add(isolationMethod);
            }
        }
        response.setResponses(isolationResponses, isolationResponses.size());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
