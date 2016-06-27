//

//

package com.cloud.storage.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Volume;
import org.apache.cloudstack.storage.command.AttachCommand;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.CreateObjectAnswer;
import org.apache.cloudstack.storage.command.CreateObjectCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.DettachCommand;
import org.apache.cloudstack.storage.command.IntroduceObjectCmd;
import org.apache.cloudstack.storage.command.SnapshotAndCopyCommand;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageSubsystemCommandHandlerBase implements StorageSubsystemCommandHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(StorageSubsystemCommandHandlerBase.class);
    protected StorageProcessor processor;

    public StorageSubsystemCommandHandlerBase(final StorageProcessor processor) {
        this.processor = processor;
    }

    @Override
    public Answer handleStorageCommands(final StorageSubSystemCommand command) {
        if (command instanceof CopyCommand) {
            return this.execute((CopyCommand) command);
        } else if (command instanceof CreateObjectCommand) {
            return execute((CreateObjectCommand) command);
        } else if (command instanceof DeleteCommand) {
            return execute((DeleteCommand) command);
        } else if (command instanceof AttachCommand) {
            return execute((AttachCommand) command);
        } else if (command instanceof DettachCommand) {
            return execute((DettachCommand) command);
        } else if (command instanceof IntroduceObjectCmd) {
            return processor.introduceObject((IntroduceObjectCmd) command);
        } else if (command instanceof SnapshotAndCopyCommand) {
            return processor.snapshotAndCopy((SnapshotAndCopyCommand) command);
        }

        return new Answer((Command) command, false, "not implemented yet");
    }

    protected Answer execute(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final DataStoreTO srcDataStore = srcData.getDataStore();
        final DataStoreTO destDataStore = destData.getDataStore();

        if (srcData.getObjectType() == DataObjectType.TEMPLATE &&
                (srcData.getDataStore().getRole() == DataStoreRole.Image || srcData.getDataStore().getRole() == DataStoreRole.ImageCache) &&
                destData.getDataStore().getRole() == DataStoreRole.Primary) {
            //copy template to primary storage
            return processor.copyTemplateToPrimaryStorage(cmd);
        } else if (srcData.getObjectType() == DataObjectType.TEMPLATE && srcDataStore.getRole() == DataStoreRole.Primary &&
                destDataStore.getRole() == DataStoreRole.Primary) {
            //clone template to a volume
            return processor.cloneVolumeFromBaseTemplate(cmd);
        } else if (srcData.getObjectType() == DataObjectType.VOLUME &&
                (srcData.getDataStore().getRole() == DataStoreRole.ImageCache || srcDataStore.getRole() == DataStoreRole.Image)) {
            //copy volume from image cache to primary
            return processor.copyVolumeFromImageCacheToPrimary(cmd);
        } else if (srcData.getObjectType() == DataObjectType.VOLUME && srcData.getDataStore().getRole() == DataStoreRole.Primary) {
            if (destData.getObjectType() == DataObjectType.VOLUME) {
                return processor.copyVolumeFromPrimaryToSecondary(cmd);
            } else if (destData.getObjectType() == DataObjectType.TEMPLATE) {
                return processor.createTemplateFromVolume(cmd);
            }
        } else if (srcData.getObjectType() == DataObjectType.SNAPSHOT && destData.getObjectType() == DataObjectType.SNAPSHOT &&
                srcData.getDataStore().getRole() == DataStoreRole.Primary) {
            return processor.backupSnapshot(cmd);
        } else if (srcData.getObjectType() == DataObjectType.SNAPSHOT && destData.getObjectType() == DataObjectType.VOLUME) {
            return processor.createVolumeFromSnapshot(cmd);
        } else if (srcData.getObjectType() == DataObjectType.SNAPSHOT && destData.getObjectType() == DataObjectType.TEMPLATE) {
            return processor.createTemplateFromSnapshot(cmd);
        }

        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final CreateObjectCommand cmd) {
        final DataTO data = cmd.getData();
        try {
            if (data.getObjectType() == DataObjectType.VOLUME) {
                return processor.createVolume(cmd);
            } else if (data.getObjectType() == DataObjectType.SNAPSHOT) {
                return processor.createSnapshot(cmd);
            }
            return new CreateObjectAnswer("not supported type");
        } catch (final Exception e) {
            s_logger.debug("Failed to create object: " + data.getObjectType() + ": " + e.toString());
            return new CreateObjectAnswer(e.toString());
        }
    }

    protected Answer execute(final DeleteCommand cmd) {
        final DataTO data = cmd.getData();
        Answer answer = null;
        if (data.getObjectType() == DataObjectType.VOLUME) {
            answer = processor.deleteVolume(cmd);
        } else if (data.getObjectType() == DataObjectType.SNAPSHOT) {
            answer = processor.deleteSnapshot(cmd);
        } else {
            answer = new Answer(cmd, false, "unsupported type");
        }

        return answer;
    }

    protected Answer execute(final AttachCommand cmd) {
        final DiskTO disk = cmd.getDisk();
        if (disk.getType() == Volume.Type.ISO) {
            return processor.attachIso(cmd);
        } else {
            return processor.attachVolume(cmd);
        }
    }

    protected Answer execute(final DettachCommand cmd) {
        final DiskTO disk = cmd.getDisk();
        if (disk.getType() == Volume.Type.ISO) {
            return processor.dettachIso(cmd);
        } else {
            return processor.dettachVolume(cmd);
        }
    }
}
