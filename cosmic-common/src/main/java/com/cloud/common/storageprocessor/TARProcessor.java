package com.cloud.common.storageprocessor;

import com.cloud.legacymodel.storage.TemplateFormatInfo;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.storage.StorageLayer;

import javax.naming.ConfigurationException;
import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TARProcessor extends AdapterBase implements Processor {
    private static final Logger s_logger = LoggerFactory.getLogger(TARProcessor.class);

    private StorageLayer _storage;

    @Override
    public TemplateFormatInfo process(final String templatePath, final ImageFormat format, final String templateName) {
        if (format != null) {
            s_logger.debug("We currently don't handle conversion from " + format + " to TAR.");
            return null;
        }

        final String tarPath = templatePath + File.separator + templateName + "." + ImageFormat.TAR.toString().toLowerCase();

        if (!this._storage.exists(tarPath)) {
            s_logger.debug("Unable to find the tar file: " + tarPath);
            return null;
        }

        final TemplateFormatInfo info = new TemplateFormatInfo();
        info.format = ImageFormat.TAR;
        info.filename = templateName + "." + ImageFormat.TAR.toString().toLowerCase();

        final File tarFile = this._storage.getFile(tarPath);

        info.size = this._storage.getSize(tarPath);

        info.virtualSize = getVirtualSize(tarFile);

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
