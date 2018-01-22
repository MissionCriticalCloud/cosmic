package com.cloud.api.command.admin.zone;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.zone.ListZonesCmd;
import com.cloud.api.response.ZoneResponse;

@APICommand(name = "listZones", group = APICommandGroup.ZoneService, description = "Lists zones", responseObject = ZoneResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListZonesCmdByAdmin extends ListZonesCmd {
}
