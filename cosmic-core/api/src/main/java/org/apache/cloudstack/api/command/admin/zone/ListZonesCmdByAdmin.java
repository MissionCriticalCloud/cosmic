package org.apache.cloudstack.api.command.admin.zone;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.zone.ListZonesCmd;
import org.apache.cloudstack.api.response.ZoneResponse;

@APICommand(name = "listZones", description = "Lists zones", responseObject = ZoneResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListZonesCmdByAdmin extends ListZonesCmd {
}
