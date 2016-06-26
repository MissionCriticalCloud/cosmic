//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.vm.DiskProfile;

import java.util.HashMap;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.VDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CreateCommand.class)
public final class CitrixCreateCommandWrapper extends CommandWrapper<CreateCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixCreateCommandWrapper.class);

    @Override
    public Answer execute(final CreateCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final StorageFilerTO pool = command.getPool();
        final DiskProfile dskch = command.getDiskCharacteristics();

        VDI vdi = null;
        try {
            final SR poolSr = citrixResourceBase.getStorageRepository(conn, pool.getUuid());
            if (command.getTemplateUrl() != null) {
                VDI tmpltvdi = null;

                tmpltvdi = citrixResourceBase.getVDIbyUuid(conn, command.getTemplateUrl());
                vdi = tmpltvdi.createClone(conn, new HashMap<>());
                vdi.setNameLabel(conn, dskch.getName());
            } else {
                final VDI.Record vdir = new VDI.Record();
                vdir.nameLabel = dskch.getName();
                vdir.SR = poolSr;
                vdir.type = Types.VdiType.USER;

                vdir.virtualSize = dskch.getSize();
                vdi = VDI.create(conn, vdir);
            }

            final VDI.Record vdir;
            vdir = vdi.getRecord(conn);

            s_logger.debug("Succesfully created VDI for " + command + ".  Uuid = " + vdir.uuid);

            final VolumeTO vol =
                    new VolumeTO(command.getVolumeId(), dskch.getType(), pool.getType(), pool.getUuid(), vdir.nameLabel, pool.getPath(), vdir.uuid, vdir.virtualSize, null);

            return new CreateAnswer(command, vol);
        } catch (final Exception e) {
            s_logger.warn("Unable to create volume; Pool=" + pool + "; Disk: " + dskch, e);
            return new CreateAnswer(command, e);
        }
    }
}
