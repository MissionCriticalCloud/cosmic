//

//

package com.cloud.storage.template;

import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.template.Processor.FormatInfo;
import com.cloud.utils.NumbersUtil;
import org.apache.cloudstack.storage.command.DownloadCommand.ResourceType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateLocation {
    public final static String Filename = "template.properties";
    private static final Logger s_logger = LoggerFactory.getLogger(TemplateLocation.class);
    StorageLayer _storage;
    String _templatePath;
    boolean _isCorrupted;
    ResourceType _resourceType = ResourceType.TEMPLATE;

    File _file;
    Properties _props;

    ArrayList<FormatInfo> _formats;

    public TemplateLocation(final StorageLayer storage, final String templatePath) {
        _storage = storage;
        _templatePath = templatePath;
        if (!_templatePath.endsWith(File.separator)) {
            _templatePath += File.separator;
        }
        _formats = new ArrayList<>(5);
        _props = new Properties();
        //TO DO - remove this hack
        if (_templatePath.matches(".*" + "volumes" + ".*")) {
            _file = _storage.getFile(_templatePath + "volume.properties");
            _resourceType = ResourceType.VOLUME;
        } else {
            _file = _storage.getFile(_templatePath + Filename);
        }
        _isCorrupted = false;
    }

    public boolean create(final long id, final boolean isPublic, final String uniqueName) throws IOException {
        final boolean result = load();
        _props.setProperty("id", Long.toString(id));
        _props.setProperty("public", Boolean.toString(isPublic));
        _props.setProperty("uniquename", uniqueName);

        return result;
    }

    public boolean load() throws IOException {
        try (FileInputStream strm = new FileInputStream(_file)) {
            _props.load(strm);
        } catch (final IOException e) {
            s_logger.warn("Unable to load the template properties", e);
        }

        for (final ImageFormat format : ImageFormat.values()) {
            final String ext = _props.getProperty(format.getFileExtension());
            if (ext != null) {
                final FormatInfo info = new FormatInfo();
                info.format = format;
                info.filename = _props.getProperty(format.getFileExtension() + ".filename");
                if (info.filename == null) {
                    continue;
                }
                info.size = NumbersUtil.parseLong(_props.getProperty(format.getFileExtension() + ".size"), -1);
                _props.setProperty("physicalSize", Long.toString(info.size));
                info.virtualSize = NumbersUtil.parseLong(_props.getProperty(format.getFileExtension() + ".virtualsize"), -1);
                _formats.add(info);

                if (!checkFormatValidity(info)) {
                    _isCorrupted = true;
                    s_logger.warn("Cleaning up inconsistent information for " + format);
                }
            }
        }

        if (_props.getProperty("uniquename") == null || _props.getProperty("virtualsize") == null) {
            return false;
        }

        return (_formats.size() > 0);
    }

    protected boolean checkFormatValidity(final FormatInfo info) {
        return (info.format != null && info.size > 0 && info.virtualSize > 0 && info.filename != null);
    }

    public boolean purge() {
        boolean purged = true;
        final String[] files = _storage.listFiles(_templatePath);
        for (final String file : files) {
            final boolean r = _storage.delete(file);
            if (!r) {
                purged = false;
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug((r ? "R" : "Unable to r") + "emove " + file);
            }
        }

        return purged;
    }

    public boolean save() {
        for (final FormatInfo info : _formats) {
            _props.setProperty(info.format.getFileExtension(), "true");
            _props.setProperty(info.format.getFileExtension() + ".filename", info.filename);
            _props.setProperty(info.format.getFileExtension() + ".size", Long.toString(info.size));
            _props.setProperty(info.format.getFileExtension() + ".virtualsize", Long.toString(info.virtualSize));
        }
        try (FileOutputStream strm = new FileOutputStream(_file)) {
            _props.store(strm, "");
        } catch (final IOException e) {
            s_logger.warn("Unable to save the template properties ", e);
            return false;
        }
        return true;
    }

    public TemplateProp getTemplateInfo() {
        final TemplateProp tmplInfo = new TemplateProp();
        tmplInfo.id = Long.parseLong(_props.getProperty("id"));
        tmplInfo.installPath = _templatePath + _props.getProperty("filename"); // _templatePath endsWith /
        if (_resourceType == ResourceType.VOLUME) {
            tmplInfo.installPath = tmplInfo.installPath.substring(tmplInfo.installPath.indexOf("volumes"));
        } else {
            tmplInfo.installPath = tmplInfo.installPath.substring(tmplInfo.installPath.indexOf("template"));
        }
        tmplInfo.isCorrupted = _isCorrupted;
        tmplInfo.isPublic = Boolean.parseBoolean(_props.getProperty("public"));
        tmplInfo.templateName = _props.getProperty("uniquename");
        if (_props.getProperty("virtualsize") != null) {
            tmplInfo.size = Long.parseLong(_props.getProperty("virtualsize"));
        }
        if (_props.getProperty("size") != null) {
            tmplInfo.physicalSize = Long.parseLong(_props.getProperty("size"));
        }

        return tmplInfo;
    }

    public FormatInfo getFormat(final ImageFormat format) {
        for (final FormatInfo info : _formats) {
            if (info.format == format) {
                return info;
            }
        }

        return null;
    }

    public boolean addFormat(final FormatInfo newInfo) {
        deleteFormat(newInfo.format);

        if (!checkFormatValidity(newInfo)) {
            s_logger.warn("Format is invalid");
            s_logger.debug("Format: " + newInfo.format + " size: " + newInfo.size + " virtualsize: " + newInfo.virtualSize + " filename: " + newInfo.filename);
            s_logger.debug("format, filename cannot be null and size, virtual size should be  > 0 ");
            return false;
        }

        _props.setProperty("virtualsize", Long.toString(newInfo.virtualSize));
        _formats.add(newInfo);
        return true;
    }

    protected FormatInfo deleteFormat(final ImageFormat format) {
        final Iterator<FormatInfo> it = _formats.iterator();
        while (it.hasNext()) {
            final FormatInfo info = it.next();
            if (info.format == format) {
                it.remove();
                _props.remove(format.getFileExtension());
                _props.remove(format.getFileExtension() + ".filename");
                _props.remove(format.getFileExtension() + ".size");
                _props.remove(format.getFileExtension() + ".virtualsize");
                return info;
            }
        }

        return null;
    }

    public void updateVirtualSize(final long virtualSize) {
        _props.setProperty("virtualsize", Long.toString(virtualSize));
    }
}
