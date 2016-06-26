package org.apache.cloudstack.storage.datastore.driver;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.CreateEntityDownloadURLCommand;
import com.cloud.agent.api.storage.DeleteEntityDownloadURLCommand;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.configuration.Config;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Upload;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.image.BaseImageStoreDriverImpl;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;
import org.apache.cloudstack.storage.image.store.ImageStoreImpl;

import javax.inject.Inject;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudStackImageStoreDriverImpl extends BaseImageStoreDriverImpl {
    private static final Logger s_logger = LoggerFactory.getLogger(CloudStackImageStoreDriverImpl.class);

    @Inject
    ConfigurationDao _configDao;
    @Inject
    HostDao _hostDao;
    @Inject
    EndPointSelector _epSelector;

    @Override
    public DataStoreTO getStoreTO(final DataStore store) {
        final ImageStoreImpl nfsStore = (ImageStoreImpl) store;
        final NfsTO nfsTO = new NfsTO();
        nfsTO.setRole(store.getRole());
        nfsTO.setUrl(nfsStore.getUri());
        return nfsTO;
    }

    @Override
    public String createEntityExtractUrl(final DataStore store, final String installPath, final ImageFormat format, final DataObject dataObject) {
        // find an endpoint to send command
        final EndPoint ep = _epSelector.select(store);
        // Create Symlink at ssvm
        final String path = installPath;
        final String uuid = UUID.randomUUID().toString() + "." + format.getFileExtension();
        final CreateEntityDownloadURLCommand cmd = new CreateEntityDownloadURLCommand(((ImageStoreEntity) store).getMountPoint(), path, uuid, dataObject.getTO());
        Answer ans = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            ans = new Answer(cmd, false, errMsg);
        } else {
            ans = ep.sendMessage(cmd);
        }
        if (ans == null || !ans.getResult()) {
            final String errorString = "Unable to create a link for entity at " + installPath + " on ssvm," + ans.getDetails();
            s_logger.error(errorString);
            throw new CloudRuntimeException(errorString);
        }
        // Construct actual URL locally now that the symlink exists at SSVM
        return generateCopyUrl(ep.getPublicAddr(), uuid);
    }

    private String generateCopyUrl(final String ipAddress, final String uuid) {

        String hostname = ipAddress;
        String scheme = "http";
        boolean _sslCopy = false;
        final String sslCfg = _configDao.getValue(Config.SecStorageEncryptCopy.toString());
        final String _ssvmUrlDomain = _configDao.getValue("secstorage.ssl.cert.domain");
        if (sslCfg != null) {
            _sslCopy = Boolean.parseBoolean(sslCfg);
        }
        if (_sslCopy && (_ssvmUrlDomain == null || _ssvmUrlDomain.isEmpty())) {
            s_logger.warn("Empty secondary storage url domain, ignoring SSL");
            _sslCopy = false;
        }
        if (_sslCopy) {
            if (_ssvmUrlDomain.startsWith("*")) {
                hostname = ipAddress.replace(".", "-");
                hostname = hostname + _ssvmUrlDomain.substring(1);
            } else {
                hostname = _ssvmUrlDomain;
            }
            scheme = "https";
        }
        return scheme + "://" + hostname + "/userdata/" + uuid;
    }

    @Override
    public void deleteEntityExtractUrl(final DataStore store, final String installPath, final String downloadUrl, final Upload.Type entityType) {
        // find an endpoint to send command based on the ssvm on which the url was created.
        final EndPoint ep = _epSelector.select(store, downloadUrl);

        // Delete Symlink at ssvm. In case of volume also delete the volume.
        final DeleteEntityDownloadURLCommand cmd = new DeleteEntityDownloadURLCommand(installPath, entityType, downloadUrl, ((ImageStoreEntity) store).getMountPoint());

        Answer ans = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            ans = new Answer(cmd, false, errMsg);
        } else {
            ans = ep.sendMessage(cmd);
        }
        if (ans == null || !ans.getResult()) {
            final String errorString = "Unable to delete the url " + downloadUrl + " for path " + installPath + " on ssvm, " + ans.getDetails();
            s_logger.error(errorString);
            throw new CloudRuntimeException(errorString);
        }
    }
}
