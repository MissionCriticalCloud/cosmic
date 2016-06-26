package org.apache.cloudstack.spring.lifecycle;

import com.cloud.utils.component.ComponentLifecycle;
import com.cloud.utils.component.SystemIntegrityChecker;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.mgmt.JmxUtil;
import com.cloud.utils.mgmt.ManagementBean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.naming.ConfigurationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudStackExtendedLifeCycle extends AbstractBeanCollector {

    private static final Logger log = LoggerFactory.getLogger(CloudStackExtendedLifeCycle.class);

    Map<Integer, Set<ComponentLifecycle>> sorted = new TreeMap<>();

    public CloudStackExtendedLifeCycle() {
        super();
        setTypeClasses(new Class<?>[]{ComponentLifecycle.class, SystemIntegrityChecker.class});
    }

    @Override
    public void start() {
        sortBeans();
        checkIntegrity();
        configure();

        super.start();
    }

    private void sortBeans() {
        for (final ComponentLifecycle lifecycle : getBeans(ComponentLifecycle.class)) {
            Set<ComponentLifecycle> set = sorted.get(lifecycle.getRunLevel());

            if (set == null) {
                set = new HashSet<>();
                sorted.put(lifecycle.getRunLevel(), set);
            }

            set.add(lifecycle);
        }
    }

    protected void checkIntegrity() {
        for (final SystemIntegrityChecker checker : getBeans(SystemIntegrityChecker.class)) {
            log.info("Running system integrity checker {}", checker);

            checker.check();
        }
    }

    private void configure() {
        log.info("Configuring CloudStack Components");

        with(new WithComponentLifeCycle() {
            @Override
            public void with(final ComponentLifecycle lifecycle) {
                try {
                    lifecycle.configure(lifecycle.getName(), lifecycle.getConfigParams());
                } catch (final ConfigurationException e) {
                    log.error("Failed to configure {}", lifecycle.getName(), e);
                    throw new CloudRuntimeException(e);
                }
            }
        });

        log.info("Done Configuring CloudStack Components");
    }

    protected void with(final WithComponentLifeCycle with) {
        for (final Set<ComponentLifecycle> lifecycles : sorted.values()) {
            for (final ComponentLifecycle lifecycle : lifecycles) {
                with.with(lifecycle);
            }
        }
    }

    @Override
    public void stop() {
        with(new WithComponentLifeCycle() {
            @Override
            public void with(final ComponentLifecycle lifecycle) {
                lifecycle.stop();
            }
        });

        super.stop();
    }

    public void startBeans() {
        log.info("Starting CloudStack Components");

        with(new WithComponentLifeCycle() {
            @Override
            public void with(final ComponentLifecycle lifecycle) {
                lifecycle.start();

                if (lifecycle instanceof ManagementBean) {
                    final ManagementBean mbean = (ManagementBean) lifecycle;
                    try {
                        JmxUtil.registerMBean(mbean);
                    } catch (final MalformedObjectNameException e) {
                        log.warn("Unable to register MBean: " + mbean.getName(), e);
                    } catch (final InstanceAlreadyExistsException e) {
                        log.warn("Unable to register MBean: " + mbean.getName(), e);
                    } catch (final MBeanRegistrationException e) {
                        log.warn("Unable to register MBean: " + mbean.getName(), e);
                    } catch (final NotCompliantMBeanException e) {
                        log.warn("Unable to register MBean: " + mbean.getName(), e);
                    }
                    log.info("Registered MBean: " + mbean.getName());
                }
            }
        });

        log.info("Done Starting CloudStack Components");
    }

    public void stopBeans() {
        with(new WithComponentLifeCycle() {
            @Override
            public void with(final ComponentLifecycle lifecycle) {
                log.info("stopping bean " + lifecycle.getName());
                lifecycle.stop();
            }
        });
    }

    @Override
    public int getPhase() {
        return 2000;
    }

    private static interface WithComponentLifeCycle {
        public void with(ComponentLifecycle lifecycle);
    }
}
