package org.apache.cloudstack.storage.image.datastore;

import com.cloud.storage.ImageStore;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Upload;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;

import java.util.Set;

public interface ImageStoreEntity extends DataStore, ImageStore {
    TemplateInfo getTemplate(long templateId);

    VolumeInfo getVolume(long volumeId);

    SnapshotInfo getSnapshot(long snapshotId);

    boolean exists(DataObject object);

    Set<TemplateInfo> listTemplates();

    String getMountPoint(); // get the mount point on ssvm.

    String createEntityExtractUrl(String installPath, ImageFormat format, DataObject dataObject);  // get the entity download URL

    void deleteExtractUrl(String installPath, String url, Upload.Type volume);
}
