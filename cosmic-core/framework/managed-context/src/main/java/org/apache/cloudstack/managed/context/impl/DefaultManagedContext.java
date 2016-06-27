package org.apache.cloudstack.managed.context.impl;

import org.apache.cloudstack.managed.context.ManagedContext;
import org.apache.cloudstack.managed.context.ManagedContextListener;
import org.apache.cloudstack.managed.context.ManagedContextUtils;
import org.apache.cloudstack.managed.threadlocal.ManagedThreadLocal;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultManagedContext implements ManagedContext {

    private static final Logger log = LoggerFactory.getLogger(DefaultManagedContext.class);

    List<ManagedContextListener<?>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void registerListener(final ManagedContextListener<?> listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(final ManagedContextListener<?> listener) {
        listeners.remove(listener);
    }

    @Override
    public void runWithContext(final Runnable run) {
        try {
            callWithContext(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    run.run();
                    return null;
                }
            });
        } catch (final Exception e) {
            /* Only care about non-checked exceptions
             * as the nature of runnable prevents checked
             * exceptions from happening
             */
            ManagedContextUtils.rethrowException(e);
        }
    }

    @Override
    public <T> T callWithContext(final Callable<T> callable) throws Exception {
        final Object owner = new Object();

        final Stack<ListenerInvocation> invocations = new Stack<>();
        final boolean reentry = !ManagedContextUtils.setAndCheckOwner(owner);
        Throwable firstError = null;

        try {
            for (final ManagedContextListener<?> listener : listeners) {
                Object data = null;

                try {
                    data = listener.onEnterContext(reentry);
                } catch (final Throwable t) {
                    /* If one listener fails, still call all other listeners
                     * and then we will call onLeaveContext for all
                     */
                    if (firstError == null) {
                        firstError = t;
                    }
                    log.error("Failed onEnterContext for listener [{}]", listener, t);
                }

                /* Stack data structure is used because in between onEnter and onLeave
                 * the listeners list could have changed
                 */
                invocations.push(new ListenerInvocation((ManagedContextListener<Object>) listener, data));
            }

            try {
                if (firstError == null) {
                    /* Only call if all the listeners didn't blow up on onEnterContext */
                    return callable.call();
                } else {
                    throwException(firstError);
                    return null;
                }
            } finally {
                Throwable lastError = null;

                while (!invocations.isEmpty()) {
                    final ListenerInvocation invocation = invocations.pop();
                    try {
                        invocation.listener.onLeaveContext(invocation.data, reentry);
                    } catch (final Throwable t) {
                        lastError = t;
                        log.error("Failed onLeaveContext for listener [{}]", invocation.listener, t);
                    }
                }

                if (firstError == null && lastError != null) {
                    throwException(lastError);
                }
            }
        } finally {
            if (ManagedContextUtils.clearOwner(owner)) {
                ManagedThreadLocal.reset();
            }
        }
    }

    protected void throwException(final Throwable t) throws Exception {
        ManagedContextUtils.rethrowException(t);
        if (t instanceof Exception) {
            throw (Exception) t;
        }
    }

    public List<ManagedContextListener<?>> getListeners() {
        return listeners;
    }

    public void setListeners(final List<ManagedContextListener<?>> listeners) {
        this.listeners = new CopyOnWriteArrayList<>(listeners);
    }

    private static class ListenerInvocation {
        ManagedContextListener<Object> listener;
        Object data;

        public ListenerInvocation(final ManagedContextListener<Object> listener, final Object data) {
            super();
            this.listener = listener;
            this.data = data;
        }
    }
}
