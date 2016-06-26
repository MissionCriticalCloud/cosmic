package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.PrivateIpVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB()
public class PrivateIpDaoImpl extends GenericDaoBase<PrivateIpVO, Long> implements PrivateIpDao {
    private static final Logger s_logger = LoggerFactory.getLogger(PrivateIpDaoImpl.class);

    private final SearchBuilder<PrivateIpVO> AllFieldsSearch;
    private final GenericSearchBuilder<PrivateIpVO, Integer> CountAllocatedByNetworkId;
    private final GenericSearchBuilder<PrivateIpVO, Integer> CountByNetworkId;

    protected PrivateIpDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("ip", AllFieldsSearch.entity().getIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("ipAddress", AllFieldsSearch.entity().getIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("taken", AllFieldsSearch.entity().getTakenAt(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        CountAllocatedByNetworkId = createSearchBuilder(Integer.class);
        CountAllocatedByNetworkId.select(null, Func.COUNT, CountAllocatedByNetworkId.entity().getId());
        CountAllocatedByNetworkId.and("networkId", CountAllocatedByNetworkId.entity().getNetworkId(), Op.EQ);
        CountAllocatedByNetworkId.and("taken", CountAllocatedByNetworkId.entity().getTakenAt(), Op.NNULL);
        CountAllocatedByNetworkId.done();

        CountByNetworkId = createSearchBuilder(Integer.class);
        CountByNetworkId.select(null, Func.COUNT, CountByNetworkId.entity().getId());
        CountByNetworkId.and("networkId", CountByNetworkId.entity().getNetworkId(), Op.EQ);
        CountByNetworkId.done();
    }

    @Override
    public PrivateIpVO allocateIpAddress(final long dcId, final long networkId, final String requestedIp) {
        final SearchCriteria<PrivateIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("taken", (Date) null);

        if (requestedIp != null) {
            sc.setParameters("ipAddress", requestedIp);
        }

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final PrivateIpVO vo = lockOneRandomRow(sc, true);
        if (vo == null) {
            txn.rollback();
            return null;
        }
        vo.setTakenAt(new Date());
        update(vo.getId(), vo);
        txn.commit();
        return vo;
    }

    @Override
    public void releaseIpAddress(final String ipAddress, final long networkId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing private ip address: " + ipAddress + " network id " + networkId);
        }
        final SearchCriteria<PrivateIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", ipAddress);
        sc.setParameters("networkId", networkId);

        final PrivateIpVO vo = createForUpdate();

        vo.setTakenAt(null);
        update(vo, sc);
    }

    @Override
    public PrivateIpVO findByIpAndSourceNetworkId(final long networkId, final String ip4Address) {
        final SearchCriteria<PrivateIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", ip4Address);
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public List<PrivateIpVO> listByNetworkId(final long networkId) {
        final SearchCriteria<PrivateIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        return listBy(sc);
    }

    @Override
    public int countAllocatedByNetworkId(final long ntwkId) {
        final SearchCriteria<Integer> sc = CountAllocatedByNetworkId.create();
        sc.setParameters("networkId", ntwkId);
        final List<Integer> results = customSearch(sc, null);
        return results.get(0);
    }

    @Override
    public void deleteByNetworkId(final long networkId) {
        final SearchCriteria<PrivateIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        remove(sc);
    }

    @Override
    public int countByNetworkId(final long ntwkId) {
        final SearchCriteria<Integer> sc = CountByNetworkId.create();
        sc.setParameters("networkId", ntwkId);
        final List<Integer> results = customSearch(sc, null);
        return results.get(0);
    }

    @Override
    public PrivateIpVO findByIpAndVpcId(final long vpcId, final String ip4Address) {
        final SearchCriteria<PrivateIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", ip4Address);
        sc.setParameters("vpcId", vpcId);
        return findOneBy(sc);
    }

    @Override
    public PrivateIpVO findByIpAndSourceNetworkIdAndVpcId(final long networkId, final String ip4Address, final long vpcId) {
        final SearchCriteria<PrivateIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", ip4Address);
        sc.setParameters("networkId", networkId);
        sc.setParameters("vpcId", vpcId);
        return findOneBy(sc);
    }
}
