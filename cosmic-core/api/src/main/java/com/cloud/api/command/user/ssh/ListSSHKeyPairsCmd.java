package com.cloud.api.command.user.ssh;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListProjectAndAccountResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.SSHKeyPairResponse;
import com.cloud.legacymodel.user.SSHKeyPair;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listSSHKeyPairs", group = APICommandGroup.SSHService, description = "List registered keypairs", responseObject = SSHKeyPairResponse.class, entityType = {SSHKeyPair.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListSSHKeyPairsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListSSHKeyPairsCmd.class.getName());
    private static final String s_name = "listsshkeypairsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "A key pair name to look for")
    private String name;

    @Parameter(name = "fingerprint", type = CommandType.STRING, description = "A public key fingerprint to look for")
    private String fingerprint;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends SSHKeyPair>, Integer> resultList = _mgr.listSSHKeyPairs(this);
        final List<SSHKeyPairResponse> responses = new ArrayList<>();
        for (final SSHKeyPair result : resultList.first()) {
            final SSHKeyPairResponse r = _responseGenerator.createSSHKeyPairResponse(result, false);
            r.setObjectName("sshkeypair");
            responses.add(r);
        }

        final ListResponse<SSHKeyPairResponse> response = new ListResponse<>();
        response.setResponses(responses, resultList.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
