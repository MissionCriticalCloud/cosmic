package com.cloud.utils.db;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.CallbackFilter;

public class UpdateFilter implements CallbackFilter {
    @Override
    public int accept(final Method method) {
        final String name = method.getName();
        return (name.startsWith("set") || name.startsWith("incr") || name.startsWith("decr")) ? 1 : 0;
    }
}
