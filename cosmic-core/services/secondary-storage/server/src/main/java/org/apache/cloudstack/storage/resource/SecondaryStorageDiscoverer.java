package org.apache.cloudstack.storage.resource;

import com.cloud.agent.AgentManager;
import com.cloud.host.HostVO;
import com.cloud.host.Status.Event;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ServerResource;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.resource.DummySecondaryStorageResource;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.net.NfsUtils;
import com.cloud.utils.script.Script;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SecondaryStorageDiscoverer is used to discover secondary
 * storage servers and make sure everything it can do is
 * correct.
 */
public class SecondaryStorageDiscoverer extends DiscovererBase implements Discoverer {
    private static final Logger s_logger = LoggerFactory.getLogger(SecondaryStorageDiscoverer.class);
    @Inject
    protected VMTemplateDao _tmpltDao = null;
    @Inject
    protected VMTemplateZoneDao _vmTemplateZoneDao = null;
    @Inject
    protected VMTemplateDao _vmTemplateDao = null;
    @Inject
    protected AgentManager _agentMgr = null;
    long _timeout = 2 * 60 * 1000; // 2 minutes
    String _mountParent;
    boolean _useServiceVM = false;
    Random _random = new Random(System.currentTimeMillis());

    protected SecondaryStorageDiscoverer() {
    }

    @Override
    public Map<? extends ServerResource, Map<String, String>>
    find(final long dcId, final Long podId, final Long clusterId, final URI uri, final String username, final String password, final List<String> hostTags) {
        if (!uri.getScheme().equalsIgnoreCase("nfs") && !uri.getScheme().equalsIgnoreCase("cifs") && !uri.getScheme().equalsIgnoreCase("file") &&
                !uri.getScheme().equalsIgnoreCase("iso") && !uri.getScheme().equalsIgnoreCase("dummy")) {
            s_logger.debug("It's not NFS or file or ISO, so not a secondary storage server: " + uri.toString());
            return null;
        }

        if (uri.getScheme().equalsIgnoreCase("nfs") || uri.getScheme().equalsIgnoreCase("cifs") || uri.getScheme().equalsIgnoreCase("iso")) {
            return createNfsSecondaryStorageResource(dcId, podId, uri);
        } else if (uri.getScheme().equalsIgnoreCase("file")) {
            return createLocalSecondaryStorageResource(dcId, podId, uri);
        } else if (uri.getScheme().equalsIgnoreCase("dummy")) {
            return createDummySecondaryStorageResource(dcId, podId, uri);
        } else {
            return null;
        }
    }

    protected Map<? extends ServerResource, Map<String, String>> createNfsSecondaryStorageResource(final long dcId, final Long podId, final URI uri) {

        if (_useServiceVM) {
            return createDummySecondaryStorageResource(dcId, podId, uri);
        }
        final String mountStr = NfsUtils.uri2Mount(uri);

        Script script = new Script(true, "mount", _timeout, s_logger);
        String mntPoint = null;
        File file = null;
        do {
            mntPoint = _mountParent + File.separator + Integer.toHexString(_random.nextInt(Integer.MAX_VALUE));
            file = new File(mntPoint);
        } while (file.exists());

        if (!file.mkdirs()) {
            s_logger.warn("Unable to make directory: " + mntPoint);
            return null;
        }

        script.add(mountStr, mntPoint);
        final String result = script.execute();
        if (result != null && !result.contains("already mounted")) {
            s_logger.warn("Unable to mount " + uri.toString() + " due to " + result);
            file.delete();
            return null;
        }

        script = new Script(true, "umount", 0, s_logger);
        script.add(mntPoint);
        script.execute();

        file.delete();

        final Map<NfsSecondaryStorageResource, Map<String, String>> srs = new HashMap<>();

        final NfsSecondaryStorageResource storage;
        if (_configDao.isPremium()) {
            final Class<?> impl;
            final String name = "com.cloud.storage.resource.PremiumSecondaryStorageResource";
            try {
                impl = Class.forName(name);
                final Constructor<?> constructor = impl.getDeclaredConstructor();
                constructor.setAccessible(true);
                storage = (NfsSecondaryStorageResource) constructor.newInstance();
            } catch (final ClassNotFoundException e) {
                s_logger.error("Unable to load com.cloud.storage.resource.PremiumSecondaryStorageResource due to ClassNotFoundException");
                return null;
            } catch (final SecurityException e) {
                s_logger.error("Unable to load com.cloud.storage.resource.PremiumSecondaryStorageResource due to SecurityException");
                return null;
            } catch (final NoSuchMethodException e) {
                s_logger.error("Unable to load com.cloud.storage.resource.PremiumSecondaryStorageResource due to NoSuchMethodException");
                return null;
            } catch (final IllegalArgumentException e) {
                s_logger.error("Unable to load com.cloud.storage.resource.PremiumSecondaryStorageResource due to IllegalArgumentException");
                return null;
            } catch (final InstantiationException e) {
                s_logger.error("Unable to load com.cloud.storage.resource.PremiumSecondaryStorageResource due to InstantiationException");
                return null;
            } catch (final IllegalAccessException e) {
                s_logger.error("Unable to load com.cloud.storage.resource.PremiumSecondaryStorageResource due to IllegalAccessException");
                return null;
            } catch (final InvocationTargetException e) {
                s_logger.error("Unable to load com.cloud.storage.resource.PremiumSecondaryStorageResource due to InvocationTargetException");
                return null;
            }
        } else {
            storage = new NfsSecondaryStorageResource();
        }

        final Map<String, String> details = new HashMap<>();
        details.put("mount.path", mountStr);
        details.put("orig.url", uri.toString());
        details.put("mount.parent", _mountParent);

        final Map<String, Object> params = new HashMap<>();
        params.putAll(details);
        params.put("zone", Long.toString(dcId));
        if (podId != null) {
            params.put("pod", podId.toString());
        }
        params.put("guid", uri.toString());
        params.put("secondary.storage.vm", "false");
        params.put("max.template.iso.size", _configDao.getValue("max.template.iso.size"));

        try {
            storage.configure("Storage", params);
        } catch (final ConfigurationException e) {
            s_logger.warn("Unable to configure the storage ", e);
            return null;
        }
        srs.put(storage, details);

        return srs;
    }

    protected Map<? extends ServerResource, Map<String, String>> createLocalSecondaryStorageResource(final long dcId, final Long podId, final URI uri) {
        final Map<LocalSecondaryStorageResource, Map<String, String>> srs = new HashMap<>();

        LocalSecondaryStorageResource storage = new LocalSecondaryStorageResource();
        storage = ComponentContext.inject(storage);

        final Map<String, String> details = new HashMap<>();

        final File file = new File(uri);
        details.put("mount.path", file.getAbsolutePath());
        details.put("orig.url", uri.toString());

        final Map<String, Object> params = new HashMap<>();
        params.putAll(details);
        params.put("zone", Long.toString(dcId));
        if (podId != null) {
            params.put("pod", podId.toString());
        }
        params.put("guid", uri.toString());

        try {
            storage.configure("Storage", params);
        } catch (final ConfigurationException e) {
            s_logger.warn("Unable to configure the storage ", e);
            return null;
        }
        srs.put(storage, details);

        return srs;
    }

    protected Map<ServerResource, Map<String, String>> createDummySecondaryStorageResource(final long dcId, final Long podId, final URI uri) {
        final Map<ServerResource, Map<String, String>> srs = new HashMap<>();

        DummySecondaryStorageResource storage = new DummySecondaryStorageResource(_useServiceVM);
        storage = ComponentContext.inject(storage);

        final Map<String, String> details = new HashMap<>();

        details.put("mount.path", uri.toString());
        details.put("orig.url", uri.toString());

        final Map<String, Object> params = new HashMap<>();
        params.putAll(details);
        params.put("zone", Long.toString(dcId));
        if (podId != null) {
            params.put("pod", podId.toString());
        }
        params.put("guid", uri.toString());

        try {
            storage.configure("Storage", params);
        } catch (final ConfigurationException e) {
            s_logger.warn("Unable to configure the storage ", e);
            return null;
        }
        srs.put(storage, details);

        return srs;
    }

    @Override
    public void postDiscovery(final List<HostVO> hosts, final long msId) {
        if (_useServiceVM) {
            for (final HostVO h : hosts) {
                _agentMgr.agentStatusTransitTo(h, Event.AgentDisconnected, msId);
            }
        }
        for (final HostVO h : hosts) {
            associateTemplatesToZone(h.getId(), h.getDataCenterId());
        }
    }

    @Override
    public boolean matchHypervisor(final String hypervisor) {
        if (hypervisor.equals("SecondaryStorage")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Hypervisor.HypervisorType getHypervisorType() {
        return Hypervisor.HypervisorType.None;
    }

    private void associateTemplatesToZone(final long hostId, final long dcId) {
        VMTemplateZoneVO tmpltZone;

        final List<VMTemplateVO> allTemplates = _vmTemplateDao.listAll();
        for (final VMTemplateVO vt : allTemplates) {
            if (vt.isCrossZones()) {
                tmpltZone = _vmTemplateZoneDao.findByZoneTemplate(dcId, vt.getId());
                if (tmpltZone == null) {
                    final VMTemplateZoneVO vmTemplateZone = new VMTemplateZoneVO(dcId, vt.getId(), new Date());
                    _vmTemplateZoneDao.persist(vmTemplateZone);
                }
            }
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        _mountParent = _params.get("mount.parent");
        if (_mountParent == null) {
            _mountParent = "/mnt";
        }

        final String useServiceVM = _params.get("secondary.storage.vm");
        if ("true".equalsIgnoreCase(useServiceVM)) {
            _useServiceVM = true;
        }
        return true;
    }
}
