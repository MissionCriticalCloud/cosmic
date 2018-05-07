package com.cloud.storage.template;

import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.storage.StorageLayer;

import javax.naming.ConfigurationException;
import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawImageProcessor extends AdapterBase implements Processor {
    private static final Logger s_logger = LoggerFactory.getLogger(RawImageProcessor.class);
    StorageLayer _storage;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (this._storage == null) {
            throw new ConfigurationException("Unable to get storage implementation");
        }

        return true;
    }

    @Override
    public FormatInfo process(final String templatePath, final ImageFormat format, final String templateName) throws InternalErrorException {
        if (format != null) {
            s_logger.debug("We currently don't handle conversion from " + format + " to raw image.");
            return null;
        }

        final String imgPath = templatePath + File.separator + templateName + "." + ImageFormat.RAW.getFileExtension();
        if (!this._storage.exists(imgPath)) {
            s_logger.debug("Unable to find raw image:" + imgPath);
            return null;
        }
        final FormatInfo info = new FormatInfo();
        info.format = ImageFormat.RAW;
        info.filename = templateName + "." + ImageFormat.RAW.getFileExtension();
        info.size = this._storage.getSize(imgPath);
        info.virtualSize = info.size;
        s_logger.debug("Process raw image " + info.filename + " successfully");
        return info;
    }

    @Override
    public long getVirtualSize(final File file) {
        return file.length();
    }
}
