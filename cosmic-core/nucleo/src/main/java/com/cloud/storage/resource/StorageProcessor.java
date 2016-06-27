//

//

package com.cloud.storage.resource;

import com.cloud.agent.api.Answer;
import org.apache.cloudstack.storage.command.AttachCommand;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.CreateObjectCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.DettachCommand;
import org.apache.cloudstack.storage.command.ForgetObjectCmd;
import org.apache.cloudstack.storage.command.IntroduceObjectCmd;
import org.apache.cloudstack.storage.command.SnapshotAndCopyCommand;

public interface StorageProcessor {
    public Answer copyTemplateToPrimaryStorage(CopyCommand cmd);

    public Answer cloneVolumeFromBaseTemplate(CopyCommand cmd);

    public Answer copyVolumeFromImageCacheToPrimary(CopyCommand cmd);

    public Answer copyVolumeFromPrimaryToSecondary(CopyCommand cmd);

    public Answer createTemplateFromVolume(CopyCommand cmd);

    public Answer createTemplateFromSnapshot(CopyCommand cmd);

    public Answer backupSnapshot(CopyCommand cmd);

    public Answer attachIso(AttachCommand cmd);

    public Answer attachVolume(AttachCommand cmd);

    public Answer dettachIso(DettachCommand cmd);

    public Answer dettachVolume(DettachCommand cmd);

    public Answer createVolume(CreateObjectCommand cmd);

    public Answer createSnapshot(CreateObjectCommand cmd);

    public Answer deleteVolume(DeleteCommand cmd);

    public Answer createVolumeFromSnapshot(CopyCommand cmd);

    public Answer deleteSnapshot(DeleteCommand cmd);

    public Answer introduceObject(IntroduceObjectCmd cmd);

    public Answer forgetObject(ForgetObjectCmd cmd);

    public Answer snapshotAndCopy(SnapshotAndCopyCommand cmd);
}
