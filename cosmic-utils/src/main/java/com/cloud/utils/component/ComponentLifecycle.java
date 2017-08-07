package com.cloud.utils.component;

import javax.naming.ConfigurationException;
import java.util.Map;

public interface ComponentLifecycle extends Named {
    int RUN_LEVEL_SYSTEM_BOOTSTRAP = 0;        // for system level bootstrap components
    int RUN_LEVEL_SYSTEM = 1;                // for system level service components (i.e., DAOs)
    int RUN_LEVEL_FRAMEWORK_BOOTSTRAP = 2;    // for framework startup checkers (i.e., DB migration check)
    int RUN_LEVEL_FRAMEWORK = 3;            // for framework bootstrap components(i.e., clustering management components)
    int RUN_LEVEL_COMPONENT_BOOTSTRAP = 4;    // general manager components
    int RUN_LEVEL_COMPONENT = 5;            // regular adapters, plugin components
    int RUN_LEVEL_APPLICATION_MAINLOOP = 6;
    int MAX_RUN_LEVELS = 7;

    @Override
    String getName();

    void setName(String name);

    Map<String, Object> getConfigParams();

    void setConfigParams(Map<String, Object> params);

    int getRunLevel();

    void setRunLevel(int level);

    boolean configure(String name, Map<String, Object> params) throws ConfigurationException;

    /**
     * Start any background tasks.
     *
     * @return true if the tasks were started, false otherwise.
     */
    boolean start();

    /**
     * Stop any background tasks.
     *
     * @return true background tasks were stopped, false otherwise.
     */
    boolean stop();
}
