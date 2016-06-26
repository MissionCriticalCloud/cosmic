// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.vpc.dao;

import com.cloud.network.Network;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offering.NetworkOffering.State;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.offerings.dao.NetworkOfferingDaoImpl;
import com.cloud.utils.db.DB;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DB()
public class MockNetworkOfferingDaoImpl extends NetworkOfferingDaoImpl implements NetworkOfferingDao {
    private static final Logger s_logger = LoggerFactory.getLogger(MockNetworkOfferingDaoImpl.class);

    /* (non-Javadoc)
     * @see com.cloud.offerings.dao.NetworkOfferingDao#findByUniqueName(java.lang.String)
     */
    @Override
    public NetworkOfferingVO findByUniqueName(final String uniqueName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.offerings.dao.NetworkOfferingDao#persistDefaultNetworkOffering(com.cloud.offerings.NetworkOfferingVO)
     */
    @Override
    public NetworkOfferingVO persistDefaultNetworkOffering(final NetworkOfferingVO offering) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.offerings.dao.NetworkOfferingDao#listSystemNetworkOfferings()
     */
    @Override
    public List<NetworkOfferingVO> listSystemNetworkOfferings() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.offerings.dao.NetworkOfferingDao#listByAvailability(com.cloud.offering.NetworkOffering.Availability, boolean)
     */
    @Override
    public List<NetworkOfferingVO> listByAvailability(final Availability availability, final boolean isSystem) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.offerings.dao.NetworkOfferingDao#getOfferingIdsToUpgradeFrom(com.cloud.offering.NetworkOffering)
     */
    @Override
    public List<Long> getOfferingIdsToUpgradeFrom(final NetworkOffering originalOffering) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.offerings.dao.NetworkOfferingDao#listByTrafficTypeGuestTypeAndState(com.cloud.offering.NetworkOffering.State, com.cloud.network.Networks.TrafficType, com
     * .cloud.network.Network.GuestType)
     */
    @Override
    public List<NetworkOfferingVO> listByTrafficTypeGuestTypeAndState(final State state, final TrafficType trafficType, final GuestType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NetworkOfferingVO findById(final Long id) {
        NetworkOfferingVO vo = null;
        if (id.longValue() == 1) {
            //network offering valid for vpc
            vo =
                    new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false, Availability.Optional, null, Network.GuestType.Isolated, false,
                            false, false, false, false);
        } else if (id.longValue() == 2) {
            //invalid offering - source nat is not included
            vo =
                    new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false, Availability.Optional, null, Network.GuestType.Isolated, false,
                            false, false, false, false);
        } else if (id.longValue() == 3) {
            //network offering invalid for vpc (conserve mode off)
            vo =
                    new NetworkOfferingVO("non vpc", "non vpc", TrafficType.Guest, false, true, null, null, false, Availability.Optional, null, Network.GuestType.Isolated,
                            true, false, false, false, false);
        } else if (id.longValue() == 4) {
            //network offering invalid for vpc (Shared)
            vo =
                    new NetworkOfferingVO("non vpc", "non vpc", TrafficType.Guest, false, true, null, null, false, Availability.Optional, null, Network.GuestType.Shared,
                            false, false, false, false, false);
        } else if (id.longValue() == 5) {
            //network offering invalid for vpc (has redundant router)
            vo =
                    new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false, Availability.Optional, null, Network.GuestType.Isolated, false,
                            false, false, false, false);
            vo.setRedundantRouter(true);
        } else if (id.longValue() == 6) {
            //network offering invalid for vpc (has lb service)
            vo =
                    new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false, Availability.Optional, null, Network.GuestType.Isolated, false,
                            false, false, false, false);
        }

        if (vo != null) {
            vo = setId(vo, id);
        }

        return vo;
    }

    private NetworkOfferingVO setId(final NetworkOfferingVO vo, final long id) {
        final NetworkOfferingVO voToReturn = vo;
        final Class<?> c = voToReturn.getClass();
        try {
            final Field f = c.getDeclaredField("id");
            f.setAccessible(true);
            f.setLong(voToReturn, id);
        } catch (final NoSuchFieldException ex) {
            s_logger.warn(ex.toString());
            return null;
        } catch (final IllegalAccessException ex) {
            s_logger.warn(ex.toString());
            return null;
        }

        return voToReturn;
    }
}
