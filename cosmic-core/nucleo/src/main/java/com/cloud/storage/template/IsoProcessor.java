package com.cloud.storage.template;

import com.cloud.model.enumeration.ImageFormat;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.storage.StorageLayer;

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

        if (!this._storage.exists(isoPath)) {
            s_logger.debug("Unable to find the iso file: " + isoPath);
            return null;
        }

        final FormatInfo info = new FormatInfo();
        info.format = ImageFormat.ISO;
        info.filename = templateName + "." + ImageFormat.ISO.getFileExtension();
        info.size = this._storage.getSize(isoPath);
        info.virtualSize = info.size;

        return info;
    }

    @Override
    public long getVirtualSize(final File file) {
        return file.length();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (this._storage == null) {
            throw new ConfigurationException("Unable to get storage implementation");
        }
        return true;
    }
}
