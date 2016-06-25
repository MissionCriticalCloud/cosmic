package com.cloud.hypervisor.ovm3.resources.helpers;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.MigrateAnswer;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.PlugNicAnswer;
import com.cloud.agent.api.PlugNicCommand;
import com.cloud.agent.api.PrepareForMigrationAnswer;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.UnPlugNicAnswer;
import com.cloud.agent.api.UnPlugNicCommand;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.ovm3.objects.CloudstackPlugin;
import com.cloud.hypervisor.ovm3.objects.Connection;
import com.cloud.hypervisor.ovm3.objects.Ovm3ResourceException;
import com.cloud.hypervisor.ovm3.objects.OvmObject;
import com.cloud.hypervisor.ovm3.objects.Xen;
import com.cloud.hypervisor.ovm3.resources.Ovm3StorageProcessor;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.Volume;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine.State;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ovm3VmSupport {

    private final Logger logger = LoggerFactory.getLogger(Ovm3VmSupport.class);
    private final OvmObject ovmObject = new OvmObject();
    private final Connection connection;
    private final Ovm3HypervisorNetwork network;
    private final Ovm3Configuration config;
    private final Ovm3HypervisorSupport hypervisor;
    private final Ovm3StorageProcessor processor;
    private final Ovm3StoragePool pool;
    private final Map<String, Map<String, String>> vmStats = new ConcurrentHashMap<>();
    private ResourceManager resourceMgr;

    public Ovm3VmSupport(final Connection conn,
                         final Ovm3Configuration ovm3config,
                         final Ovm3HypervisorSupport ovm3hyper,
                         final Ovm3StorageProcessor ovm3stp,
                         final Ovm3StoragePool ovm3sp,
                         final Ovm3HypervisorNetwork ovm3hvn) {
        connection = conn;
        config = ovm3config;
        hypervisor = ovm3hyper;
        pool = ovm3sp;
        processor = ovm3stp;
        network = ovm3hvn;
    }

    public Boolean createVifs(final Xen.Vm vm, final VirtualMachineTO spec)
            throws Ovm3ResourceException {
        if (spec.getNics() != null) {
            final NicTO[] nics = spec.getNics();
            return createVifs(vm, nics);
        } else {
            logger.info("No nics for vm " + spec.getName());
            return false;
        }
    }

    private Boolean createVifs(final Xen.Vm vm, final NicTO[] nics)
            throws Ovm3ResourceException {
        for (final NicTO nic : nics) {
            if (!createVif(vm, nic)) {
                return false;
            }
        }
        return true;
    }

    /* should add bitrates and latency... */
    private Boolean createVif(final Xen.Vm vm, final NicTO nic)
            throws Ovm3ResourceException {
        try {
            final String net = network.getNetwork(nic);
            if (net != null) {
                logger.debug("Adding vif " + nic.getDeviceId() + " "
                        + nic.getMac() + " " + net + " to " + vm.getVmName());
                vm.addVif(nic.getDeviceId(), net, nic.getMac());
            } else {
                logger.debug("Unable to add vif " + nic.getDeviceId() + " no network for " + vm.getVmName());
                return false;
            }
        } catch (final Exception e) {
            final String msg = "Unable to add vif " + nic.getType() + " for "
                    + vm.getVmName() + " " + e.getMessage();
            logger.debug(msg);
            throw new Ovm3ResourceException(msg);
        }
        return true;
    }

    /* Migration should make sure both HVs are the same ? */
    public PrepareForMigrationAnswer execute(final PrepareForMigrationCommand cmd) {
        final VirtualMachineTO vm = cmd.getVirtualMachine();
        if (logger.isDebugEnabled()) {
            logger.debug("Preparing host for migrating " + vm.getName());
        }
        final NicTO[] nics = vm.getNics();
        try {
            for (final NicTO nic : nics) {
                network.getNetwork(nic);
            }
            hypervisor.setVmState(vm.getName(), State.Migrating);
            logger.debug("VM " + vm.getName() + " is in Migrating state");
            return new PrepareForMigrationAnswer(cmd);
        } catch (final Ovm3ResourceException e) {
            logger.error("Catch Exception " + e.getClass().getName()
                    + " prepare for migration failed due to: " + e.getMessage());
            return new PrepareForMigrationAnswer(cmd, e);
        }
    }

    /* do migrations of VMs in a simple way just inside a cluster for now */
    public MigrateAnswer execute(final MigrateCommand cmd) {
        final String vmName = cmd.getVmName();
        final String destUuid = cmd.getHostGuid();
        final String destIp = cmd.getDestinationIp();
        State state = State.Error;
    /*
     * TODO: figure out non pooled migration, works from CLI but not from the agent... perhaps pause the VM and then
     * migrate it ? for now just stop the VM.
     */
        String msg = "Migrating " + vmName + " to " + destIp;
        logger.info(msg);
        if (!config.getAgentInOvm3Cluster() && !config.getAgentInOvm3Pool()) {
            try {
                final Xen xen = new Xen(connection);
                final Xen.Vm vm = xen.getRunningVmConfig(vmName);
                final HostVO destHost = resourceMgr.findHostByGuid(destUuid);
                if (destHost == null) {
                    msg = "Unable to find migration target host in DB "
                            + destUuid + " with ip " + destIp;
                    logger.info(msg);
                    return new MigrateAnswer(cmd, false, msg, null);
                }
                xen.stopVm(ovmObject.deDash(vm.getVmRootDiskPoolId()),
                        vm.getVmUuid());
                msg = destHost.toString();
                state = State.Stopping;
                return new MigrateAnswer(cmd, false, msg, null);
            } catch (final Ovm3ResourceException e) {
                msg = "Unpooled VM Migrate of " + vmName + " to " + destUuid
                        + " failed due to: " + e.getMessage();
                logger.debug(msg, e);
                return new MigrateAnswer(cmd, false, msg, null);
            } finally {
        /* shouldn't we just reinitialize completely as a last resort ? */
                hypervisor.setVmState(vmName, state);
            }
        } else {
            try {
                final Xen xen = new Xen(connection);
                final Xen.Vm vm = xen.getRunningVmConfig(vmName);
                if (vm == null) {
                    state = State.Stopped;
                    msg = vmName + " is no running on " + config.getAgentHostname();
                    return new MigrateAnswer(cmd, false, msg, null);
                }
        /* not a storage migration!!! */
                xen.migrateVm(ovmObject.deDash(vm.getVmRootDiskPoolId()),
                        vm.getVmUuid(), destIp);
                state = State.Stopping;
                msg = "Migration of " + vmName + " successfull";
                return new MigrateAnswer(cmd, true, msg, null);
            } catch (final Ovm3ResourceException e) {
                msg = "Pooled VM Migrate" + ": Migration of " + vmName + " to "
                        + destIp + " failed due to " + e.getMessage();
                logger.debug(msg, e);
                return new MigrateAnswer(cmd, false, msg, null);
            } finally {
                hypervisor.setVmState(vmName, state);
            }
        }
    }

    /*
     */
    public GetVncPortAnswer execute(final GetVncPortCommand cmd) {
        try {
            final Xen host = new Xen(connection);
            final Xen.Vm vm = host.getRunningVmConfig(cmd.getName());
            final Integer vncPort = vm.getVncPort();
            logger.debug("get vnc port for " + cmd.getName() + ": " + vncPort);
            return new GetVncPortAnswer(cmd, connection.getIp(), vncPort);
        } catch (final Ovm3ResourceException e) {
            logger.debug("get vnc port for " + cmd.getName() + " failed", e);
            return new GetVncPortAnswer(cmd, e.getMessage());
        }
    }

    public GetVmStatsAnswer execute(final GetVmStatsCommand cmd) {
        final List<String> vmNames = cmd.getVmNames();
        final Map<String, VmStatsEntry> vmStatsNameMap = new HashMap<>();
        for (final String vmName : vmNames) {
            final VmStatsEntry e = getVmStat(vmName);
            vmStatsNameMap.put(vmName, e);
        }
        return new GetVmStatsAnswer(cmd,
                (HashMap<String, VmStatsEntry>) vmStatsNameMap);
    }

    private VmStatsEntry getVmStat(final String vmName) {
        final CloudstackPlugin cSp = new CloudstackPlugin(connection);
        Map<String, String> oldVmStats = null;
        Map<String, String> newVmStats = null;
        final VmStatsEntry stats = new VmStatsEntry();
        try {
            if (vmStats.containsKey(vmName)) {
                oldVmStats = new HashMap<>();
                oldVmStats.putAll(vmStats.get(vmName));
            }
            newVmStats = cSp.ovsDomUStats(vmName);
        } catch (final Ovm3ResourceException e) {
            logger.info("Unable to retrieve stats from " + vmName, e);
            return stats;
        }
        if (oldVmStats == null) {
            logger.debug("No old stats retrieved stats from " + vmName);
            stats.setNumCPUs(1);
            stats.setNetworkReadKBs(0);
            stats.setNetworkWriteKBs(0);
            stats.setDiskReadKBs(0);
            stats.setDiskWriteKBs(0);
            stats.setDiskReadIOs(0);
            stats.setDiskWriteIOs(0);
            stats.setCPUUtilization(0);
            stats.setEntityType("vm");
        } else {
            logger.debug("Retrieved new stats from " + vmName);
            final int cpus = Integer.parseInt(newVmStats.get("vcpus"));
            stats.setNumCPUs(cpus);
            stats.setNetworkReadKBs(doubleMin(newVmStats.get("rx_bytes"), oldVmStats.get("rx_bytes")));
            stats.setNetworkWriteKBs(doubleMin(newVmStats.get("tx_bytes"), oldVmStats.get("tx_bytes")));
            stats.setDiskReadKBs(doubleMin(newVmStats.get("rd_bytes"), oldVmStats.get("rd_bytes")));
            stats.setDiskWriteKBs(doubleMin(newVmStats.get("rw_bytes"), oldVmStats.get("rw_bytes")));
            stats.setDiskReadIOs(doubleMin(newVmStats.get("rd_ops"), oldVmStats.get("rd_ops")));
            stats.setDiskWriteIOs(doubleMin(newVmStats.get("rw_ops"), oldVmStats.get("rw_ops")));
            final Double dCpu = doubleMin(newVmStats.get("cputime"), oldVmStats.get("cputime"));
            final Double dTime = doubleMin(newVmStats.get("uptime"), oldVmStats.get("uptime"));
            final Double cpupct = dCpu / dTime * 100 * cpus;
            stats.setCPUUtilization(cpupct);
            stats.setEntityType("vm");
        }
        ((ConcurrentHashMap<String, Map<String, String>>) vmStats).put(
                vmName, newVmStats);
        return stats;
    }

    private Double doubleMin(final String operatorX, final String operatorY) {
        try {
            return Double.parseDouble(operatorX) - Double.parseDouble(operatorY);
        } catch (final NullPointerException e) {
            return 0D;
        }
    }

    public PlugNicAnswer execute(final PlugNicCommand cmd) {
        final Answer ans = plugNunplugNic(cmd.getNic(), cmd.getVmName(), true);
        return new PlugNicAnswer(cmd, ans.getResult(), ans.getDetails());
    }

    private Answer plugNunplugNic(final NicTO nic, final String vmName, final Boolean plug) {
        try {
            final Xen xen = new Xen(connection);
            final Xen.Vm vm = xen.getVmConfig(vmName);
      /* check running */
            if (vm == null) {
                return new Answer(null, false,
                        "Unable to execute command due to missing VM");
            }
            // setup the NIC in the VM config.
            if (plug) {
                createVif(vm, nic);
                vm.setupVifs();
            } else {
                deleteVif(vm, nic);
            }
            // execute the change
            xen.configureVm(ovmObject.deDash(vm.getPrimaryPoolUuid()),
                    vm.getVmUuid());
        } catch (final Ovm3ResourceException e) {
            final String msg = "Unable to execute command due to " + e.toString();
            logger.debug(msg);
            return new Answer(null, false, msg);
        }
        return new Answer(null, true, "success");
    }

    private Boolean deleteVif(final Xen.Vm vm, final NicTO nic)
            throws Ovm3ResourceException {
    /*
     * here we should use the housekeeping of VLANs/Networks etc.. so we can clean after the last VM is gone
     */
        try {
            final String net = network.getNetwork(nic);
            if (net != null) {
                logger.debug("Removing vif " + nic.getDeviceId() + " " + " "
                        + nic.getMac() + " " + net + " from " + vm.getVmName());
                vm.removeVif(net, nic.getMac());
            } else {
                logger.debug("Unable to remove vif " + nic.getDeviceId() + " no network for " + vm.getVmName());
                return false;
            }
        } catch (final Exception e) {
            final String msg = "Unable to remove vif " + nic.getType() + " for "
                    + vm.getVmName() + " " + e.getMessage();
            logger.debug(msg);
            throw new Ovm3ResourceException(msg);
        }
        return true;
    }

    public UnPlugNicAnswer execute(final UnPlugNicCommand cmd) {
        final Answer ans = plugNunplugNic(cmd.getNic(), cmd.getVmName(), false);
        return new UnPlugNicAnswer(cmd, ans.getResult(), ans.getDetails());
    }

    public void cleanup(final Xen.Vm vm) {
        try {
            cleanupNetwork(vm.getVmVifs());
        } catch (final XmlRpcException e) {
            logger.info("Clean up network for " + vm.getVmName() + " failed", e);
        }
        final String vmName = vm.getVmName();
    /* should become a single entity */
        vmStats.remove(vmName);
    }

    /* This is not create for us, but really start */
  /*
   * public boolean startVm(String repoId, String vmId) throws XmlRpcException { Xen host = new Xen(c); try { if
   * (host.getRunningVmConfig(vmId) == null) { LOGGER.error("Create VM " + vmId + " first on " + c.getIp()); return
   * false; } else { LOGGER.info("VM " + vmId + " exists on " + c.getIp()); } host.startVm(repoId, vmId); } catch
   * (Exception e) { LOGGER.error("Failed to start VM " + vmId + " on " + c.getIp() + " " + e.getMessage()); return
   * false; } return true; }
   */
  /*
   * TODO: OVM already cleans stuff up, just not the extra bridges which we don't want right now, as we'd have to keep a
   * state table of which vlans need to stay on the host!? A map with vlanid -> list-o-hosts
   */
    private void cleanupNetwork(final List<String> vifs) throws XmlRpcException {
    /* peel out vif info for vlan stuff */
    }

    /*
     * Add rootdisk, datadisk and iso's
     */
    public Boolean createVbds(final Xen.Vm vm, final VirtualMachineTO spec) {
        if (spec.getDisks() == null) {
            logger.info("No disks defined for " + vm.getVmName());
            return false;
        }
        for (final DiskTO disk : spec.getDisks()) {
            try {
                if (disk.getType() == Volume.Type.ROOT) {
                    final VolumeObjectTO vol = (VolumeObjectTO) disk.getData();
                    final String diskFile = processor.getVirtualDiskPath(vol.getUuid(), vol.getDataStore().getUuid());
                    vm.addRootDisk(diskFile);
                    vm.setPrimaryPoolUuid(vol.getDataStore().getUuid());
                    logger.debug("Adding root disk: " + diskFile);
                } else if (disk.getType() == Volume.Type.ISO) {
                    final DataTO isoTo = disk.getData();
                    if (isoTo.getPath() != null) {
                        final TemplateObjectTO template = (TemplateObjectTO) isoTo;
                        final DataStoreTO store = template.getDataStore();
                        if (!(store instanceof NfsTO)) {
                            throw new CloudRuntimeException(
                                    "unsupported protocol");
                        }
                        final NfsTO nfsStore = (NfsTO) store;
                        final String secPoolUuid = pool.setupSecondaryStorage(nfsStore.getUrl());
                        final String isoPath = config.getAgentSecStoragePath() + "/"
                                + secPoolUuid + "/"
                                + template.getPath();
                        vm.addIso(isoPath);
            /* check if secondary storage is mounted */
                        logger.debug("Adding ISO: " + isoPath);
                    }
                } else if (disk.getType() == Volume.Type.DATADISK) {
                    final VolumeObjectTO vol = (VolumeObjectTO) disk.getData();
                    final String diskFile = processor.getVirtualDiskPath(vol.getUuid(), vol.getDataStore().getUuid());
                    vm.addDataDisk(diskFile);
                    logger.debug("Adding data disk: "
                            + diskFile);
                } else {
                    throw new CloudRuntimeException("Unknown disk type: "
                            + disk.getType());
                }
            } catch (final Exception e) {
                logger.debug("CreateVbds failed", e);
                throw new CloudRuntimeException("Exception" + e.getMessage(), e);
            }
        }
        return true;
    }
}
