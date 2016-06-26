package org.apache.cloudstack.api.command.admin.vpc;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.vpc.VpcOffering;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.VpcOfferingResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVPCOffering", description = "Creates VPC offering", responseObject = VpcOfferingResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVPCOfferingCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateVPCOfferingCmd.class.getName());
    private static final String s_name = "createvpcofferingresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "the name of the vpc offering")
    private String vpcOfferingName;

    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, required = true, description = "the display text of " + "the vpc offering")
    private String displayText;

    @Parameter(name = ApiConstants.SUPPORTED_SERVICES,
            type = CommandType.LIST,
            required = true,
            collectionType = CommandType.STRING,
            description = "services supported by the vpc offering")
    private List<String> supportedServices;

    @Parameter(name = ApiConstants.SERVICE_PROVIDER_LIST, type = CommandType.MAP, description = "provider to service mapping. "
            + "If not specified, the provider for the service will be mapped to the default provider on the physical network")
    private Map<String, ? extends Map<String, String>> serviceProviderList;

    @Parameter(name = ApiConstants.SERVICE_CAPABILITY_LIST, type = CommandType.MAP, description = "desired service capabilities as part of vpc offering", since = "4.4")
    private Map<String, List<String>> serviceCapabilitystList;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID,
            type = CommandType.UUID,
            entityType = ServiceOfferingResponse.class,
            description = "the ID of the service offering for the VPC router appliance")
    private Long serviceOfferingId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void create() throws ResourceAllocationException {
        final VpcOffering vpcOff = _vpcProvSvc.createVpcOffering(getVpcOfferingName(), getDisplayText(),
                getSupportedServices(), getServiceProviders(), getServiceCapabilitystList(), getServiceOfferingId());
        if (vpcOff != null) {
            setEntityId(vpcOff.getId());
            setEntityUuid(vpcOff.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create a VPC offering");
        }
    }

    public String getVpcOfferingName() {
        return vpcOfferingName;
    }

    public String getDisplayText() {
        return displayText;
    }

    public List<String> getSupportedServices() {
        return supportedServices;
    }

    public Map<String, List<String>> getServiceProviders() {
        Map<String, List<String>> serviceProviderMap = null;
        if (serviceProviderList != null && !serviceProviderList.isEmpty()) {
            serviceProviderMap = new HashMap<>();
            final Collection<? extends Map<String, String>> servicesCollection = serviceProviderList.values();
            final Iterator<? extends Map<String, String>> iter = servicesCollection.iterator();
            while (iter.hasNext()) {
                final Map<String, String> obj = iter.next();
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("service provider entry specified: " + obj);
                }
                final HashMap<String, String> services = (HashMap<String, String>) obj;
                final String service = services.get("service");
                final String provider = services.get("provider");
                List<String> providerList = null;
                if (serviceProviderMap.containsKey(service)) {
                    providerList = serviceProviderMap.get(service);
                } else {
                    providerList = new ArrayList<>();
                }
                providerList.add(provider);
                serviceProviderMap.put(service, providerList);
            }
        }

        return serviceProviderMap;
    }

    public Map<String, List<String>> getServiceCapabilitystList() {
        return serviceCapabilitystList;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public void execute() {
        final VpcOffering vpc = _vpcProvSvc.getVpcOffering(getEntityId());
        if (vpc != null) {
            final VpcOfferingResponse response = _responseGenerator.createVpcOfferingResponse(vpc);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create VPC offering");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VPC_OFFERING_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating VPC offering. Id: " + getEntityId();
    }
}
