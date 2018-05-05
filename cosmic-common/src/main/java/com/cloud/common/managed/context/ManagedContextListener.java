package com.cloud.common.managed.context;

public interface ManagedContextListener<T> {

    /**
     * @param reentry True if listener is being invoked in a nested context
     * @return
     */
    T onEnterContext(boolean reentry);

    /**
     * @param data    The data returned from the onEnterContext call
     * @param reentry True if listener is being invoked in a nested context
     */
    void onLeaveContext(T data, boolean reentry);
}
