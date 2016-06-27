package org.apache.cloudstack.api.command.user.vpc;

import com.cloud.network.vpc.VpcOffering;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.VpcOfferingResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVPCOfferings", description = "Lists VPC offerings", responseObject = VpcOfferingResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVPCOfferingsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVPCOfferingsCmd.class.getName());
    private static final String s_name = "listvpcofferingsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VpcOfferingResponse.class, description = "list VPC offerings by id")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "list VPC offerings by name")
    private String vpcOffName;

    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, description = "list VPC offerings by display text")
    private String displayText;

    @Parameter(name = ApiConstants.IS_DEFAULT, type = CommandType.BOOLEAN, description = "true if need to list only default " + "VPC offerings. Default value is false")
    private Boolean isDefault;

    @Parameter(name = ApiConstants.SUPPORTED_SERVICES,
            type = CommandType.LIST,
            collectionType = CommandType.STRING,
            description = "list VPC offerings supporting certain services")
    private List<String> supportedServices;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "list VPC offerings by state")
    private String state;

    public Boolean getIsDefault() {
        return isDefault;
    }

    @Override
    public void execute() {
        final Pair<List<? extends VpcOffering>, Integer> offerings =
                _vpcProvSvc.listVpcOfferings(getId(), getVpcOffName(), getDisplayText(), getSupportedServices(), isDefault, this.getKeyword(), getState(),
                        this.getStartIndex(), this.getPageSizeVal());
        final ListResponse<VpcOfferingResponse> response = new ListResponse<>();
        final List<VpcOfferingResponse> offeringResponses = new ArrayList<>();
        for (final VpcOffering offering : offerings.first()) {
            final VpcOfferingResponse offeringResponse = _responseGenerator.createVpcOfferingResponse(offering);
            offeringResponses.add(offeringResponse);
        }

        response.setResponses(offeringResponses, offerings.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public Long getId() {
        return id;
    }

    public String getVpcOffName() {
        return vpcOffName;
    }

    public String getDisplayText() {
        return displayText;
    }

    public List<String> getSupportedServices() {
        return supportedServices;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getState() {
        return state;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
