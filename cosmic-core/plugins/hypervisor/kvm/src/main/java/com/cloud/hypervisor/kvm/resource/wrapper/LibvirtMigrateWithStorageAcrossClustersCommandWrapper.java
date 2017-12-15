package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.MigrateWithStorageAcrossClustersAnswer;
import com.cloud.agent.api.MigrateWithStorageAcrossClustersCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.to.VolumeObjectTO;
import com.cloud.utils.Pair;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.StoragePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

@ResourceWrapper(handles = MigrateWithStorageAcrossClustersCommand.class)
public final class LibvirtMigrateWithStorageAcrossClustersCommandWrapper extends CommandWrapper<MigrateWithStorageAcrossClustersCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtMigrateWithStorageAcrossClustersCommandWrapper.class);

    @Override
    public Answer execute(final MigrateWithStorageAcrossClustersCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final VirtualMachineTO vm = command.getVirtualMachine();

        try {
            final LibvirtUtilitiesHelper utilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
            final Connect sourceConnection = utilitiesHelper.getConnectionByVmName(vm.getName());
            final Connect destinationConnection = utilitiesHelper.retrieveQemuConnection("qemu+tcp://" + command.getDestinationIpAddress() + "/system");

            final Domain domain = sourceConnection.domainLookupByName(vm.getName());

            // VIR_DOMAIN_XML_MIGRATABLE = 8
            String domainXml = domain.getXMLDesc(8);

            final XPath xPath = XPathFactory.newInstance().newXPath();
            final XPathExpression expression = xPath.compile("/pool/target/path");

            final List<VolumeObjectTO> volumes = new ArrayList<>();
            for (final Pair<VolumeTO, StorageFilerTO> entry : command.getVolumeMapping()) {
                final VolumeTO volumeTO = entry.first();
                final StorageFilerTO storageFilerTO = entry.second();

                final StoragePool sourcePool = sourceConnection.storagePoolLookupByName(volumeTO.getPoolUuid());
                final String sourcePoolXml = sourcePool.getXMLDesc(0);

                final StoragePool destinationPool = destinationConnection.storagePoolLookupByName(storageFilerTO.getUuid());
                final String destinationPoolXml = destinationPool.getXMLDesc(0);

                final String sourcePath = expression.evaluate(new InputSource(new StringReader(sourcePoolXml)));
                final String sourceLocation = sourcePath + "/" + volumeTO.getPath();

                final String destinationPath = expression.evaluate(new InputSource(new StringReader(destinationPoolXml)));
                final String destinationLocation = destinationPath + "/" + volumeTO.getPath();

                domainXml = domainXml.replace(sourceLocation, destinationLocation);

                final VolumeObjectTO volumeObjectTO = new VolumeObjectTO();
                volumeObjectTO.setId(volumeTO.getId());
                volumeObjectTO.setPath(volumeTO.getPath());
                volumes.add(volumeObjectTO);
            }

            // VIR_MIGRATE_LIVE = 1
            // VIR_MIGRATE_UNDEFINE_SOURCE = 16
            // VIR_MIGRATE_NON_SHARED_DISK = 64
            domain.migrate(destinationConnection, 1 | 16 | 64, domainXml, vm.getName(), null, 0);

            return new MigrateWithStorageAcrossClustersAnswer(command, volumes);
        } catch (final Exception e) {
            s_logger.error("Migration of vm " + vm.getName() + " with storage failed due to " + e.toString(), e);
            return new MigrateWithStorageAcrossClustersAnswer(command, e);
        }
    }
}
