//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachOrDettachConfigDriveCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.List;
import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.VBD;
import com.xensource.xenapi.VDI;
import com.xensource.xenapi.VM;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = AttachOrDettachConfigDriveCommand.class)
public final class CitrixAttachOrDettachConfigDriveCommandWrapper extends CommandWrapper<AttachOrDettachConfigDriveCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixAttachOrDettachConfigDriveCommandWrapper.class);

    @Override
    public Answer execute(final AttachOrDettachConfigDriveCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();

        final String vmName = command.getVmName();
        final List<String[]> vmData = command.getVmData();
        final String label = command.getConfigDriveLabel();
        final Boolean isAttach = command.isAttach();

        try {
            final Set<VM> vms = VM.getByNameLabel(conn, vmName);
            for (final VM vm : vms) {
                if (isAttach) {
                    if (!citrixResourceBase.createAndAttachConfigDriveIsoForVM(conn, vm, vmData, label)) {
                        s_logger.debug("Failed to attach config drive iso to VM " + vmName);
                    }
                } else {
                    // delete the config drive iso attached to VM
                    final Set<VDI> vdis = VDI.getByNameLabel(conn, vmName + ".iso");
                    if (vdis != null && !vdis.isEmpty()) {
                        s_logger.debug("Deleting config drive for the VM " + vmName);
                        final VDI vdi = vdis.iterator().next();
                        // Find the VM's CD-ROM VBD
                        final Set<VBD> vbds = vdi.getVBDs(conn);

                        for (final VBD vbd : vbds) {
                            final VBD.Record vbdRec = vbd.getRecord(conn);

                            if (vbdRec.type.equals(Types.VbdType.CD) && !vbdRec.empty && !vbdRec.userdevice.equals(citrixResourceBase._attachIsoDeviceNum)) {
                                if (vbdRec.currentlyAttached) {
                                    vbd.eject(conn);
                                }
                                vbd.destroy(conn);
                            }
                        }
                        vdi.destroy(conn);
                    }

                    s_logger.debug("Successfully dettached config drive iso from the VM " + vmName);
                }
            }
        } catch (final Types.XenAPIException ex) {
            s_logger.debug("Failed to attach config drive iso to VM " + vmName + " " + ex.getMessage());
        } catch (final XmlRpcException ex) {
            s_logger.debug("Failed to attach config drive iso to VM " + vmName + " " + ex.getMessage());
        }

        return new Answer(command, true, "success");
    }
}
