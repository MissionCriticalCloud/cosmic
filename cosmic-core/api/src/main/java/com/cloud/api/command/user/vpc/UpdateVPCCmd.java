package com.cloud.api.command.user.vpc;

import com.cloud.acl.RoleType;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCustomIdCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.VpcOfferingResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.network.vpc.Vpc;
import com.cloud.legacymodel.user.Account;
import com.cloud.model.enumeration.AdvertMethod;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVPC", group = APICommandGroup.VPCService, description = "Updates a VPC", responseObject = VpcResponse.class, responseView = ResponseView.Restricted, entityType = {Vpc.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVPCCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVPCCmd.class.getName());
    private static final String s_name = "updatevpcresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VpcResponse.class, required = true, description = "the id of the VPC")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the VPC")
    private String vpcName;

    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, description = "the display text of the VPC")
    private String displayText;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the vpc to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    @Parameter(name = ApiConstants.VPC_OFF_ID, type = CommandType.UUID, entityType = VpcOfferingResponse.class, description = "The new VPC offering ID to switch to. This will result " +
            "in a restart+cleanup of the VPC")
    private Long vpcOfferingId;

    @Parameter(name = ApiConstants.SOURCE_NAT_LIST, type = CommandType.STRING,
            description = "Source NAT CIDR list for used to allow other CIDRs to be source NATted by the VPC over the public interface")
    private String sourceNatList;

    @Parameter(name = ApiConstants.SYSLOG_SERVER_LIST, type = CommandType.STRING,
            description = "Comma separated list of IP addresses to configure as syslog servers on the VPC to forward IP tables logging")
    private String syslogServerList;

    @Parameter(name = ApiConstants.ADVERT_INTERVAL, type = CommandType.LONG,
            description = "VRRP advertisement interval. Defaults to 1.")
    private Long advertInterval;

    @Parameter(name = ApiConstants.ADVERT_METHOD, type = CommandType.STRING,
            description = "VRRP advertisement method to use: unicast / multicast. Defaults to multicast'")
    private String advertMethod;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Vpc result = _vpcService.updateVpc(getId(), getVpcName(), getDisplayText(), getCustomId(), getDisplayVpc(), getVpcOfferingId(), getSourceNatList(),
                getSyslogServerList(), getAdvertInterval(), getAdvertMethod());
        if (result != null) {
            final VpcResponse response = _responseGenerator.createVpcResponse(ResponseView.Restricted, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update VPC");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Vpc vpc = _entityMgr.findById(Vpc.class, getId());
        if (vpc != null) {
            return vpc.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }

    public String getVpcName() {
        return vpcName;
    }

    public String getDisplayText() {
        return displayText;
    }

    public Boolean getDisplayVpc() {
        return display;
    }

    public Long getVpcOfferingId() {
        return vpcOfferingId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VPC_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "updating VPC id=" + getId();
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.vpcSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getId();
    }

    public String getSourceNatList() {
        if (StringUtils.isEmpty(sourceNatList)) {
            return sourceNatList;
        }
        return sourceNatList.replaceAll("\\s", "");
    }

    public String getSyslogServerList() {
        if (StringUtils.isEmpty(syslogServerList)) {
            return syslogServerList;
        }
        return syslogServerList.replaceAll("\\s", "");
    }

    @Override
    public void checkUuid() {
        if (getCustomId() != null) {
            _uuidMgr.checkUuid(getCustomId(), Vpc.class);
        }
    }

    public Long getAdvertInterval() {
        return advertInterval;
    }

    public AdvertMethod getAdvertMethod() {
        if (advertMethod != null) {
            return AdvertMethod.valueOf(advertMethod.toUpperCase());
        } else {
            return null;
        }
    }
}
