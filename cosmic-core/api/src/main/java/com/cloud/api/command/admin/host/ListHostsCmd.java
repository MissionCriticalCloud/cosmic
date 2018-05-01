package com.cloud.api.command.admin.host;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiConstants.HostDetails;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.host.Host;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.utils.Ternary;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listHosts", group = APICommandGroup.HostService, description = "Lists hosts.", responseObject = HostResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListHostsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListHostsCmd.class.getName());

    private static final String s_name = "listhostsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.CLUSTER_ID, type = CommandType.UUID, entityType = ClusterResponse.class, description = "lists hosts existing in particular cluster")
    private Long clusterId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = HostResponse.class, description = "the id of the host")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the host")
    private String hostName;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "the Pod ID for the host")
    private Long podId;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "the state of the host")
    private String state;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "the host type")
    private String type;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the Zone ID for the host")
    private Long zoneId;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            required = false,
            description = "lists hosts in the same cluster as this VM and flag hosts with enough CPU/RAm to host this VM")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.RESOURCE_STATE,
            type = CommandType.STRING,
            description = "list hosts by resource state. Resource state represents current state determined by admin of host, valule can be one of [Enabled, Disabled, Unmanaged," +
                    " PrepareForMaintenance, ErrorInMaintenance, Maintenance, Error]")
    private String resourceState;

    @Parameter(name = ApiConstants.DETAILS,
            type = CommandType.LIST,
            collectionType = CommandType.STRING,
            description = "comma separated list of host details requested, value can be a list of [ min, all, capacity, events, stats]")
    private List<String> viewDetails;

    @Parameter(name = ApiConstants.HA_HOST, type = CommandType.BOOLEAN, description = "if true, list only hosts dedicated to HA")
    private Boolean haHost;

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, description = "hypervisor type of host: XenServer,KVM")
    private String hypervisor;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getClusterId() {
        return clusterId;
    }

    public Long getId() {
        return id;
    }

    public String getHostName() {
        return hostName;
    }

    public Long getPodId() {
        return podId;
    }

    public String getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public Boolean getHaHost() {
        return haHost;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public HypervisorType getHypervisor() {
        return HypervisorType.getType(hypervisor);
    }

    public String getResourceState() {
        return resourceState;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Host;
    }

    @Override
    public void execute() {
        ListResponse<HostResponse> response = null;
        if (getVirtualMachineId() == null) {
            response = _queryService.searchForServers(this);
        } else {
            final Pair<List<? extends Host>, Integer> result;
            final Ternary<Pair<List<? extends Host>, Integer>, List<? extends Host>, Map<Host, Boolean>> hostsForMigration =
                    _mgr.listHostsForMigrationOfVM(getVirtualMachineId(), getStartIndex(), getPageSizeVal(), null);
            result = hostsForMigration.first();
            final List<? extends Host> hostsWithCapacity = hostsForMigration.second();

            response = new ListResponse<>();
            final List<HostResponse> hostResponses = new ArrayList<>();
            for (final Host host : result.first()) {
                final HostResponse hostResponse = _responseGenerator.createHostResponse(host, getDetails());
                Boolean suitableForMigration = false;
                if (hostsWithCapacity.contains(host)) {
                    suitableForMigration = true;
                }
                hostResponse.setSuitableForMigration(suitableForMigration);
                hostResponse.setObjectName("host");
                hostResponses.add(hostResponse);
            }

            response.setResponses(hostResponses, result.second());
        }
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    public EnumSet<HostDetails> getDetails() throws InvalidParameterValueException {
        final EnumSet<HostDetails> dv;
        if (viewDetails == null || viewDetails.size() <= 0) {
            dv = EnumSet.of(HostDetails.all);
        } else {
            try {
                final ArrayList<HostDetails> dc = new ArrayList<>();
                for (final String detail : viewDetails) {
                    dc.add(HostDetails.valueOf(detail));
                }
                dv = EnumSet.copyOf(dc);
            } catch (final IllegalArgumentException e) {
                throw new InvalidParameterValueException("The details parameter contains a non permitted value. The allowed values are " +
                        EnumSet.allOf(HostDetails.class));
            }
        }
        return dv;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
