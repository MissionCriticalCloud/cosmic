package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.org.Grouping;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SequenceFetcher;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineDataCenterVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import javax.persistence.TableGenerator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @config {@table
 * || Param Name | Description | Values | Default ||
 * || mac.address.prefix | prefix to attach to all public and private mac addresses | number | 06 ||
 * }
 **/
@Component(value = "EngineDataCenterDao")
public class EngineDataCenterDaoImpl extends GenericDaoBase<EngineDataCenterVO, Long> implements EngineDataCenterDao {
    private static final Logger s_logger = LoggerFactory.getLogger(EngineDataCenterDaoImpl.class);

    protected SearchBuilder<EngineDataCenterVO> NameSearch;
    protected SearchBuilder<EngineDataCenterVO> ListZonesByDomainIdSearch;
    protected SearchBuilder<EngineDataCenterVO> PublicZonesSearch;
    protected SearchBuilder<EngineDataCenterVO> ChildZonesSearch;
    protected SearchBuilder<EngineDataCenterVO> DisabledZonesSearch;
    protected SearchBuilder<EngineDataCenterVO> TokenSearch;
    protected SearchBuilder<EngineDataCenterVO> StateChangeSearch;
    protected SearchBuilder<EngineDataCenterVO> UUIDSearch;

    protected long _prefix;
    protected Random _rand = new Random(System.currentTimeMillis());
    protected TableGenerator _tgMacAddress;

    @Inject
    protected DcDetailsDao _detailsDao;

    protected EngineDataCenterDaoImpl() {
        super();
        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        NameSearch.done();

        ListZonesByDomainIdSearch = createSearchBuilder();
        ListZonesByDomainIdSearch.and("domainId", ListZonesByDomainIdSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        ListZonesByDomainIdSearch.done();

        PublicZonesSearch = createSearchBuilder();
        PublicZonesSearch.and("domainId", PublicZonesSearch.entity().getDomainId(), SearchCriteria.Op.NULL);
        PublicZonesSearch.done();

        ChildZonesSearch = createSearchBuilder();
        ChildZonesSearch.and("domainid", ChildZonesSearch.entity().getDomainId(), SearchCriteria.Op.IN);
        ChildZonesSearch.done();

        DisabledZonesSearch = createSearchBuilder();
        DisabledZonesSearch.and("allocationState", DisabledZonesSearch.entity().getAllocationState(), SearchCriteria.Op.EQ);
        DisabledZonesSearch.done();

        TokenSearch = createSearchBuilder();
        TokenSearch.and("zoneToken", TokenSearch.entity().getZoneToken(), SearchCriteria.Op.EQ);
        TokenSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("state", StateChangeSearch.entity().getState(), SearchCriteria.Op.EQ);
        StateChangeSearch.done();

        UUIDSearch = createSearchBuilder();
        UUIDSearch.and("uuid", UUIDSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        UUIDSearch.done();

        _tgMacAddress = _tgs.get("macAddress");
        assert _tgMacAddress != null : "Couldn't get mac address table generator";
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        if (!super.configure(name, params)) {
            return false;
        }

        final String value = (String) params.get("mac.address.prefix");
        _prefix = (long) NumbersUtil.parseInt(value, 06) << 40;

        return true;
    }

    @Override
    public EngineDataCenterVO findByName(final String name) {
        final SearchCriteria<EngineDataCenterVO> sc = NameSearch.create();
        sc.setParameters("name", name);
        return findOneBy(sc);
    }

    @Override
    @DB
    public boolean update(final Long zoneId, final EngineDataCenterVO zone) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final boolean persisted = super.update(zoneId, zone);
        if (!persisted) {
            return persisted;
        }
        saveDetails(zone);
        txn.commit();
        return persisted;
    }

    @Override
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final EngineDataCenterVO zone = createForUpdate();
        zone.setName(null);

        update(id, zone);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }

    @Override
    public EngineDataCenterVO findByToken(final String zoneToken) {
        final SearchCriteria<EngineDataCenterVO> sc = TokenSearch.create();
        sc.setParameters("zoneToken", zoneToken);
        return findOneBy(sc);
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataCenterResourceEntity zoneEntity, final Object data) {

        final EngineDataCenterVO vo = findById(zoneEntity.getId());

        final Date oldUpdatedTime = vo.getLastUpdated();

        final SearchCriteria<EngineDataCenterVO> sc = StateChangeSearch.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "lastUpdated", new Date());

        final int rows = update(vo, sc);

        if (rows == 0 && s_logger.isDebugEnabled()) {
            final EngineDataCenterVO dbDC = findByIdIncludingRemoved(vo.getId());
            if (dbDC != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=").append(dbDC.getId()).append("; state=").append(dbDC.getState()).append(";updatedTime=").append(dbDC.getLastUpdated());
                str.append(": New Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatedTime=")
                   .append(vo.getLastUpdated());
                str.append(": stale Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update dataCenter: id=" + vo.getId() + ", as there is no such dataCenter exists in the database anymore");
            }
        }
        return rows > 0;
    }

    @Override
    public List<EngineDataCenterVO> findZonesByDomainId(final Long domainId) {
        final SearchCriteria<EngineDataCenterVO> sc = ListZonesByDomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        return listBy(sc);
    }

    @Override
    public List<EngineDataCenterVO> findZonesByDomainId(final Long domainId, final String keyword) {
        final SearchCriteria<EngineDataCenterVO> sc = ListZonesByDomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        if (keyword != null) {
            final SearchCriteria<EngineDataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        return listBy(sc);
    }

    @Override
    public List<EngineDataCenterVO> findChildZones(final Object[] ids, final String keyword) {
        final SearchCriteria<EngineDataCenterVO> sc = ChildZonesSearch.create();
        sc.setParameters("domainid", ids);
        if (keyword != null) {
            final SearchCriteria<EngineDataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        return listBy(sc);
    }

    @Override
    public List<EngineDataCenterVO> listPublicZones(final String keyword) {
        final SearchCriteria<EngineDataCenterVO> sc = PublicZonesSearch.create();
        if (keyword != null) {
            final SearchCriteria<EngineDataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        //sc.setParameters("domainId", domainId);
        return listBy(sc);
    }

    @Override
    public List<EngineDataCenterVO> findByKeyword(final String keyword) {
        final SearchCriteria<EngineDataCenterVO> ssc = createSearchCriteria();
        ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
        ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
        return listBy(ssc);
    }

    @Override
    public String[] getNextAvailableMacAddressPair(final long id) {
        return getNextAvailableMacAddressPair(id, 0);
    }

    @Override
    public String[] getNextAvailableMacAddressPair(final long id, final long mask) {
        final SequenceFetcher fetch = SequenceFetcher.getInstance();

        long seq = fetch.getNextSequence(Long.class, _tgMacAddress, id);
        seq = seq | _prefix | ((id & 0x7f) << 32);
        seq |= mask;
        seq |= ((_rand.nextInt(Short.MAX_VALUE) << 16) & 0x00000000ffff0000l);
        final String[] pair = new String[2];
        pair[0] = NetUtils.long2Mac(seq);
        pair[1] = NetUtils.long2Mac(seq | 0x1l << 39);
        return pair;
    }

    @Override
    public void loadDetails(final EngineDataCenterVO zone) {
        final Map<String, String> details = _detailsDao.findDetails(zone.getId());
        zone.setDetails(details);
    }

    @Override
    public void saveDetails(final EngineDataCenterVO zone) {
        final Map<String, String> details = zone.getDetails();
        if (details == null) {
            return;
        }
        _detailsDao.persist(zone.getId(), details);
    }

    @Override
    public List<EngineDataCenterVO> listDisabledZones() {
        final SearchCriteria<EngineDataCenterVO> sc = DisabledZonesSearch.create();
        sc.setParameters("allocationState", Grouping.AllocationState.Disabled);

        final List<EngineDataCenterVO> dcs = listBy(sc);

        return dcs;
    }

    @Override
    public List<EngineDataCenterVO> listEnabledZones() {
        final SearchCriteria<EngineDataCenterVO> sc = DisabledZonesSearch.create();
        sc.setParameters("allocationState", Grouping.AllocationState.Enabled);

        final List<EngineDataCenterVO> dcs = listBy(sc);

        return dcs;
    }

    @Override
    public EngineDataCenterVO findByTokenOrIdOrName(final String tokenOrIdOrName) {
        EngineDataCenterVO result = findByToken(tokenOrIdOrName);
        if (result == null) {
            result = findByName(tokenOrIdOrName);
            if (result == null) {
                try {
                    final Long dcId = Long.parseLong(tokenOrIdOrName);
                    return findById(dcId);
                } catch (final NumberFormatException nfe) {
                    s_logger.debug("Cannot parse " + tokenOrIdOrName + " into long. " + nfe);
                }
            }
        }
        return result;
    }
}
