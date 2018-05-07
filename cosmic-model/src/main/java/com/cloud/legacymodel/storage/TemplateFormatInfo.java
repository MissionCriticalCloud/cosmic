package com.cloud.legacymodel.storage;

import com.cloud.model.enumeration.ImageFormat;

public class TemplateFormatInfo {
    public ImageFormat format;
    public long size;
    public long virtualSize;
    public String filename;
    public boolean isCorrupted;
}
