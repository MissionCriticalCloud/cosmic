package com.cloud.api.command.admin.region;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PortableIpRangeResponse;
import com.cloud.api.response.PortableIpResponse;
import com.cloud.region.PortableIp;
import com.cloud.region.PortableIpRange;
import com.cloud.user.Account;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listPortableIpRanges", description = "list portable IP ranges", responseObject = PortableIpRangeResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListPortableIpRangesCmd extends BaseListCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(ListPortableIpRangesCmd.class.getName());

    private static final String s_name = "listportableipresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.REGION_ID, type = CommandType.INTEGER, required = false, description = "Id of a Region")
    private Integer regionId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, required = false, entityType = PortableIpRangeResponse.class, description = "Id of the portable ip range")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Integer getRegionIdId() {
        return regionId;
    }

    public Long getPortableIpRangeId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        final ListResponse<PortableIpRangeResponse> response = new ListResponse<>();
        final List<PortableIpRangeResponse> responses = new ArrayList<>();

        final List<? extends PortableIpRange> portableIpRanges = _configService.listPortableIpRanges(this);
        if (portableIpRanges != null && !portableIpRanges.isEmpty()) {
            for (final PortableIpRange range : portableIpRanges) {
                final PortableIpRangeResponse rangeResponse = _responseGenerator.createPortableIPRangeResponse(range);

                final List<? extends PortableIp> portableIps = _configService.listPortableIps(range.getId());
                if (portableIps != null && !portableIps.isEmpty()) {
                    final List<PortableIpResponse> portableIpResponses = new ArrayList<>();
                    for (final PortableIp portableIP : portableIps) {
                        final PortableIpResponse portableIpresponse = _responseGenerator.createPortableIPResponse(portableIP);
                        portableIpResponses.add(portableIpresponse);
                    }
                    rangeResponse.setPortableIpResponses(portableIpResponses);
                }
                responses.add(rangeResponse);
            }
            response.setResponses(responses, portableIpRanges.size());
        }
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
