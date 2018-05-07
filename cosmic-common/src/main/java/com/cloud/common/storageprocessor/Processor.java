package com.cloud.common.storageprocessor;

import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.storage.TemplateFormatInfo;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.utils.component.Adapter;

import java.io.File;
import java.io.IOException;

/**
 * Generic interface to process different types of image formats
 * for templates downloaded and for conversion from one format
 * to anther.
 */
public interface Processor extends Adapter {

    /**
     * Returns image format if it was able to process the original file and
     *
     * @param templatePath path to the templates to process.
     * @param format       Format of the original file.  If null, it means unknown.  If not null,
     *                     there is already a file with thte template name and image format extension
     *                     that exists in case a conversion can be done.
     */
    TemplateFormatInfo process(String templatePath, ImageFormat format, String templateName) throws InternalErrorException;

    long getVirtualSize(File file) throws IOException;
}
