package com.cloud.storage.resource;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.CopyCommand;
import com.cloud.legacymodel.communication.command.CreateObjectCommand;
import com.cloud.legacymodel.communication.command.DeleteCommand;
import com.cloud.legacymodel.communication.command.ForgetObjectCommand;
import com.cloud.legacymodel.communication.command.IntroduceObjectCommand;
import com.cloud.legacymodel.communication.command.SnapshotAndCopyCommand;
import com.cloud.storage.command.AttachCommand;
import com.cloud.storage.command.DettachCommand;

public interface StorageProcessor {
    Answer copyTemplateToPrimaryStorage(CopyCommand cmd);

    Answer cloneVolumeFromBaseTemplate(CopyCommand cmd);

    Answer copyVolumeFromImageCacheToPrimary(CopyCommand cmd);

    Answer copyVolumeFromPrimaryToSecondary(CopyCommand cmd);

    Answer createTemplateFromVolume(CopyCommand cmd);

    Answer createTemplateFromSnapshot(CopyCommand cmd);

    Answer backupSnapshot(CopyCommand cmd);

    Answer attachIso(AttachCommand cmd);

    Answer attachVolume(AttachCommand cmd);

    Answer dettachIso(DettachCommand cmd);

    Answer dettachVolume(DettachCommand cmd);

    Answer createVolume(CreateObjectCommand cmd);

    Answer createSnapshot(CreateObjectCommand cmd);

    Answer deleteVolume(DeleteCommand cmd);

    Answer createVolumeFromSnapshot(CopyCommand cmd);

    Answer deleteSnapshot(DeleteCommand cmd);

    Answer introduceObject(IntroduceObjectCommand cmd);

    Answer forgetObject(ForgetObjectCommand cmd);

    Answer snapshotAndCopy(SnapshotAndCopyCommand cmd);
}
