package com.cloud.cluster;

import com.cloud.cluster.dao.ManagementServerHostDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.ComponentLifecycle;
import com.cloud.utils.db.DbProperties;
import org.apache.cloudstack.framework.config.ConfigDepot;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterServiceServletAdapter extends AdapterBase implements ClusterServiceAdapter {

    private static final Logger s_logger = LoggerFactory.getLogger(ClusterServiceServletAdapter.class);
    private static final int DEFAULT_SERVICE_PORT = 9090;
    private static final int DEFAULT_REQUEST_TIMEOUT = 300;            // 300 seconds
    @Inject
    protected ConfigDepot _configDepot;
    @Inject
    private ClusterManager _manager;
    @Inject
    private ManagementServerHostDao _mshostDao;
    private ClusterServiceServletContainer _servletContainer;

    private int _clusterServicePort = DEFAULT_SERVICE_PORT;

    public ClusterServiceServletAdapter() {
        setRunLevel(ComponentLifecycle.RUN_LEVEL_FRAMEWORK);
    }

    @Override
    public ClusterService getPeerService(final String strPeer) throws RemoteException {
        try {
            init();
        } catch (final ConfigurationException e) {
            s_logger.error("Unable to init ClusterServiceServletAdapter");
            throw new RemoteException("Unable to init ClusterServiceServletAdapter");
        }

        final String serviceUrl = getServiceEndpointName(strPeer);
        if (serviceUrl == null) {
            return null;
        }

        return new ClusterServiceServletImpl(serviceUrl);
    }

    @Override
    public String getServiceEndpointName(final String strPeer) {
        try {
            init();
        } catch (final ConfigurationException e) {
            s_logger.error("Unable to init ClusterServiceServletAdapter");
            return null;
        }

        final long msid = Long.parseLong(strPeer);

        final ManagementServerHostVO mshost = _mshostDao.findByMsid(msid);
        if (mshost == null) {
            return null;
        }

        return composeEndpointName(mshost.getServiceIP(), mshost.getServicePort());
    }

    @Override
    public int getServicePort() {
        return _clusterServicePort;
    }

    private void init() throws ConfigurationException {
        if (_mshostDao != null) {
            return;
        }

        final Properties dbProps = DbProperties.getDbProperties();

        _clusterServicePort = NumbersUtil.parseInt(dbProps.getProperty("cluster.servlet.port"), DEFAULT_SERVICE_PORT);
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Cluster servlet port : " + _clusterServicePort);
        }
    }

    private String composeEndpointName(final String nodeIP, final int port) {
        final StringBuffer sb = new StringBuffer();
        sb.append("http://").append(nodeIP).append(":").append(port).append("/clusterservice");
        return sb.toString();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        init();
        return true;
    }

    @Override
    public boolean start() {
        _servletContainer = new ClusterServiceServletContainer();
        _servletContainer.start(new ClusterServiceServletHttpHandler(_manager), _clusterServicePort);
        return true;
    }

    @Override
    public boolean stop() {
        if (_servletContainer != null) {
            _servletContainer.stop();
        }
        return true;
    }
}
