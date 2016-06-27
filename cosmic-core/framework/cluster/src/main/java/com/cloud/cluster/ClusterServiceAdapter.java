package com.cloud.cluster;

import com.cloud.utils.component.Adapter;
import org.apache.cloudstack.framework.config.ConfigKey;

import java.rmi.RemoteException;

public interface ClusterServiceAdapter extends Adapter {
    final ConfigKey<Integer> ClusterMessageTimeOut = new ConfigKey<>(Integer.class, "cluster.message.timeout.seconds", "Advance", "300",
            "Time (in seconds) to wait before a inter-management server message post times out.", true);

    public ClusterService getPeerService(String strPeer) throws RemoteException;

    public String getServiceEndpointName(String strPeer);

    public int getServicePort();
}
