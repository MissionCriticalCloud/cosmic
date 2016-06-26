package org.apache.cloudstack.api.command.user.zone;

import com.cloud.exception.InvalidParameterValueException;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listZones", description = "Lists zones", responseObject = ZoneResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListZonesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListZonesCmd.class.getName());

    private static final String s_name = "listzonesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the ID of the zone")
    private Long id;

    @Parameter(name = ApiConstants.AVAILABLE,
            type = CommandType.BOOLEAN,
            description = "true if you want to retrieve all available Zones. False if you only want to return the Zones"
                    + " from which you have at least one VM. Default is false.")
    private Boolean available;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "the ID of the domain associated with the zone")
    private Long domainId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the zone")
    private String name;

    @Parameter(name = ApiConstants.NETWORK_TYPE, type = CommandType.STRING, description = "the network type of the zone that the virtual machine belongs to")
    private String networkType;

    @Parameter(name = ApiConstants.SHOW_CAPACITIES, type = CommandType.BOOLEAN, description = "flag to display the capacity of the zones")
    private Boolean showCapacities;

    @Parameter(name = ApiConstants.TAGS, type = CommandType.MAP, description = "List zones by resource tags (key/value pairs)", since = "4.3")
    private Map tags;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Boolean isAvailable() {
        return available;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getName() {
        return name;
    }

    public String getNetworkType() {
        return networkType;
    }

    public Boolean getShowCapacities() {
        return showCapacities;
    }

    public Map<String, String> getTags() {
        Map<String, String> tagsMap = null;
        if (tags != null && !tags.isEmpty()) {
            tagsMap = new HashMap<>();
            final Collection<?> servicesCollection = tags.values();
            final Iterator<?> iter = servicesCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> services = (HashMap<String, String>) iter.next();
                final String key = services.get("key");
                final String value = services.get("value");
                if (value == null) {
                    throw new InvalidParameterValueException("No value is passed in for key " + key);
                }
                tagsMap.put(key, value);
            }
        }
        return tagsMap;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {

        final ListResponse<ZoneResponse> response = _queryService.listDataCenters(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
