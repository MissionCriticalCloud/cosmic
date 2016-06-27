package com.cloud.vpc.dao;

import com.cloud.network.Network.Service;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDaoImpl;
import com.cloud.utils.db.DB;

@DB()
public class MockNetworkOfferingServiceMapDaoImpl extends NetworkOfferingServiceMapDaoImpl {

    @Override
    public boolean areServicesSupportedByNetworkOffering(final long networkOfferingId, final Service... services) {
        if (services.length > 0 && services[0] == Service.SourceNat && networkOfferingId != 2) {
            return true;
        } else if (services.length > 0 && services[0] == Service.Lb && networkOfferingId == 6) {
            return true;
        }
        return false;
    }
}
