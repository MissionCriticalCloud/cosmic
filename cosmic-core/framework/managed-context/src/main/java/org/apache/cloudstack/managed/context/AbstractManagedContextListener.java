package org.apache.cloudstack.managed.context;

public class AbstractManagedContextListener<T> implements ManagedContextListener<T> {

    @Override
    public T onEnterContext(final boolean reentry) {
        return null;
    }

    @Override
    public void onLeaveContext(final T data, final boolean reentry) {
    }
}
