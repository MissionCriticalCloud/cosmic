//

//

package com.cloud.storage.template;

import com.cloud.exception.InternalErrorException;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StorageLayer;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.AdapterBase;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VhdProcessor processes the downloaded template for VHD.  It
 * currently does not handle any type of template conversion
 * into the VHD format.
 */
public class VhdProcessor extends AdapterBase implements Processor {

    private static final Logger s_logger = LoggerFactory.getLogger(VhdProcessor.class);
    private final int vhdFooterSize = 512;
    private final int vhdFooterCreatorAppOffset = 28;
    private final int vhdFooterCreatorVerOffset = 32;
    private final int vhdFooterCurrentSizeOffset = 48;
    private final byte[][] citrixCreatorApp = {{0x74, 0x61, 0x70, 0x00}, {0x43, 0x54, 0x58, 0x53}}; /*"tap ", and "CTXS"*/
    StorageLayer _storage;

    @Override
    public FormatInfo process(final String templatePath, final ImageFormat format, final String templateName) throws InternalErrorException {
        if (format != null) {
            s_logger.debug("We currently don't handle conversion from " + format + " to VHD.");
            return null;
        }

        final String vhdPath = templatePath + File.separator + templateName + "." + ImageFormat.VHD.getFileExtension();
        if (!_storage.exists(vhdPath)) {
            s_logger.debug("Unable to find the vhd file: " + vhdPath);
            return null;
        }

        final File vhdFile = _storage.getFile(vhdPath);

        final FormatInfo info = new FormatInfo();
        info.format = ImageFormat.VHD;
        info.filename = templateName + "." + ImageFormat.VHD.getFileExtension();
        info.size = _storage.getSize(vhdPath);

        try {
            info.virtualSize = getTemplateVirtualSize(vhdFile);
        } catch (final IOException e) {
            s_logger.error("Unable to get the virtual size for " + vhdPath);
            throw new InternalErrorException("unable to get virtual size from vhd file");
        }

        return info;
    }

    @Override
    public long getVirtualSize(final File file) throws IOException {
        try {
            final long size = getTemplateVirtualSize(file);
            return size;
        } catch (final Exception e) {
            s_logger.info("[ignored]" + "failed to get template virtual size for VHD: " + e.getLocalizedMessage());
        }
        return file.length();
    }

    protected long getTemplateVirtualSize(final File file) throws IOException {
        final byte[] currentSize = new byte[8];
        final byte[] creatorApp = new byte[4];

        try (FileInputStream strm = new FileInputStream(file)) {
            long skipped = strm.skip(file.length() - vhdFooterSize + vhdFooterCreatorAppOffset);
            if (skipped == -1) {
                throw new IOException("Unexpected end-of-file");
            }
            long read = strm.read(creatorApp);
            if (read == -1) {
                throw new IOException("Unexpected end-of-file");
            }
            skipped = strm.skip(vhdFooterCurrentSizeOffset - vhdFooterCreatorVerOffset);
            if (skipped == -1) {
                throw new IOException("Unexpected end-of-file");
            }
            read = strm.read(currentSize);
            if (read == -1) {
                throw new IOException("Unexpected end-of-file");
            }
        }

        return NumbersUtil.bytesToLong(currentSize);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        _storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (_storage == null) {
            throw new ConfigurationException("Unable to get storage implementation");
        }

        return true;
    }
}
