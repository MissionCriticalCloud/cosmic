package com.cloud.hypervisor.kvm.storage;

import com.cloud.agent.api.Answer;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;

import org.apache.cloudstack.storage.command.AttachCommand;
import org.apache.cloudstack.storage.command.AttachPrimaryDataStoreCmd;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.CreateObjectCommand;
import org.apache.cloudstack.storage.command.CreatePrimaryDataStoreCmd;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.DettachCommand;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

public class KvmStorageResource {

  private final LibvirtComputingResource resource;

  public KvmStorageResource(LibvirtComputingResource resource) {
    this.resource = resource;
  }

  public Answer handleStorageCommands(StorageSubSystemCommand command) {
    if (command instanceof CopyCommand) {
      return this.execute((CopyCommand) command);
    } else if (command instanceof AttachPrimaryDataStoreCmd) {
      return this.execute((AttachPrimaryDataStoreCmd) command);
    } else if (command instanceof CreatePrimaryDataStoreCmd) {
      return execute((CreatePrimaryDataStoreCmd) command);
    } else if (command instanceof CreateObjectCommand) {
      return execute((CreateObjectCommand) command);
    } else if (command instanceof DeleteCommand) {
      return execute((DeleteCommand) command);
    } else if (command instanceof AttachCommand) {
      return execute((AttachCommand) command);
    } else if (command instanceof DettachCommand) {
      return execute((DettachCommand) command);
    }
    return new Answer(command, false, "not implemented yet");
  }

  protected Answer execute(CopyCommand cmd) {
    return new Answer(cmd, false, "not implemented yet");
  }

  protected Answer execute(AttachPrimaryDataStoreCmd cmd) {
    return new Answer(cmd, false, "not implemented yet");
  }

  protected Answer execute(CreatePrimaryDataStoreCmd cmd) {
    return new Answer(cmd, false, "not implemented yet");
  }

  protected Answer execute(CreateObjectCommand cmd) {
    return new Answer(cmd, false, "not implemented yet");
  }

  protected Answer execute(DeleteCommand cmd) {
    return new Answer(cmd, false, "not implemented yet");
  }

  protected Answer execute(AttachCommand cmd) {
    return new Answer(cmd, false, "not implemented yet");
  }

  protected Answer execute(DettachCommand cmd) {
    return new Answer(cmd, false, "not implemented yet");
  }
}