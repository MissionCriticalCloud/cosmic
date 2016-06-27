//

//

package com.cloud.utils.component;

/**
 * before any other components are loaded.  Within ComponentLocator
 * database upgrades and other verification to make sure it works.
 */
public interface SystemIntegrityChecker {
    public static final String Name = "system-integrity-checker";

    void check();
}
