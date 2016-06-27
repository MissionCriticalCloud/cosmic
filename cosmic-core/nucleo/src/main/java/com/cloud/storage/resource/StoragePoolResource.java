//

//

package com.cloud.storage.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.CopyVolumeAnswer;
import com.cloud.agent.api.storage.CopyVolumeCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;

public interface StoragePoolResource {
    // FIXME: Should have a PrimaryStorageDownloadAnswer
    PrimaryStorageDownloadAnswer execute(PrimaryStorageDownloadCommand cmd);

    // FIXME: Should have an DestroyAnswer
    Answer execute(DestroyCommand cmd);

    CopyVolumeAnswer execute(CopyVolumeCommand cmd);
}
