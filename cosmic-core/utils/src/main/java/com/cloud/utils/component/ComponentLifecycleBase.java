//

//

package com.cloud.utils.component;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentLifecycleBase implements ComponentLifecycle {
    private static final Logger s_logger = LoggerFactory.getLogger(ComponentLifecycleBase.class);

    protected String _name;
    protected int _runLevel;
    protected Map<String, Object> _configParams = new HashMap<>();

    public ComponentLifecycleBase() {
        _name = this.getClass().getSimpleName();
        _runLevel = RUN_LEVEL_COMPONENT;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void setName(final String name) {
        _name = name;
    }

    @Override
    public Map<String, Object> getConfigParams() {
        return _configParams;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        _configParams = params;
    }

    @Override
    public int getRunLevel() {
        return _runLevel;
    }

    @Override
    public void setRunLevel(final int level) {
        _runLevel = level;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        _configParams = params;
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
