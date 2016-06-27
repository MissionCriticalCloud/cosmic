package com.cloud.offerings.dao;

import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.offerings.NetworkOfferingDetailsVO;
import com.cloud.utils.db.GenericDao;

import java.util.Map;

public interface NetworkOfferingDetailsDao extends GenericDao<NetworkOfferingDetailsVO, Long> {

    Map<NetworkOffering.Detail, String> getNtwkOffDetails(long offeringId);

    String getDetail(long offeringId, Detail detailName);
}
