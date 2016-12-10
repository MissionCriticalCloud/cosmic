package com.cloud.api.command.admin.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.vpc.ListVPCsCmd;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.network.vpc.Vpc;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVPCs", description = "Lists VPCs", responseObject = VpcResponse.class, responseView = ResponseView.Full, entityType = {Vpc.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVPCsCmdByAdmin extends ListVPCsCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVPCsCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final Pair<List<? extends Vpc>, Integer> vpcs =
                _vpcService.listVpcs(getId(), getVpcName(), getDisplayText(), getSupportedServices(), getCidr(), getVpcOffId(), getState(), getAccountName(), getDomainId(),
                        getKeyword(), getStartIndex(), getPageSizeVal(), getZoneId(), isRecursive(), listAll(), getRestartRequired(), getTags(),
                        getProjectId(), getDisplay());
        final ListResponse<VpcResponse> response = new ListResponse<>();
        final List<VpcResponse> vpcResponses = new ArrayList<>();
        for (final Vpc vpc : vpcs.first()) {
            final VpcResponse offeringResponse = _responseGenerator.createVpcResponse(ResponseView.Full, vpc);
            vpcResponses.add(offeringResponse);
        }

        response.setResponses(vpcResponses, vpcs.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }
}
