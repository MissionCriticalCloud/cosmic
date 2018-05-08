package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.LibvirtConnection;
import com.cloud.common.storageprocessor.Processor;
import com.cloud.common.storageprocessor.QCOW2Processor;
import com.cloud.common.storageprocessor.TemplateLocation;
import com.cloud.legacymodel.to.VMSnapshotTO;
import com.cloud.utils.script.Script;
import com.cloud.utils.storage.StorageLayer;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

/**
 * This class is used to wrap the calls to several static methods. By doing so, we make easier to mock this class and
 * the methods wrapped here.
 */
public class LibvirtUtilitiesHelper {

    public static final int TIMEOUT = 10000;

    public Connect getConnectionByVmName(final String vmName) throws LibvirtException {
        return LibvirtConnection.getConnectionByVmName(vmName);
    }

    public Connect getConnection() throws LibvirtException {
        return LibvirtConnection.getConnection();
    }

    public TemplateLocation buildTemplateLocation(final StorageLayer storage, final String templatePath) {
        return new TemplateLocation(storage, templatePath);
    }

    public Processor buildQcow2Processor(final StorageLayer storage) throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put(StorageLayer.InstanceConfigKey, storage);

        final Processor qcow2Processor = new QCOW2Processor();
        qcow2Processor.configure("QCOW2 Processor", params);

        return qcow2Processor;
    }

    public String generateUuidName() {
        return UUID.randomUUID().toString();
    }

    public Connect getConnectionByType(final String hvsType) throws LibvirtException {
        return LibvirtConnection.getConnectionByType(hvsType);
    }

    public String retrieveSshKeysPath() {
        return LibvirtComputingResource.SSHKEYSPATH;
    }

    public String retrieveSshPubKeyPath() {
        return LibvirtComputingResource.SSHPUBKEYPATH;
    }

    public String retrieveSshPrvKeyPath() {
        return LibvirtComputingResource.SSHPRVKEYPATH;
    }

    public String retrieveBashScriptPath() {
        return LibvirtComputingResource.BASH_SCRIPT_PATH;
    }

    public Connect retrieveQemuConnection(final String qemuUri) throws LibvirtException {
        return new Connect(qemuUri);
    }

    public Script buildScript(final String scriptPath) {
        return new Script(scriptPath, TIMEOUT);
    }

    public String generateVMSnapshotXML(final VMSnapshotTO snapshot, final VMSnapshotTO parent, final String domainXmlDesc) {
        final String parentName = (parent == null) ? "" : ("  <parent><name>" + parent.getSnapshotName() + "</name></parent>\n");
        return "<domainsnapshot>\n"
                + "  <name>" + snapshot.getSnapshotName() + "</name>\n"
                + "  <state>running</state>\n"
                + parentName
                + "  <creationTime>" + (int) Math.rint(snapshot.getCreateTime() / 1000) + "</creationTime>\n"
                + domainXmlDesc
                + "</domainsnapshot>";
    }
}
