package org.apache.cloudstack.storage.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.storage.JavaStorageLayer;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;
import org.apache.cloudstack.storage.template.DownloadManagerImpl;

import java.net.URI;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalNfsSecondaryStorageResource extends NfsSecondaryStorageResource {

    private static final Logger s_logger = LoggerFactory.getLogger(LocalNfsSecondaryStorageResource.class);

    public LocalNfsSecondaryStorageResource() {
        this._dlMgr = new DownloadManagerImpl();
        ((DownloadManagerImpl) _dlMgr).setThreadPool(Executors.newFixedThreadPool(10));
        _storage = new JavaStorageLayer();
        this._inSystemVM = false;
    }

    @Override
    public void setParentPath(final String path) {
        this._parent = path;
    }

    @Override
    synchronized public String getRootDir(final String secUrl) {
        try {
            final URI uri = new URI(secUrl);
            final String dir = mountUri(uri);
            return _parent + "/" + dir;
        } catch (final Exception e) {
            final String msg = "GetRootDir for " + secUrl + " failed due to " + e.toString();
            s_logger.error(msg, e);
            throw new CloudRuntimeException(msg);
        }
    }

    @Override
    public Answer executeRequest(final Command cmd) {
        return super.executeRequest(cmd);
    }

    @Override
    protected void mount(final String localRootPath, final String remoteDevice, final URI uri) {
        ensureLocalRootPathExists(localRootPath, uri);

        if (mountExists(localRootPath, uri)) {
            return;
        }

        attemptMount(localRootPath, remoteDevice, uri);

        // Change permissions for the mountpoint - seems to bypass authentication
        final Script script = new Script(true, "chmod", _timeout, s_logger);
        script.add("777", localRootPath);
        final String result = script.execute();
        if (result != null) {
            final String errMsg = "Unable to set permissions for " + localRootPath + " due to " + result;
            s_logger.error(errMsg);
            throw new CloudRuntimeException(errMsg);
        }
        s_logger.debug("Successfully set 777 permission for " + localRootPath);

        // XXX: Adding the check for creation of snapshots dir here. Might have
        // to move it somewhere more logical later.
        checkForSnapshotsDir(localRootPath);
        checkForVolumesDir(localRootPath);
    }
}
