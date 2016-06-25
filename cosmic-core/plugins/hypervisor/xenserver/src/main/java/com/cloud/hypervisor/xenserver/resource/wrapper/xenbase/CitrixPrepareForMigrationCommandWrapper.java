//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.PrepareForMigrationAnswer;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.List;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = PrepareForMigrationCommand.class)
public final class CitrixPrepareForMigrationCommandWrapper extends CommandWrapper<PrepareForMigrationCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixPrepareForMigrationCommandWrapper.class);

    @Override
    public Answer execute(final PrepareForMigrationCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();

        final VirtualMachineTO vm = command.getVirtualMachine();
        final List<String[]> vmDataList = vm.getVmData();
        String configDriveLabel = vm.getConfigDriveLabel();

        if (configDriveLabel == null) {
            configDriveLabel = "config";
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Preparing host for migrating " + vm);
        }

        final NicTO[] nics = vm.getNics();
        try {
            citrixResourceBase.prepareISO(conn, vm.getName(), vmDataList, configDriveLabel);

            for (final NicTO nic : nics) {
                citrixResourceBase.getNetwork(conn, nic);
            }
            s_logger.debug("4. The VM " + vm.getName() + " is in Migrating state");

            return new PrepareForMigrationAnswer(command);
        } catch (final Exception e) {
            s_logger.warn("Catch Exception " + e.getClass().getName() + " prepare for migration failed due to " + e.toString(), e);
            return new PrepareForMigrationAnswer(command, e);
        }
    }
}
