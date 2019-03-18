package com.cloud.api.command.user.config;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.BaseCmd;
import com.cloud.api.response.CapabilitiesResponse;
import com.cloud.legacymodel.user.Account;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listCapabilities", group = APICommandGroup.ConfigurationService, description = "Lists capabilities", responseObject = CapabilitiesResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListCapabilitiesCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListCapabilitiesCmd.class.getName());

    private static final String s_name = "listcapabilitiesresponse";

    @Override
    public void execute() {
        final Map<String, Object> capabilities = _mgr.listCapabilities(this);
        final CapabilitiesResponse response = new CapabilitiesResponse();
        response.setCosmicVersion((String) capabilities.get("cosmicVersion"));
        response.setCosmic((Boolean) true);
        response.setUserPublicTemplateEnabled((Boolean) capabilities.get("userPublicTemplateEnabled"));
        response.setSupportELB((String) capabilities.get("supportELB"));
        response.setProjectInviteRequired((Boolean) capabilities.get("projectInviteRequired"));
        response.setAllowUsersCreateProjects((Boolean) capabilities.get("allowusercreateprojects"));
        response.setDiskOffMinSize((Long) capabilities.get("customDiskOffMinSize"));
        response.setDiskOffMaxSize((Long) capabilities.get("customDiskOffMaxSize"));
        response.setRegionSecondaryEnabled((Boolean) capabilities.get("regionSecondaryEnabled"));
        response.setKVMSnapshotEnabled((Boolean) capabilities.get("KVMSnapshotEnabled"));
        response.setAllowUserViewDestroyedVM((Boolean) capabilities.get("allowUserViewDestroyedVM"));
        response.setAllowUserExpungeRecoverVM((Boolean) capabilities.get("allowUserExpungeRecoverVM"));
        response.setXenServerDeploymentsEnabled((Boolean) capabilities.get("xenserverDeploymentsEnabled"));
        response.setKvmDeploymentsEnabled((Boolean) capabilities.get("KVMDeploymentsEnabled"));
        if (capabilities.containsKey("apiLimitInterval")) {
            response.setApiLimitInterval((Integer) capabilities.get("apiLimitInterval"));
        }
        if (capabilities.containsKey("apiLimitMax")) {
            response.setApiLimitMax((Integer) capabilities.get("apiLimitMax"));
        }
        response.setObjectName("capability");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
