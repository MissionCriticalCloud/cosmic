//

//

package com.cloud.utils.component;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;

/**
 * Copied/Modified from Spring source
 */
public class ComponentNamingPolicy implements NamingPolicy {

    public static final ComponentNamingPolicy INSTANCE = new ComponentNamingPolicy();

    @Override
    public String getClassName(String prefix, final String source, final Object key, final Predicate names) {
        if (prefix == null) {
            prefix = "net.sf.cglib.empty.Object";
        } else if (prefix.startsWith("java")) {
            prefix = "_" + prefix;
        }
        final String base = prefix + "_" + source.substring(source.lastIndexOf('.') + 1) + getTag() + "_" + Integer.toHexString(key.hashCode());
        String attempt = base;
        int index = 2;
        while (names.evaluate(attempt)) {
            attempt = base + "_" + index++;
        }
        return attempt;
    }

    /**
     * Returns a string which is incorporated into every generated class name.
     * By default returns "ByCloudStack"
     */
    protected String getTag() {
        return "ByCloudStack";
    }

    @Override
    public int hashCode() {
        return getTag().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof ComponentNamingPolicy) && ((ComponentNamingPolicy) o).getTag().equals(getTag());
    }
}
