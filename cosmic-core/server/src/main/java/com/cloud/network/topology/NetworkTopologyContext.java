package com.cloud.network.topology;

import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;

import javax.inject.Inject;
import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Qualifier;

public class NetworkTopologyContext {

    private final Hashtable<NetworkType, NetworkTopology> _flyweight = new Hashtable<>();

    @Inject
    @Qualifier("basicNetworkTopology")
    private BasicNetworkTopology _basicNetworkTopology;

    @Inject
    @Qualifier("advancedNetworkTopology")
    private AdvancedNetworkTopology _advancedNetworkTopology;

    public void init() {
        _flyweight.put(NetworkType.Basic, _basicNetworkTopology);
        _flyweight.put(NetworkType.Advanced, _advancedNetworkTopology);
    }

    public NetworkTopology retrieveNetworkTopology(final DataCenter dc) {
        if (!_flyweight.containsKey(dc.getNetworkType())) {
            throw new IllegalArgumentException("The given type cannot be related to a NetworkTopology implementation. "
                    + "Please, give a correct type.");
        }
        return _flyweight.get(dc.getNetworkType());
    }

    /**
     * Method used for tests purpose only. Please do not use it to set the AdvanceNetworkTopology and it is managed by Spring.
     *
     * @param advancedNetworkTopology
     */
    public void setAdvancedNetworkTopology(final AdvancedNetworkTopology advancedNetworkTopology) {
        _advancedNetworkTopology = advancedNetworkTopology;
    }

    /**
     * Method used for tests purpose only. Please do not use it to set the BasicNetworkTopology and it is managed by Spring.
     *
     * @param basicNetworkTopology
     */
    public void setBasicNetworkTopology(final BasicNetworkTopology basicNetworkTopology) {
        _basicNetworkTopology = basicNetworkTopology;
    }
}
