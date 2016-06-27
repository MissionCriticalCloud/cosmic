package org.apache.cloudstack.storage.image.format;

import javax.inject.Inject;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ImageFormatHelper {
    private static final ImageFormat defaultFormat = new Unknown();
    private static List<ImageFormat> formats;

    public static ImageFormat getFormat(final String format) {
        for (final ImageFormat fm : formats) {
            if (fm.toString().equals(format)) {
                return fm;
            }
        }
        return ImageFormatHelper.defaultFormat;
    }

    @Inject
    public void setFormats(final List<ImageFormat> formats) {
        ImageFormatHelper.initFormats(formats);
    }

    private static synchronized void initFormats(final List<ImageFormat> newFormats) {
        formats = newFormats;
    }
}
