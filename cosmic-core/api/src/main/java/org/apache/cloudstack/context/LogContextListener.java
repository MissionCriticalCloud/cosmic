package org.apache.cloudstack.context;

import com.cloud.dao.EntityManager;
import org.apache.cloudstack.managed.context.ManagedContextListener;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class LogContextListener implements ManagedContextListener<Object> {

    @Inject
    EntityManager entityMgr;

    @Override
    public Object onEnterContext(final boolean reentry) {
        if (!reentry && LogContext.current() == null) {
            LogContext.registerSystemLogContextOnceOnly();
        }
        return null;
    }

    @Override
    public void onLeaveContext(final Object unused, final boolean reentry) {
        if (!reentry) {
            LogContext.unregister();
        }
    }

    @PostConstruct
    public void init() {
        LogContext.init(entityMgr);
    }
}
