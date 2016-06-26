package com.cloud.network.lb;

import static java.lang.String.format;

import com.cloud.configuration.Config;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LBHealthCheckManagerImpl extends ManagerBase implements LBHealthCheckManager, Manager {
    private static final Logger s_logger = LoggerFactory.getLogger(LBHealthCheckManagerImpl.class);

    @Inject
    ConfigurationDao _configDao;
    @Inject
    LoadBalancingRulesService _lbService;
    ScheduledExecutorService _executor;
    private String name;
    private Map<String, String> _configs;
    private long _interval;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _configs = _configDao.getConfiguration("management-server", params);
        if (s_logger.isInfoEnabled()) {
            s_logger.info(format("Configuring LBHealthCheck Manager %1$s", name));
        }
        this.name = name;
        _interval = NumbersUtil.parseLong(_configs.get(Config.LBHealthCheck.key()), 600);
        _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LBHealthCheck"));
        return true;
    }

    @Override
    public boolean start() {
        s_logger.debug("LB HealthCheckmanager is getting Started");
        _executor.scheduleAtFixedRate(new UpdateLBHealthCheck(), 10, _interval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        s_logger.debug("HealthCheckmanager is getting Stopped");
        _executor.shutdown();
        return true;
    }

    @Override
    public void updateLBHealthCheck(final Scheme scheme) {
        try {
            _lbService.updateLBHealthChecks(scheme);
        } catch (final ResourceUnavailableException e) {
            s_logger.debug("Error while updating the LB HealtCheck ", e);
        }
        s_logger.debug("LB HealthCheck Manager is running and getting the updates from LB providers and updating service status");
    }

    protected class UpdateLBHealthCheck extends ManagedContextRunnable {
        @Override
        protected void runInContext() {
            try {
                updateLBHealthCheck(Scheme.Public);
                updateLBHealthCheck(Scheme.Internal);
            } catch (final Exception e) {
                s_logger.error("Exception in LB HealthCheck Update Checker", e);
            }
        }
    }
}
