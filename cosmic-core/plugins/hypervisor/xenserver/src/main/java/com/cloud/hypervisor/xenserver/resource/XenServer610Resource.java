package com.cloud.hypervisor.xenserver.resource;

import com.cloud.agent.api.to.DiskTO;
import com.cloud.resource.ServerResource;
import com.cloud.storage.Volume;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import javax.ejb.Local;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VBD;
import com.xensource.xenapi.VDI;
import com.xensource.xenapi.VIF;
import com.xensource.xenapi.VM;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Local(value = ServerResource.class)
public class XenServer610Resource extends XenServer600Resource {

    private static final Logger s_logger = LoggerFactory.getLogger(XenServer610Resource.class);

    public List<VolumeObjectTO> getUpdatedVolumePathsOfMigratedVm(final Connection connection, final VM migratedVm, final DiskTO[] volumes) throws CloudRuntimeException {
        final List<VolumeObjectTO> volumeToList = new ArrayList<>();

        try {
            // Volume paths would have changed. Return that information.
            final Set<VBD> vbds = migratedVm.getVBDs(connection);
            final Map<String, VDI> deviceIdToVdiMap = new HashMap<>();
            // get vdi:vbdr to a map
            for (final VBD vbd : vbds) {
                final VBD.Record vbdr = vbd.getRecord(connection);
                if (vbdr.type == Types.VbdType.DISK) {
                    final VDI vdi = vbdr.VDI;
                    deviceIdToVdiMap.put(vbdr.userdevice, vdi);
                }
            }

            for (final DiskTO volumeTo : volumes) {
                if (volumeTo.getType() != Volume.Type.ISO) {
                    final VolumeObjectTO vol = (VolumeObjectTO) volumeTo.getData();
                    final Long deviceId = volumeTo.getDiskSeq();
                    final VDI vdi = deviceIdToVdiMap.get(deviceId.toString());
                    final VolumeObjectTO newVol = new VolumeObjectTO();
                    newVol.setPath(vdi.getUuid(connection));
                    newVol.setId(vol.getId());
                    volumeToList.add(newVol);
                }
            }
        } catch (final Exception e) {
            s_logger.error("Unable to get the updated VDI paths of the migrated vm " + e.toString(), e);
            throw new CloudRuntimeException("Unable to get the updated VDI paths of the migrated vm " + e.toString(), e);
        }

        return volumeToList;
    }

    @Override
    protected void plugDom0Vif(final Connection conn, final VIF dom0Vif) throws XmlRpcException, XenAPIException {
        // do nothing. In xenserver 6.1 and beyond this step isn't needed.
    }
}
