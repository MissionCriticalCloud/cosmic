package org.apache.cloudstack.api.command.user.autoscale;

import com.cloud.network.as.AutoScaleVmProfile;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AutoScaleVmProfileResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listAutoScaleVmProfiles", description = "Lists autoscale vm profiles.", responseObject = AutoScaleVmProfileResponse.class, entityType = {AutoScaleVmProfile
        .class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListAutoScaleVmProfilesCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListAutoScaleVmProfilesCmd.class.getName());

    private static final String s_name = "listautoscalevmprofilesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = AutoScaleVmProfileResponse.class, description = "the ID of the autoscale vm profile")
    private Long id;

    @Parameter(name = ApiConstants.TEMPLATE_ID, type = CommandType.UUID, entityType = TemplateResponse.class, description = "the templateid of the autoscale vm profile")
    private Long templateId;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID, type = CommandType.UUID, entityType = ServiceOfferingResponse.class, description = "list profiles by service offering " +
            "id", since = "4.4")
    private Long serviceOffId;

    @Parameter(name = ApiConstants.OTHER_DEPLOY_PARAMS, type = CommandType.STRING, description = "the otherdeployparameters of the autoscale vm profile")
    private String otherDeployParams;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, since = "4.4", description = "availability zone for the auto deployed " +
            "virtual machine")
    private Long zoneId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this parameter",
            since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getOtherDeployParams() {
        return otherDeployParams;
    }

    public Long getServiceOfferingId() {
        return serviceOffId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public Boolean getDisplay() {
        if (display != null) {
            return display;
        }
        return super.getDisplay();
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<? extends AutoScaleVmProfile> autoScaleProfiles = _autoScaleService.listAutoScaleVmProfiles(this);
        final ListResponse<AutoScaleVmProfileResponse> response = new ListResponse<>();
        final List<AutoScaleVmProfileResponse> responses = new ArrayList<>();
        if (autoScaleProfiles != null) {
            for (final AutoScaleVmProfile autoScaleVmProfile : autoScaleProfiles) {
                final AutoScaleVmProfileResponse autoScaleVmProfileResponse = _responseGenerator.createAutoScaleVmProfileResponse(autoScaleVmProfile);
                autoScaleVmProfileResponse.setObjectName("autoscalevmprofile");
                responses.add(autoScaleVmProfileResponse);
            }
        }
        response.setResponses(responses);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
