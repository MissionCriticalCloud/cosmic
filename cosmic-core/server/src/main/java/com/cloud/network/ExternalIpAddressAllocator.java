package com.cloud.network;

import static com.cloud.utils.AutoCloseableUtil.closeAutoCloseable;

import com.cloud.dc.dao.VlanDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalIpAddressAllocator extends AdapterBase implements IpAddrAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(ExternalIpAddressAllocator.class);
    @Inject
    ConfigurationDao _configDao = null;
    @Inject
    IPAddressDao _ipAddressDao = null;
    @Inject
    VlanDao _vlanDao;
    private boolean _isExternalIpAllocatorEnabled = false;
    private String _externalIpAllocatorUrl = null;

    @Override
    public IpAddr getPublicIpAddress(final String macAddr, final long dcId, final long podId) {
        /*TODO: call API to get  ip address from external DHCP server*/
        return getPrivateIpAddress(macAddr, dcId, podId);
    }

    @Override
    public IpAddr getPrivateIpAddress(final String macAddr, final long dcId, final long podId) {
        if (_externalIpAllocatorUrl == null || _externalIpAllocatorUrl.equalsIgnoreCase("")) {
            return new IpAddr();
        }
        final String urlString = _externalIpAllocatorUrl + "?command=getIpAddr&mac=" + macAddr + "&dc=" + dcId + "&pod=" + podId;
        s_logger.debug("getIP:" + urlString);

        BufferedReader in = null;
        try {
            final URL url = new URL(urlString);
            final URLConnection conn = url.openConnection();
            conn.setReadTimeout(30000);

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String inputLine;
            while ((inputLine = in.readLine()) != null) {
                s_logger.debug(inputLine);
                final String[] tokens = inputLine.split(",");
                if (tokens.length != 3) {
                    s_logger.debug("the return value should be: mac,netmask,gateway");
                    return new IpAddr();
                }
                return new IpAddr(tokens[0], tokens[1], tokens[2]);
            }

            return new IpAddr();
        } catch (final MalformedURLException e) {
            throw new CloudRuntimeException("URL is malformed " + urlString, e);
        } catch (final IOException e) {
            return new IpAddr();
        } finally {
            closeAutoCloseable(in, "closing buffered reader");
        }
    }

    @Override
    public boolean releasePublicIpAddress(final String ip, final long dcId, final long podId) {
        /*TODO: call API to release the ip address from external DHCP server*/
        return releasePrivateIpAddress(ip, dcId, podId);
    }

    @Override
    public boolean releasePrivateIpAddress(final String ip, final long dcId, final long podId) {
        /*TODO: call API to release the ip address from external DHCP server*/
        if (_externalIpAllocatorUrl == null || _externalIpAllocatorUrl.equalsIgnoreCase("")) {
            return false;
        }

        final String urlString = _externalIpAllocatorUrl + "?command=releaseIpAddr&ip=" + ip + "&dc=" + dcId + "&pod=" + podId;

        s_logger.debug("releaseIP:" + urlString);
        BufferedReader in = null;
        try {
            final URL url = new URL(urlString);
            final URLConnection conn = url.openConnection();
            conn.setReadTimeout(30000);

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            return true;
        } catch (final MalformedURLException e) {
            throw new CloudRuntimeException("URL is malformed " + urlString, e);
        } catch (final IOException e) {
            return false;
        } finally {
            closeAutoCloseable(in, "buffered reader close");
        }
    }

    @Override
    public boolean externalIpAddressAllocatorEnabled() {
        return _isExternalIpAllocatorEnabled;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _isExternalIpAllocatorEnabled = Boolean.parseBoolean(_configDao.getValue("direct.attach.network.externalIpAllocator.enabled"));
        _externalIpAllocatorUrl = _configDao.getValue("direct.attach.network.externalIpAllocator.url");
        _name = name;

        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
