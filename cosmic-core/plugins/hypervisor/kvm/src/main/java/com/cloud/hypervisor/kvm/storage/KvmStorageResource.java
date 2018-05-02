package com.cloud.hypervisor.kvm.storage;

import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.AttachPrimaryDataStoreCommand;
import com.cloud.legacymodel.communication.command.CopyCommand;
import com.cloud.legacymodel.communication.command.CreateObjectCommand;
import com.cloud.legacymodel.communication.command.CreatePrimaryDataStoreCommand;
import com.cloud.legacymodel.communication.command.DeleteCommand;
import com.cloud.legacymodel.communication.command.StorageSubSystemCommand;
import com.cloud.storage.command.AttachCommand;
import com.cloud.storage.command.DettachCommand;

public class KvmStorageResource {

    private final LibvirtComputingResource resource;

    public KvmStorageResource(final LibvirtComputingResource resource) {
        this.resource = resource;
    }

    public Answer handleStorageCommands(final StorageSubSystemCommand command) {
        if (command instanceof CopyCommand) {
            return this.execute((CopyCommand) command);
        } else if (command instanceof AttachPrimaryDataStoreCommand) {
            return this.execute((AttachPrimaryDataStoreCommand) command);
        } else if (command instanceof CreatePrimaryDataStoreCommand) {
            return execute((CreatePrimaryDataStoreCommand) command);
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

    protected Answer execute(final CopyCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final AttachPrimaryDataStoreCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final CreatePrimaryDataStoreCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final CreateObjectCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final DeleteCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final AttachCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final DettachCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }
}
