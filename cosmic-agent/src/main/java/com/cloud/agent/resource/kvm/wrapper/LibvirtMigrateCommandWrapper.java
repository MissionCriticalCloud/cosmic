package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.async.MigrateKvmAsync;
import com.cloud.agent.resource.kvm.vif.VifDriver;
import com.cloud.agent.resource.kvm.xml.LibvirtDiskDef;
import com.cloud.agent.resource.kvm.xml.LibvirtVmDef;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.MigrateAnswer;
import com.cloud.legacymodel.communication.command.MigrateCommand;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.utils.Ternary;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = MigrateCommand.class)
public final class LibvirtMigrateCommandWrapper extends LibvirtCommandWrapper<MigrateCommand, Answer, LibvirtComputingResource> {

    private static final String GRAPHICS_ELEM_END = "/graphics>";
    private static final String GRAPHICS_ELEM_START = "<graphics";
    private static final String CONTENTS_WILDCARD = "(?s).*";
    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtMigrateCommandWrapper.class);

    @Override
    public Answer execute(final MigrateCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final String vmName = command.getVmName();

        String result = null;

        List<LibvirtVmDef.InterfaceDef> ifaces = null;
        final List<LibvirtDiskDef> disks;

        Domain dm = null;
        Connect dconn = null;
        Domain destDomain = null;
        Connect conn = null;
        final String xmlDesc;
        List<Ternary<String, Boolean, String>> vmsnapshots = null;
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

            conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
            ifaces = libvirtComputingResource.getInterfaces(conn, vmName);
            disks = libvirtComputingResource.getDisks(conn, vmName);
            dm = conn.domainLookupByName(vmName);

            VirtualMachineTO to = command.getVirtualMachine();

            /*
             * We replace the private IP address with the address of the destination host. This is because the VNC listens on
             * the private IP address of the hypervisor, but that address is ofcourse different on the target host.
             *
             * MigrateCommand.getDestinationIp() returns the private IP address of the target hypervisor. So it's safe to use.
             *
             * The Domain.migrate method from libvirt supports passing a different XML description for the instance to be used
             * on the target host.
             *
             * This is supported by libvirt-java from version 0.50.0
             *
             * CVE-2015-3252: Get XML with sensitive information suitable for migration by using VIR_DOMAIN_XML_MIGRATABLE
             * flag (value = 8) https://libvirt.org/html/libvirt-libvirt-domain.html#virDomainXMLFlags
             *
             * Use VIR_DOMAIN_XML_SECURE (value = 1) prior to v1.0.0.
             */
            final int xmlFlag = conn.getLibVirVersion() >= 1000000 ? 8 : 1; // 1000000 equals v1.0.0

            final String target = command.getDestinationIp();

            // xmlDesc = dm.getXMLDesc(xmlFlag).replace(libvirtComputingResource.getPrivateIp(), command.getDestinationIp());

            String vncPassword = to.getVncPassword();
            xmlDesc = replaceIpForVNCInDescFileAndNormalizePassword(dm.getXMLDesc(xmlFlag), target, vncPassword);

            // delete the metadata of vm snapshots before migration
            vmsnapshots = libvirtComputingResource.cleanVMSnapshotMetadata(dm);

            dconn = libvirtUtilitiesHelper.retrieveQemuConnection("qemu+tcp://" + command.getDestinationIp() + "/system");

            // run migration in thread so we can monitor it
            s_logger.info("Live migration of instance " + vmName + " initiated");
            final ExecutorService executor = Executors.newFixedThreadPool(1);
            final Callable<Domain> worker = new MigrateKvmAsync(libvirtComputingResource, dm, dconn, xmlDesc, vmName,
                    command.getDestinationIp());
            final Future<Domain> migrateThread = executor.submit(worker);
            executor.shutdown();
            long sleeptime = 0;
            while (!executor.isTerminated()) {
                Thread.sleep(100);
                sleeptime += 100;
                if (sleeptime == 1000) {
                    final int migrateDowntime = libvirtComputingResource.getMigrateDowntime();
                    if (migrateDowntime > 0) {
                        try {
                            final int setDowntime = dm.migrateSetMaxDowntime(migrateDowntime);
                            if (setDowntime == 0) {
                                s_logger.debug("Set max downtime for migration of " + vmName + " to " + String.valueOf(migrateDowntime) + "ms");
                            }
                        } catch (final LibvirtException e) {
                            s_logger.debug("Failed to set max downtime for migration, perhaps migration completed? Error: " + e.getMessage());
                        }
                    }
                }
                if (sleeptime % 1000 == 0) {
                    s_logger.info("Waiting for migration of " + vmName + " to complete, waited " + sleeptime + "ms");
                }

                // pause vm if we meet the vm.migrate.pauseafter threshold and not already paused
                final int migratePauseAfter = libvirtComputingResource.getMigratePauseAfter();
                if (migratePauseAfter > 0 && sleeptime > migratePauseAfter) {
                    DomainState state = null;
                    try {
                        state = dm.getInfo().state;
                    } catch (final LibvirtException e) {
                        s_logger.info("Couldn't get VM domain state after " + sleeptime + "ms: " + e.getMessage());
                    }
                    if (state != null && state == DomainState.VIR_DOMAIN_RUNNING) {
                        try {
                            s_logger.info("Pausing VM " + vmName + " due to property vm.migrate.pauseafter setting to " + migratePauseAfter + "ms to complete migration");
                            dm.suspend();
                        } catch (final LibvirtException e) {
                            // pause could be racy if it attempts to pause right when vm is finished, simply warn
                            s_logger.info("Failed to pause vm " + vmName + " : " + e.getMessage());
                        }
                    }
                }
            }
            s_logger.info("Migration thread for " + vmName + " is done");

            destDomain = migrateThread.get(10, TimeUnit.SECONDS);

            if (destDomain != null) {
                for (final LibvirtDiskDef disk : disks) {
                    libvirtComputingResource.cleanupDisk(disk);
                }
            }
        } catch (final LibvirtException e) {
            s_logger.debug("Can't migrate domain: " + e.getMessage());
            result = e.getMessage();
        } catch (final InterruptedException e) {
            s_logger.debug("Interrupted while migrating domain: " + e.getMessage());
            result = e.getMessage();
        } catch (final ExecutionException e) {
            s_logger.debug("Failed to execute while migrating domain: " + e.getMessage());
            result = e.getMessage();
        } catch (final TimeoutException e) {
            s_logger.debug("Timed out while migrating domain: " + e.getMessage());
            result = e.getMessage();
        } finally {
            try {
                if (dm != null && result != null) {
                    // restore vm snapshots in case of failed migration
                    if (vmsnapshots != null) {
                        libvirtComputingResource.restoreVMSnapshotMetadata(dm, vmName, vmsnapshots);
                    }
                }
                if (dm != null) {
                    if (dm.isPersistent() == 1) {
                        dm.undefine();
                    }
                    dm.free();
                }
                if (dconn != null) {
                    dconn.close();
                }
                if (destDomain != null) {
                    destDomain.free();
                }
            } catch (final LibvirtException e) {
                s_logger.trace("Ignoring libvirt error.", e);
            }
        }

        if (result == null) {
            for (final LibvirtVmDef.InterfaceDef iface : ifaces) {
                // We don't know which "traffic type" is associated with
                // each interface at this point, so inform all vif drivers
                final List<VifDriver> allVifDrivers = libvirtComputingResource.getAllVifDrivers();
                for (final VifDriver vifDriver : allVifDrivers) {
                    vifDriver.unplug(iface);
                }
            }
        }

        return new MigrateAnswer(command, result == null, result, null);
    }

    /**
     * This function assumes an qemu machine description containing a single graphics element like
     *     <graphics type='vnc' port='5900' autoport='yes' listen='10.10.10.1'>
     *       <listen type='address' address='10.10.10.1'/>
     *     </graphics>
     * @param xmlDesc the qemu xml description
     * @param target the ip address to migrate to
     * @param vncPassword if set, the VNC password truncated to 8 characters
     * @return the new xmlDesc
     */
    String replaceIpForVNCInDescFileAndNormalizePassword(String xmlDesc, final String target, String vncPassword) {
        final int begin = xmlDesc.indexOf(GRAPHICS_ELEM_START);
        if (begin >= 0) {
            final int end = xmlDesc.lastIndexOf(GRAPHICS_ELEM_END) + GRAPHICS_ELEM_END.length();
            if (end > begin) {
                String graphElem = xmlDesc.substring(begin, end);
                graphElem = graphElem.replaceAll("listen='[a-zA-Z0-9\\.]*'", "listen='" + target + "'");
                graphElem = graphElem.replaceAll("address='[a-zA-Z0-9\\.]*'", "address='" + target + "'");
                if (!vncPassword.equals("")) {
                    graphElem = graphElem.replaceAll("passwd='([^\\s]+)'", "passwd='" + vncPassword + "'");
                }
                xmlDesc = xmlDesc.replaceAll(GRAPHICS_ELEM_START + CONTENTS_WILDCARD + GRAPHICS_ELEM_END, graphElem);
            }
        }
        return xmlDesc;
    }
}
