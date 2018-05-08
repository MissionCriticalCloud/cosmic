package com.cloud.common.storageprocessor;

import com.cloud.legacymodel.communication.command.DownloadCommand.ResourceType;
import com.cloud.legacymodel.storage.TemplateFormatInfo;
import com.cloud.legacymodel.storage.TemplateProp;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.storage.StorageLayer;

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

    ArrayList<TemplateFormatInfo> _formats;

    public TemplateLocation(final StorageLayer storage, final String templatePath) {
        this._storage = storage;
        this._templatePath = templatePath;
        if (!this._templatePath.endsWith(File.separator)) {
            this._templatePath += File.separator;
        }
        this._formats = new ArrayList<>(5);
        this._props = new Properties();
        //TO DO - remove this hack
        if (this._templatePath.matches(".*" + "volumes" + ".*")) {
            this._file = this._storage.getFile(this._templatePath + "volume.properties");
            this._resourceType = ResourceType.VOLUME;
        } else {
            this._file = this._storage.getFile(this._templatePath + Filename);
        }
        this._isCorrupted = false;
    }

    public boolean create(final long id, final boolean isPublic, final String uniqueName) throws IOException {
        final boolean result = load();
        this._props.setProperty("id", Long.toString(id));
        this._props.setProperty("public", Boolean.toString(isPublic));
        this._props.setProperty("uniquename", uniqueName);

        return result;
    }

    public boolean load() throws IOException {
        try (final FileInputStream strm = new FileInputStream(this._file)) {
            this._props.load(strm);
        } catch (final IOException e) {
            s_logger.warn("Unable to load the template properties", e);
        }

        for (final ImageFormat format : ImageFormat.values()) {
            final String ext = this._props.getProperty(format.getFileExtension());
            if (ext != null) {
                final TemplateFormatInfo info = new TemplateFormatInfo();
                info.format = format;
                info.filename = this._props.getProperty(format.getFileExtension() + ".filename");
                if (info.filename == null) {
                    continue;
                }
                info.size = NumbersUtil.parseLong(this._props.getProperty(format.getFileExtension() + ".size"), -1);
                this._props.setProperty("physicalSize", Long.toString(info.size));
                info.virtualSize = NumbersUtil.parseLong(this._props.getProperty(format.getFileExtension() + ".virtualsize"), -1);
                this._formats.add(info);

                if (!checkFormatValidity(info)) {
                    this._isCorrupted = true;
                    s_logger.warn("Cleaning up inconsistent information for " + format);
                }
            }
        }

        if (this._props.getProperty("uniquename") == null || this._props.getProperty("virtualsize") == null) {
            return false;
        }

        return (this._formats.size() > 0);
    }

    protected boolean checkFormatValidity(final TemplateFormatInfo info) {
        return (info.format != null && info.size > 0 && info.virtualSize > 0 && info.filename != null);
    }

    public boolean purge() {
        boolean purged = true;
        final String[] files = this._storage.listFiles(this._templatePath);
        for (final String file : files) {
            final boolean r = this._storage.delete(file);
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
        for (final TemplateFormatInfo info : this._formats) {
            this._props.setProperty(info.format.getFileExtension(), "true");
            this._props.setProperty(info.format.getFileExtension() + ".filename", info.filename);
            this._props.setProperty(info.format.getFileExtension() + ".size", Long.toString(info.size));
            this._props.setProperty(info.format.getFileExtension() + ".virtualsize", Long.toString(info.virtualSize));
        }
        try (final FileOutputStream strm = new FileOutputStream(this._file)) {
            this._props.store(strm, "");
        } catch (final IOException e) {
            s_logger.warn("Unable to save the template properties ", e);
            return false;
        }
        return true;
    }

    public TemplateProp getTemplateInfo() {
        final TemplateProp tmplInfo = new TemplateProp();
        tmplInfo.setId(Long.parseLong(this._props.getProperty("id")));
        tmplInfo.setInstallPath(this._templatePath + this._props.getProperty("filename")); // _templatePath endsWith /
        if (this._resourceType == ResourceType.VOLUME) {
            tmplInfo.setInstallPath(tmplInfo.getInstallPath().substring(tmplInfo.getInstallPath().indexOf("volumes")));
        } else {
            tmplInfo.setInstallPath(tmplInfo.getInstallPath().substring(tmplInfo.getInstallPath().indexOf("template")));
        }
        tmplInfo.setCorrupted(this._isCorrupted);
        tmplInfo.setPublic(Boolean.parseBoolean(this._props.getProperty("public")));
        tmplInfo.setTemplateName(this._props.getProperty("uniquename"));
        if (this._props.getProperty("virtualsize") != null) {
            tmplInfo.setSize(Long.parseLong(this._props.getProperty("virtualsize")));
        }
        if (this._props.getProperty("size") != null) {
            tmplInfo.setPhysicalSize(Long.parseLong(this._props.getProperty("size")));
        }

        return tmplInfo;
    }

    public TemplateFormatInfo getFormat(final ImageFormat format) {
        for (final TemplateFormatInfo info : this._formats) {
            if (info.format == format) {
                return info;
            }
        }

        return null;
    }

    public boolean addFormat(final TemplateFormatInfo newInfo) {
        deleteFormat(newInfo.format);

        if (!checkFormatValidity(newInfo)) {
            s_logger.warn("Format is invalid");
            s_logger.debug("Format: " + newInfo.format + " size: " + newInfo.size + " virtualsize: " + newInfo.virtualSize + " filename: " + newInfo.filename);
            s_logger.debug("format, filename cannot be null and size, virtual size should be  > 0 ");
            return false;
        }

        this._props.setProperty("virtualsize", Long.toString(newInfo.virtualSize));
        this._formats.add(newInfo);
        return true;
    }

    protected TemplateFormatInfo deleteFormat(final ImageFormat format) {
        final Iterator<TemplateFormatInfo> it = this._formats.iterator();
        while (it.hasNext()) {
            final TemplateFormatInfo info = it.next();
            if (info.format == format) {
                it.remove();
                this._props.remove(format.getFileExtension());
                this._props.remove(format.getFileExtension() + ".filename");
                this._props.remove(format.getFileExtension() + ".size");
                this._props.remove(format.getFileExtension() + ".virtualsize");
                return info;
            }
        }

        return null;
    }

    public void updateVirtualSize(final long virtualSize) {
        this._props.setProperty("virtualsize", Long.toString(virtualSize));
    }
}
