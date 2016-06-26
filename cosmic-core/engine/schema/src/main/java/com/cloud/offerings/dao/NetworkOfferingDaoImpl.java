package com.cloud.offerings.dao;

import com.cloud.network.Network;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.offerings.NetworkOfferingDetailsVO;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkOfferingDaoImpl extends GenericDaoBase<NetworkOfferingVO, Long> implements NetworkOfferingDao {
    final SearchBuilder<NetworkOfferingVO> NameSearch;
    final SearchBuilder<NetworkOfferingVO> SystemOfferingSearch;
    final SearchBuilder<NetworkOfferingVO> AvailabilitySearch;
    final SearchBuilder<NetworkOfferingVO> AllFieldsSearch;
    private final GenericSearchBuilder<NetworkOfferingVO, Long> UpgradeSearch;
    @Inject
    NetworkOfferingDetailsDao _detailsDao;

    protected NetworkOfferingDaoImpl() {
        super();

        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        NameSearch.and("uniqueName", NameSearch.entity().getUniqueName(), SearchCriteria.Op.EQ);
        NameSearch.done();

        SystemOfferingSearch = createSearchBuilder();
        SystemOfferingSearch.and("system", SystemOfferingSearch.entity().isSystemOnly(), SearchCriteria.Op.EQ);
        SystemOfferingSearch.done();

        AvailabilitySearch = createSearchBuilder();
        AvailabilitySearch.and("availability", AvailabilitySearch.entity().getAvailability(), SearchCriteria.Op.EQ);
        AvailabilitySearch.and("isSystem", AvailabilitySearch.entity().isSystemOnly(), SearchCriteria.Op.EQ);
        AvailabilitySearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("trafficType", AllFieldsSearch.entity().getTrafficType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("guestType", AllFieldsSearch.entity().getGuestType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("isSystem", AllFieldsSearch.entity().isSystemOnly(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        UpgradeSearch = createSearchBuilder(Long.class);
        UpgradeSearch.selectFields(UpgradeSearch.entity().getId());
        UpgradeSearch.and("physicalNetworkId", UpgradeSearch.entity().getId(), Op.NEQ);
        UpgradeSearch.and("physicalNetworkId", UpgradeSearch.entity().isSystemOnly(), Op.EQ);
        UpgradeSearch.and("trafficType", UpgradeSearch.entity().getTrafficType(), Op.EQ);
        UpgradeSearch.and("guestType", UpgradeSearch.entity().getGuestType(), Op.EQ);
        UpgradeSearch.and("state", UpgradeSearch.entity().getState(), Op.EQ);
        UpgradeSearch.done();
    }

    @Override
    @DB
    public boolean remove(final Long networkOfferingId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final NetworkOfferingVO offering = findById(networkOfferingId);
        offering.setUniqueName(null);
        update(networkOfferingId, offering);
        final boolean result = super.remove(networkOfferingId);
        txn.commit();
        return result;
    }

    @Override
    public NetworkOfferingVO findByUniqueName(final String uniqueName) {
        final SearchCriteria<NetworkOfferingVO> sc = NameSearch.create();

        sc.setParameters("uniqueName", uniqueName);

        return findOneBy(sc);
    }

    @Override
    public NetworkOfferingVO persistDefaultNetworkOffering(final NetworkOfferingVO offering) {
        assert offering.getUniqueName() != null : "how are you going to find this later if you don't set it?";
        NetworkOfferingVO vo = findByUniqueName(offering.getUniqueName());
        if (vo != null) {
            return vo;
        }
        try {
            vo = persist(offering);
            return vo;
        } catch (final EntityExistsException e) {
            // Assume it's conflict on unique name from two different management servers.
            return findByUniqueName(offering.getName());
        }
    }

    @Override
    public List<NetworkOfferingVO> listSystemNetworkOfferings() {
        final SearchCriteria<NetworkOfferingVO> sc = SystemOfferingSearch.create();
        sc.setParameters("system", true);
        return this.listIncludingRemovedBy(sc, null);
    }

    @Override
    public List<NetworkOfferingVO> listByAvailability(final Availability availability, final boolean isSystem) {
        final SearchCriteria<NetworkOfferingVO> sc = AvailabilitySearch.create();
        sc.setParameters("availability", availability);
        sc.setParameters("isSystem", isSystem);
        return listBy(sc, null);
    }

    @Override
    public List<Long> getOfferingIdsToUpgradeFrom(final NetworkOffering originalOffering) {
        final SearchCriteria<Long> sc = UpgradeSearch.create();
        // exclude original offering
        sc.addAnd("id", SearchCriteria.Op.NEQ, originalOffering.getId());

        // list only non-system offerings
        sc.addAnd("systemOnly", SearchCriteria.Op.EQ, false);

        // Type of the network should be the same
        sc.addAnd("guestType", SearchCriteria.Op.EQ, originalOffering.getGuestType());

        // Traffic types should be the same
        sc.addAnd("trafficType", SearchCriteria.Op.EQ, originalOffering.getTrafficType());

        sc.addAnd("state", SearchCriteria.Op.EQ, NetworkOffering.State.Enabled);

        //specify Vlan should be the same
        sc.addAnd("specifyVlan", SearchCriteria.Op.EQ, originalOffering.getSpecifyVlan());

        return customSearch(sc, null);
    }

    @Override
    public List<NetworkOfferingVO> listByTrafficTypeGuestTypeAndState(final NetworkOffering.State state, final TrafficType trafficType, final Network.GuestType type) {
        final SearchCriteria<NetworkOfferingVO> sc = AllFieldsSearch.create();
        sc.setParameters("trafficType", trafficType);
        sc.setParameters("guestType", type);
        sc.setParameters("state", state);
        return listBy(sc, null);
    }

    @Override
    @DB
    public NetworkOfferingVO persist(final NetworkOfferingVO off, final Map<Detail, String> details) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        //1) persist the offering
        final NetworkOfferingVO vo = super.persist(off);

        //2) persist the details
        if (details != null && !details.isEmpty()) {
            for (final NetworkOffering.Detail detail : details.keySet()) {
                _detailsDao.persist(new NetworkOfferingDetailsVO(off.getId(), detail, details.get(detail)));
            }
        }

        txn.commit();
        return vo;
    }
}
