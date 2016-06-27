package org.apache.cloudstack.api.command.admin.host;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiConstants.HostDetails;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ClusterResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listHosts", description = "Lists hosts.", responseObject = HostResponse.class,
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
                    _mgr.listHostsForMigrationOfVM(getVirtualMachineId(), getStartIndex(), getPageSizeVal());
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
