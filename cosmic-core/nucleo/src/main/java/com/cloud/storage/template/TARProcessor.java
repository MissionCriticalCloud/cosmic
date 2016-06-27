//

//

package com.cloud.storage.template;

import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StorageLayer;
import com.cloud.utils.component.AdapterBase;

import javax.naming.ConfigurationException;
import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TARProcessor extends AdapterBase implements Processor {
    private static final Logger s_logger = LoggerFactory.getLogger(TARProcessor.class);

    private StorageLayer _storage;

    @Override
    public FormatInfo process(final String templatePath, final ImageFormat format, final String templateName) {
        if (format != null) {
            s_logger.debug("We currently don't handle conversion from " + format + " to TAR.");
            return null;
        }

        final String tarPath = templatePath + File.separator + templateName + "." + ImageFormat.TAR.getFileExtension();

        if (!_storage.exists(tarPath)) {
            s_logger.debug("Unable to find the tar file: " + tarPath);
            return null;
        }

        final FormatInfo info = new FormatInfo();
        info.format = ImageFormat.TAR;
        info.filename = templateName + "." + ImageFormat.TAR.getFileExtension();

        final File tarFile = _storage.getFile(tarPath);

        info.size = _storage.getSize(tarPath);

        info.virtualSize = getVirtualSize(tarFile);

        return info;
    }

    @Override
    public long getVirtualSize(final File file) {
        return file.length();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (_storage == null) {
            throw new ConfigurationException("Unable to get storage implementation");
        }

        return true;
    }
}
