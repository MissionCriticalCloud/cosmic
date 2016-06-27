package org.apache.cloudstack.managed.context;

public interface ManagedContextListener<T> {

    /**
     * @param reentry True if listener is being invoked in a nested context
     * @return
     */
    public T onEnterContext(boolean reentry);

    /**
     * @param data    The data returned from the onEnterContext call
     * @param reentry True if listener is being invoked in a nested context
     */
    public void onLeaveContext(T data, boolean reentry);
}
