//

//

package com.cloud.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class CloudResourceBundle {

    private final ResourceBundle _bundle;

    public CloudResourceBundle(final ResourceBundle bundle) {
        _bundle = bundle;
    }

    public static CloudResourceBundle getBundle(final String baseName, final Locale locale) {
        return new CloudResourceBundle(ResourceBundle.getBundle(baseName, locale));
    }

    public String t(final String key) {
        return getString(key);
    }

    private String getString(final String key) {
        try {
            return _bundle.getString(key);
        } catch (final Exception e) {
            return key; //if translation is not found, just return original word (i.e. English).
        }
    }
}
