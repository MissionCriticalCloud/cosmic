package com.cloud.storage.resource;

import com.cloud.agent.api.Answer;
import com.cloud.storage.command.AttachCommand;
import com.cloud.storage.command.CopyCommand;
import com.cloud.storage.command.CreateObjectCommand;
import com.cloud.storage.command.DeleteCommand;
import com.cloud.storage.command.DettachCommand;
import com.cloud.storage.command.ForgetObjectCmd;
import com.cloud.storage.command.IntroduceObjectCmd;
import com.cloud.storage.command.SnapshotAndCopyCommand;

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
