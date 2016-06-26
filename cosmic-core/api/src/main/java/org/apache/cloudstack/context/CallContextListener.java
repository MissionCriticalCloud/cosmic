package org.apache.cloudstack.context;

import com.cloud.dao.EntityManager;
import org.apache.cloudstack.managed.context.ManagedContextListener;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class CallContextListener implements ManagedContextListener<Object> {

    @Inject
    EntityManager entityMgr;

    @Override
    public Object onEnterContext(final boolean reentry) {
        if (!reentry && CallContext.current() == null) {
            CallContext.registerSystemCallContextOnceOnly();
        }
        return null;
    }

    @Override
    public void onLeaveContext(final Object unused, final boolean reentry) {
        if (!reentry) {
            CallContext.unregisterAll();
        }
    }

    @PostConstruct
    public void init() {
        CallContext.init(entityMgr);
    }
}
