package com.cloud.api.command.admin.zone;

import com.cloud.api.command.user.zone.ListZonesCmd;
import com.cloud.api.response.ZoneResponse;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;

@APICommand(name = "listZones", description = "Lists zones", responseObject = ZoneResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListZonesCmdByAdmin extends ListZonesCmd {
}
