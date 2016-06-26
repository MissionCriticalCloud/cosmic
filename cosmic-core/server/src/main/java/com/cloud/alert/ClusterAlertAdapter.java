package com.cloud.alert;

import com.cloud.cluster.ClusterManager;
import com.cloud.cluster.ClusterNodeJoinEventArgs;
import com.cloud.cluster.ClusterNodeLeftEventArgs;
import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.cluster.dao.ManagementServerHostDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.events.EventArgs;
import com.cloud.utils.events.SubscriptionMgr;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClusterAlertAdapter extends AdapterBase implements AlertAdapter {

    private static final Logger s_logger = LoggerFactory.getLogger(ClusterAlertAdapter.class);

    @Inject
    private AlertManager _alertMgr;
    @Inject
    private ManagementServerHostDao _mshostDao;

    public void onClusterAlert(final Object sender, final EventArgs args) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Receive cluster alert, EventArgs: " + args.getClass().getName());
        }

        if (args instanceof ClusterNodeJoinEventArgs) {
            onClusterNodeJoined(sender, (ClusterNodeJoinEventArgs) args);
        } else if (args instanceof ClusterNodeLeftEventArgs) {
            onClusterNodeLeft(sender, (ClusterNodeLeftEventArgs) args);
        } else {
            s_logger.error("Unrecognized cluster alert event");
        }
    }

    private void onClusterNodeJoined(final Object sender, final ClusterNodeJoinEventArgs args) {
        if (s_logger.isDebugEnabled()) {
            for (final ManagementServerHostVO mshost : args.getJoinedNodes()) {
                s_logger.debug("Handle cluster node join alert, joined node: " + mshost.getServiceIP() + ", msidL: " + mshost.getMsid());
            }
        }

        for (final ManagementServerHostVO mshost : args.getJoinedNodes()) {
            if (mshost.getId() == args.getSelf().longValue()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Management server node " + mshost.getServiceIP() + " is up, send alert");
                }

                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_MANAGMENT_NODE, 0, new Long(0), "Management server node " + mshost.getServiceIP() + " is up", "");
                break;
            }
        }
    }

    private void onClusterNodeLeft(final Object sender, final ClusterNodeLeftEventArgs args) {

        if (s_logger.isDebugEnabled()) {
            for (final ManagementServerHostVO mshost : args.getLeftNodes()) {
                s_logger.debug("Handle cluster node left alert, leaving node: " + mshost.getServiceIP() + ", msid: " + mshost.getMsid());
            }
        }

        for (final ManagementServerHostVO mshost : args.getLeftNodes()) {
            if (mshost.getId() != args.getSelf().longValue()) {
                if (_mshostDao.increaseAlertCount(mshost.getId()) > 0) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Detected management server node " + mshost.getServiceIP() + " is down, send alert");
                    }
                    _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_MANAGMENT_NODE, 0, new Long(0), "Management server node " + mshost.getServiceIP() + " is down",
                            "");
                } else {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Detected management server node " + mshost.getServiceIP() + " is down, but alert has already been set");
                    }
                }
            }
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start configuring cluster alert manager : " + name);
        }

        try {
            SubscriptionMgr.getInstance().subscribe(ClusterManager.ALERT_SUBJECT, this, "onClusterAlert");
        } catch (final SecurityException e) {
            throw new ConfigurationException("Unable to register cluster event subscription");
        } catch (final NoSuchMethodException e) {
            throw new ConfigurationException("Unable to register cluster event subscription");
        }

        return true;
    }
}
