//

//

package com.cloud.utils.component;

import java.util.List;

// This interface defines methods for pluggable code within the Cloud Stack.
public interface PluggableService {
    // The config command properties filenames that lists allowed API commands
    // and role masks supported by this pluggable service
    List<Class<?>> getCommands();
}
