package org.apache.cloudstack.api.command.admin.network;

import com.cloud.network.Networks;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.response.IsolationMethodResponse;
import org.apache.cloudstack.api.response.ListResponse;

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
