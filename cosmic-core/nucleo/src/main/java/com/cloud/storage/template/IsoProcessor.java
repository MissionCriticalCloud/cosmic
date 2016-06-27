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

public class IsoProcessor extends AdapterBase implements Processor {
    private static final Logger s_logger = LoggerFactory.getLogger(IsoProcessor.class);

    StorageLayer _storage;

    @Override
    public FormatInfo process(final String templatePath, final ImageFormat format, final String templateName) {
        if (format != null) {
            s_logger.debug("We don't handle conversion from " + format + " to ISO.");
            return null;
        }

        final String isoPath = templatePath + File.separator + templateName + "." + ImageFormat.ISO.getFileExtension();

        if (!_storage.exists(isoPath)) {
            s_logger.debug("Unable to find the iso file: " + isoPath);
            return null;
        }

        final FormatInfo info = new FormatInfo();
        info.format = ImageFormat.ISO;
        info.filename = templateName + "." + ImageFormat.ISO.getFileExtension();
        info.size = _storage.getSize(isoPath);
        info.virtualSize = info.size;

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
