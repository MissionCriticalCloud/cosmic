package com.cloud.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClusterService extends Remote {
    String execute(ClusterServicePdu pdu) throws RemoteException;

    boolean ping(String callingPeer) throws RemoteException;
}
