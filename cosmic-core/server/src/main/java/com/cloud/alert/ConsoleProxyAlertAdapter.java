package com.cloud.alert;

import com.cloud.consoleproxy.ConsoleProxyAlertEventArgs;
import com.cloud.consoleproxy.ConsoleProxyManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.events.SubscriptionMgr;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.dao.ConsoleProxyDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleProxyAlertAdapter extends AdapterBase implements AlertAdapter {

    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyAlertAdapter.class);

    @Inject
    private AlertManager _alertMgr;
    @Inject
    private ConsoleProxyDao _consoleProxyDao;
    @Inject
    private ZoneRepository zoneRepository;

    public void onProxyAlert(final Object sender, final ConsoleProxyAlertEventArgs args) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("received console proxy alert");
        }

        final Zone zone = zoneRepository.findById(args.getZoneId()).orElse(null);
        ConsoleProxyVO proxy = args.getProxy();
        //FIXME - Proxy can be null in case of creation failure. Have a better fix than checking for != 0
        if (proxy == null && args.getProxyId() != 0) {
            proxy = _consoleProxyDao.findById(args.getProxyId());
        }

        if (proxy == null && args.getType() != ConsoleProxyAlertEventArgs.PROXY_CREATE_FAILURE) {
            throw new CloudRuntimeException("Invalid alert arguments, proxy must be set");
        }

        switch (args.getType()) {
            case ConsoleProxyAlertEventArgs.PROXY_CREATED:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("New console proxy created, zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() +
                            ", private IP: " + proxy.getPrivateIpAddress());
                }
                break;

            case ConsoleProxyAlertEventArgs.PROXY_UP:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Console proxy is up, zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() +
                            ", private IP: " + proxy.getPrivateIpAddress());
                }

                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_CONSOLE_PROXY, args.getZoneId(), proxy.getPodIdToDeployIn(),
                        "Console proxy up in zone: " + zone.getName() +
                                ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " +
                                (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()), "Console proxy up (zone " + zone.getName() + ")");
                break;

            case ConsoleProxyAlertEventArgs.PROXY_DOWN:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Console proxy is down, zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() +
                            ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
                }

                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_CONSOLE_PROXY, args.getZoneId(), proxy.getPodIdToDeployIn(),
                        "Console proxy down in zone: " + zone.getName() +
                                ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " +
                                (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()), "Console proxy down (zone " + zone.getName() + ")");
                break;

            case ConsoleProxyAlertEventArgs.PROXY_REBOOTED:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Console proxy is rebooted, zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() +
                            ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
                }

                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_CONSOLE_PROXY, args.getZoneId(), proxy.getPodIdToDeployIn(),
                        "Console proxy rebooted in zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() +
                                ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()), "Console proxy rebooted (zone " + zone.getName() +
                                ")");
                break;

            case ConsoleProxyAlertEventArgs.PROXY_CREATE_FAILURE:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Console proxy creation failure, zone: " + zone.getName());
                }
                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_CONSOLE_PROXY, args.getZoneId(), null,
                        "Console proxy creation failure. zone: " + zone.getName() + ", error details: " + args.getMessage(),
                        "Console proxy creation failure (zone " + zone.getName() + ")");
                break;

            case ConsoleProxyAlertEventArgs.PROXY_START_FAILURE:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Console proxy startup failure, zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " +
                            proxy.getPublicIpAddress() + ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
                }

                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_CONSOLE_PROXY, args.getZoneId(), proxy.getPodIdToDeployIn(),
                        "Console proxy startup failure. zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() +
                                ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()) + ", error details: " + args.getMessage(),
                        "Console proxy startup failure (zone " + zone.getName() + ")");
                break;

            case ConsoleProxyAlertEventArgs.PROXY_FIREWALL_ALERT:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Console proxy firewall alert, zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " +
                            proxy.getPublicIpAddress() + ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
                }

                _alertMgr.sendAlert(
                        AlertManager.AlertType.ALERT_TYPE_CONSOLE_PROXY,
                        args.getZoneId(),
                        proxy.getPodIdToDeployIn(),
                        "Failed to open console proxy firewall port. zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " +
                                proxy.getPublicIpAddress() + ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()),
                        "Console proxy alert (zone " + zone.getName() + ")");
                break;

            case ConsoleProxyAlertEventArgs.PROXY_STORAGE_ALERT:
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Console proxy storage alert, zone: " + zone.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " +
                            proxy.getPublicIpAddress() + ", private IP: " + proxy.getPrivateIpAddress() + ", message: " + args.getMessage());
                }

                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_STORAGE_MISC, args.getZoneId(), proxy.getPodIdToDeployIn(),
                        "Console proxy storage issue. zone: " + zone.getName() + ", message: " + args.getMessage(), "Console proxy alert (zone " + zone.getName() + ")");
                break;
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start configuring console proxy alert manager : " + name);
        }

        try {
            SubscriptionMgr.getInstance().subscribe(ConsoleProxyManager.ALERT_SUBJECT, this, "onProxyAlert");
        } catch (final SecurityException e) {
            throw new ConfigurationException("Unable to register console proxy event subscription, exception: " + e);
        } catch (final NoSuchMethodException e) {
            throw new ConfigurationException("Unable to register console proxy event subscription, exception: " + e);
        }

        return true;
    }
}
