package com.cloud.cluster;

import com.cloud.utils.DateUtil;

import javax.management.StandardMBean;
import java.util.Date;
import java.util.TimeZone;

public class ClusterManagerMBeanImpl extends StandardMBean implements ClusterManagerMBean {
    private final ClusterManagerImpl _clusterMgr;
    private final ManagementServerHostVO _mshostVo;

    public ClusterManagerMBeanImpl(final ClusterManagerImpl clusterMgr, final ManagementServerHostVO mshostVo) {
        super(ClusterManagerMBean.class, false);

        _clusterMgr = clusterMgr;
        _mshostVo = mshostVo;
    }

    @Override
    public long getMsid() {
        return _mshostVo.getMsid();
    }

    @Override
    public String getLastUpdateTime() {
        final Date date = _mshostVo.getLastUpdateTime();
        return DateUtil.getDateDisplayString(TimeZone.getDefault(), date);
    }

    @Override
    public String getClusterNodeIP() {
        return _mshostVo.getServiceIP();
    }

    @Override
    public String getVersion() {
        return _mshostVo.getVersion();
    }

    @Override
    public int getHeartbeatInterval() {
        return _clusterMgr.getHeartbeatInterval();
    }

    @Override
    public int getHeartbeatThreshold() {
        return ClusterManager.HeartbeatThreshold.value();
    }
}
