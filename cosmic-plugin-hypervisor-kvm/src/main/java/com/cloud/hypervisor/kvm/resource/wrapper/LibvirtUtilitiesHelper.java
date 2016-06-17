package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.ConfigurationException;

import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtConnection;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.template.Processor;
import com.cloud.storage.template.QCOW2Processor;
import com.cloud.storage.template.TemplateLocation;
import com.cloud.utils.script.Script;

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
    final TemplateLocation location = new TemplateLocation(storage, templatePath);
    return location;
  }

  public Processor buildQcow2Processor(final StorageLayer storage) throws ConfigurationException {
    final Map<String, Object> params = new HashMap<String, Object>();
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
    final Script script = new Script(scriptPath, TIMEOUT);
    return script;
  }
}