package com.cloud.network;

import com.cloud.network.Networks.TrafficType;

/* User can provide a Label, while configuring a zone, to specify
 * a physical network that is to be used for a traffic type defined
 * by CloudStack. See the enum data type TrafficType. This label is
 * called Traffic label. This might encapsulate physical network
 * specific properties like VLAN ID, name of virtual network object or more.
 * The name of virtual network object is dependent on type of hypervisor.
 * For example it is name of xenserver bridge in case of XenServer.
 */
public interface TrafficLabel {

    public TrafficType getTrafficType();

    public String getNetworkLabel();
}
