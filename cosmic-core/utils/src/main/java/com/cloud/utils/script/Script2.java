//

//

package com.cloud.utils.script;

import java.util.HashMap;

import org.slf4j.Logger;

public class Script2 extends Script {
    HashMap<String, ParamType> _params = new HashMap<>();

    public Script2(final String command, final Logger logger) {
        this(command, 0, logger);
    }

    public Script2(final String command, final long timeout, final Logger logger) {
        super(command, timeout, logger);
    }

    @Override
    public void add(final String param) {
        add(param, ParamType.NORMAL);
    }

    public void add(final String param, final ParamType type) {
        _params.put(param, type);
        super.add(param);
    }

    @Override
    protected String buildCommandLine(final String[] command) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            final String cmd = command[i];
            final ParamType type = getType(cmd);
            if (type == ParamType.PASSWORD) {
                builder.append("******").append(" ");
            } else {
                builder.append(command[i]).append(" ");
            }
        }

        return builder.toString();
    }

    private ParamType getType(final String cmd) {
        return _params.get(cmd);
    }

    public static enum ParamType {
        NORMAL, PASSWORD,
    }
}
