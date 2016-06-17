//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.HashMap;
import java.util.Map;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.template.TemplateProp;

@ResourceWrapper(handles = ModifyStoragePoolCommand.class)
public final class LibvirtModifyStoragePoolCommandWrapper
    extends CommandWrapper<ModifyStoragePoolCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final ModifyStoragePoolCommand command,
      final LibvirtComputingResource libvirtComputingResource) {
    final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
    final KvmStoragePool storagepool = storagePoolMgr.createStoragePool(command.getPool().getUuid(),
        command.getPool().getHost(), command.getPool().getPort(), command.getPool().getPath(),
        command.getPool().getUserInfo(), command.getPool().getType());
    if (storagepool == null) {
      return new Answer(command, false, " Failed to create storage pool");
    }

    final Map<String, TemplateProp> tInfo = new HashMap<String, TemplateProp>();
    final ModifyStoragePoolAnswer answer = new ModifyStoragePoolAnswer(command, storagepool.getCapacity(),
        storagepool.getAvailable(), tInfo);

    return answer;
  }
}