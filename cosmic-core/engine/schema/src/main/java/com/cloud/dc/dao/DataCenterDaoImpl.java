package com.cloud.dc.dao;

import com.cloud.dc.DataCenterDetailVO;
import com.cloud.dc.DataCenterIpAddressVO;
import com.cloud.dc.DataCenterLinkLocalIpAddressVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.DataCenterVnetVO;
import com.cloud.dc.PodVlanVO;
import com.cloud.network.dao.AccountGuestVlanMapDao;
import com.cloud.network.dao.AccountGuestVlanMapVO;
import com.cloud.org.Grouping;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SequenceFetcher;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.net.NetUtils;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import javax.persistence.TableGenerator;
import java.util.ArrayList;
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
@Component
public class DataCenterDaoImpl extends GenericDaoBase<DataCenterVO, Long> implements DataCenterDao {
    private static final Logger s_logger = LoggerFactory.getLogger(DataCenterDaoImpl.class);

    protected SearchBuilder<DataCenterVO> NameSearch;
    protected SearchBuilder<DataCenterVO> ListZonesByDomainIdSearch;
    protected SearchBuilder<DataCenterVO> PublicZonesSearch;
    protected SearchBuilder<DataCenterVO> ChildZonesSearch;
    protected SearchBuilder<DataCenterVO> DisabledZonesSearch;
    protected SearchBuilder<DataCenterVO> TokenSearch;

    @Inject
    protected DataCenterIpAddressDao _ipAllocDao = null;
    @Inject
    protected DataCenterLinkLocalIpAddressDao _linkLocalIpAllocDao = null;
    @Inject
    protected DataCenterVnetDao _vnetAllocDao = null;
    @Inject
    protected PodVlanDao _podVlanAllocDao = null;
    @Inject
    protected DataCenterDetailsDao _detailsDao = null;
    @Inject
    protected AccountGuestVlanMapDao _accountGuestVlanMapDao = null;

    protected long _prefix;
    protected Random _rand = new Random(System.currentTimeMillis());
    protected TableGenerator _tgMacAddress;

    public DataCenterDaoImpl() {
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

        if (!_ipAllocDao.configure("Ip Alloc", params)) {
            return false;
        }

        if (!_vnetAllocDao.configure("vnet Alloc", params)) {
            return false;
        }
        return true;
    }

    @Override
    public DataCenterVO findByName(final String name) {
        final SearchCriteria<DataCenterVO> sc = NameSearch.create();
        sc.setParameters("name", name);
        return findOneBy(sc);
    }

    @Override
    @DB
    public boolean update(final Long zoneId, final DataCenterVO zone) {
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
    public DataCenterVO findByToken(final String zoneToken) {
        final SearchCriteria<DataCenterVO> sc = TokenSearch.create();
        sc.setParameters("zoneToken", zoneToken);
        return findOneBy(sc);
    }

    @Override
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final DataCenterVO zone = createForUpdate();
        zone.setName(null);

        update(id, zone);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }

    @Override
    public List<DataCenterVO> findZonesByDomainId(final Long domainId) {
        final SearchCriteria<DataCenterVO> sc = ListZonesByDomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        return listBy(sc);
    }

    @Override
    public List<DataCenterVO> findZonesByDomainId(final Long domainId, final String keyword) {
        final SearchCriteria<DataCenterVO> sc = ListZonesByDomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        if (keyword != null) {
            final SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        return listBy(sc);
    }

    @Override
    public List<DataCenterVO> findChildZones(final Object[] ids, final String keyword) {
        final SearchCriteria<DataCenterVO> sc = ChildZonesSearch.create();
        sc.setParameters("domainid", ids);
        if (keyword != null) {
            final SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        return listBy(sc);
    }

    @Override
    public List<DataCenterVO> listPublicZones(final String keyword) {
        final SearchCriteria<DataCenterVO> sc = PublicZonesSearch.create();
        if (keyword != null) {
            final SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        //sc.setParameters("domainId", domainId);
        return listBy(sc);
    }

    @Override
    public List<DataCenterVO> findByKeyword(final String keyword) {
        final SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
        ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
        ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
        return listBy(ssc);
    }

    @Override
    public void releaseVnet(final String vnet, final long dcId, final long physicalNetworkId, final long accountId, final String reservationId) {
        _vnetAllocDao.release(vnet, physicalNetworkId, accountId, reservationId);
    }

    @Override
    public List<DataCenterVnetVO> findVnet(final long dcId, final long physicalNetworkId, final String vnet) {
        return _vnetAllocDao.findVnet(dcId, physicalNetworkId, vnet);
    }

    @Override
    public int countZoneVlans(final long dcId, final boolean onlyCountAllocated) {
        return _vnetAllocDao.countZoneVlans(dcId, onlyCountAllocated);
    }

    @Override
    public void releasePrivateIpAddress(final String ipAddress, final long dcId, final Long instanceId) {
        _ipAllocDao.releaseIpAddress(ipAddress, dcId, instanceId);
    }

    @Override
    public void releasePrivateIpAddress(final long nicId, final String reservationId) {
        _ipAllocDao.releaseIpAddress(nicId, reservationId);
    }

    @Override
    public void releaseLinkLocalIpAddress(final long nicId, final String reservationId) {
        _linkLocalIpAllocDao.releaseIpAddress(nicId, reservationId);
    }

    @Override
    public void releaseLinkLocalIpAddress(final String ipAddress, final long dcId, final Long instanceId) {
        _linkLocalIpAllocDao.releaseIpAddress(ipAddress, dcId, instanceId);
    }

    @Override
    public boolean deletePrivateIpAddressByPod(final long podId) {
        return _ipAllocDao.deleteIpAddressByPod(podId);
    }

    @Override
    public boolean deleteLinkLocalIpAddressByPod(final long podId) {
        return _linkLocalIpAllocDao.deleteIpAddressByPod(podId);
    }

    @Override
    public String allocateVnet(final long dataCenterId, final long physicalNetworkId, final long accountId, final String reservationId, final boolean canUseSystemGuestVlans) {
        final ArrayList<Long> dedicatedVlanDbIds = new ArrayList<>();
        boolean useDedicatedGuestVlans = false;
        final List<AccountGuestVlanMapVO> maps = _accountGuestVlanMapDao.listAccountGuestVlanMapsByAccount(accountId);
        for (final AccountGuestVlanMapVO map : maps) {
            dedicatedVlanDbIds.add(map.getId());
        }
        if (dedicatedVlanDbIds != null && !dedicatedVlanDbIds.isEmpty()) {
            useDedicatedGuestVlans = true;
            final DataCenterVnetVO vo = _vnetAllocDao.take(physicalNetworkId, accountId, reservationId, dedicatedVlanDbIds);
            if (vo != null) {
                return vo.getVnet();
            }
        }
        if (!useDedicatedGuestVlans || (useDedicatedGuestVlans && canUseSystemGuestVlans)) {
            final DataCenterVnetVO vo = _vnetAllocDao.take(physicalNetworkId, accountId, reservationId, null);
            if (vo != null) {
                return vo.getVnet();
            }
        }
        return null;
    }

    @Override
    public String allocatePodVlan(final long podId, final long accountId) {
        final PodVlanVO vo = _podVlanAllocDao.take(podId, accountId);
        if (vo == null) {
            return null;
        }
        return vo.getVlan();
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
    public Pair<String, Long> allocatePrivateIpAddress(final long dcId, final long podId, final long instanceId, final String reservationId) {
        _ipAllocDao.releaseIpAddress(instanceId);
        final DataCenterIpAddressVO vo = _ipAllocDao.takeIpAddress(dcId, podId, instanceId, reservationId);
        if (vo == null) {
            return null;
        }
        return new Pair<>(vo.getIpAddress(), vo.getMacAddress());
    }

    @Override
    public DataCenterIpAddressVO allocatePrivateIpAddress(final long dcId, final String reservationId) {
        final DataCenterIpAddressVO vo = _ipAllocDao.takeDataCenterIpAddress(dcId, reservationId);
        return vo;
    }

    @Override
    public String allocateLinkLocalIpAddress(final long dcId, final long podId, final long instanceId, final String reservationId) {
        final DataCenterLinkLocalIpAddressVO vo = _linkLocalIpAllocDao.takeIpAddress(dcId, podId, instanceId, reservationId);
        if (vo == null) {
            return null;
        }
        return vo.getIpAddress();
    }

    @Override
    public void addVnet(final long dcId, final long physicalNetworkId, final List<String> vnets) {
        _vnetAllocDao.add(dcId, physicalNetworkId, vnets);
    }

    @Override
    public void deleteVnet(final long physicalNetworkId) {
        _vnetAllocDao.delete(physicalNetworkId);
    }

    @Override
    public List<DataCenterVnetVO> listAllocatedVnets(final long physicalNetworkId) {
        return _vnetAllocDao.listAllocatedVnets(physicalNetworkId);
    }

    @Override
    public void addPrivateIpAddress(final long dcId, final long podId, final String start, final String end) {
        _ipAllocDao.addIpRange(dcId, podId, start, end);
    }

    @Override
    public void addLinkLocalIpAddress(final long dcId, final long podId, final String start, final String end) {
        _linkLocalIpAllocDao.addIpRange(dcId, podId, start, end);
    }

    @Override
    public void loadDetails(final DataCenterVO zone) {
        final Map<String, String> details = _detailsDao.listDetailsKeyPairs(zone.getId());
        zone.setDetails(details);
    }

    @Override
    public void saveDetails(final DataCenterVO zone) {
        final Map<String, String> details = zone.getDetails();
        if (details == null) {
            return;
        }

        final List<DataCenterDetailVO> resourceDetails = new ArrayList<>();
        for (final String key : details.keySet()) {
            resourceDetails.add(new DataCenterDetailVO(zone.getId(), key, details.get(key), true));
        }

        _detailsDao.saveDetails(resourceDetails);
    }

    @Override
    public List<DataCenterVO> listDisabledZones() {
        final SearchCriteria<DataCenterVO> sc = DisabledZonesSearch.create();
        sc.setParameters("allocationState", Grouping.AllocationState.Disabled);

        final List<DataCenterVO> dcs = listBy(sc);

        return dcs;
    }

    @Override
    public List<DataCenterVO> listEnabledZones() {
        final SearchCriteria<DataCenterVO> sc = DisabledZonesSearch.create();
        sc.setParameters("allocationState", Grouping.AllocationState.Enabled);

        final List<DataCenterVO> dcs = listBy(sc);

        return dcs;
    }

    @Override
    public DataCenterVO findByTokenOrIdOrName(final String tokenOrIdOrName) {
        DataCenterVO result = findByToken(tokenOrIdOrName);
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

    @Override
    public List<DataCenterVO> listAllZones() {
        final SearchCriteria<DataCenterVO> sc = NameSearch.create();
        final List<DataCenterVO> dcs = listBy(sc);

        return dcs;
    }
}
