package com.cloud.offerings.dao;

import com.cloud.network.Network;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Map;

/**
 * NetworkOfferingDao deals with searches and operations done on the
 * network_offering table.
 */
public interface NetworkOfferingDao extends GenericDao<NetworkOfferingVO, Long> {
    /**
     * Returns the network offering that matches the name.
     *
     * @param uniqueName name
     * @return NetworkOfferingVO
     */
    NetworkOfferingVO findByUniqueName(String uniqueName);

    /**
     * If not, then it persists it into the database.
     *
     * @param offering network offering to persist if not in the database.
     * @return NetworkOfferingVO backed by a row in the database
     */
    NetworkOfferingVO persistDefaultNetworkOffering(NetworkOfferingVO offering);

    List<NetworkOfferingVO> listSystemNetworkOfferings();

    List<NetworkOfferingVO> listByAvailability(Availability availability, boolean isSystem);

    List<Long> getOfferingIdsToUpgradeFrom(NetworkOffering originalOffering);

    List<NetworkOfferingVO> listByTrafficTypeGuestTypeAndState(NetworkOffering.State state, TrafficType trafficType, Network.GuestType type);

    NetworkOfferingVO persist(NetworkOfferingVO off, Map<Detail, String> details);
}
