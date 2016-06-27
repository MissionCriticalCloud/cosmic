package org.apache.cloudstack.api.command.admin.host;

import com.cloud.host.Host;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.HostForMigrationResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.UserVmResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "findHostsForMigration", description = "Find hosts suitable for migrating a virtual machine.", responseObject = HostForMigrationResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class FindHostsForMigrationCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(FindHostsForMigrationCmd.class.getName());

    private static final String s_name = "findhostsformigrationresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            required = true,
            description = "find hosts to which this VM can be migrated and flag the hosts with enough " + "CPU/RAM to host the VM")
    private Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        ListResponse<HostForMigrationResponse> response = null;
        final Pair<List<? extends Host>, Integer> result;
        final Map<Host, Boolean> hostsRequiringStorageMotion;

        final Ternary<Pair<List<? extends Host>, Integer>, List<? extends Host>, Map<Host, Boolean>> hostsForMigration =
                _mgr.listHostsForMigrationOfVM(getVirtualMachineId(), this.getStartIndex(), this.getPageSizeVal());
        result = hostsForMigration.first();
        final List<? extends Host> hostsWithCapacity = hostsForMigration.second();
        hostsRequiringStorageMotion = hostsForMigration.third();

        response = new ListResponse<>();
        final List<HostForMigrationResponse> hostResponses = new ArrayList<>();
        for (final Host host : result.first()) {
            final HostForMigrationResponse hostResponse = _responseGenerator.createHostForMigrationResponse(host);
            Boolean suitableForMigration = false;
            if (hostsWithCapacity.contains(host)) {
                suitableForMigration = true;
            }
            hostResponse.setSuitableForMigration(suitableForMigration);

            final Boolean requiresStorageMotion = hostsRequiringStorageMotion.get(host);
            if (requiresStorageMotion != null && requiresStorageMotion) {
                hostResponse.setRequiresStorageMotion(true);
            } else {
                hostResponse.setRequiresStorageMotion(false);
            }

            hostResponse.setObjectName("host");
            hostResponses.add(hostResponse);
        }

        response.setResponses(hostResponses, result.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
