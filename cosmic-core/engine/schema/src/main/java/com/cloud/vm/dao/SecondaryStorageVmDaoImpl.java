package com.cloud.vm.dao;

import com.cloud.legacymodel.storage.SecondaryStorageVmRole;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.vm.SecondaryStorageVmVO;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SecondaryStorageVmDaoImpl extends GenericDaoBase<SecondaryStorageVmVO, Long> implements SecondaryStorageVmDao {
    protected final Attribute _updateTimeAttr;
    protected SearchBuilder<SecondaryStorageVmVO> DataCenterStatusSearch;
    protected SearchBuilder<SecondaryStorageVmVO> StateSearch;
    protected SearchBuilder<SecondaryStorageVmVO> HostSearch;
    protected SearchBuilder<SecondaryStorageVmVO> LastHostSearch;
    protected SearchBuilder<SecondaryStorageVmVO> HostUpSearch;
    protected SearchBuilder<SecondaryStorageVmVO> ZoneSearch;
    protected SearchBuilder<SecondaryStorageVmVO> StateChangeSearch;
    protected SearchBuilder<SecondaryStorageVmVO> InstanceSearch;

    public SecondaryStorageVmDaoImpl() {
        this.DataCenterStatusSearch = createSearchBuilder();
        this.DataCenterStatusSearch.and("dc", this.DataCenterStatusSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.DataCenterStatusSearch.and("states", this.DataCenterStatusSearch.entity().getState(), SearchCriteria.Op.IN);
        this.DataCenterStatusSearch.and("role", this.DataCenterStatusSearch.entity().getRole(), SearchCriteria.Op.EQ);
        this.DataCenterStatusSearch.done();

        this.StateSearch = createSearchBuilder();
        this.StateSearch.and("states", this.StateSearch.entity().getState(), SearchCriteria.Op.IN);
        this.StateSearch.and("role", this.StateSearch.entity().getRole(), SearchCriteria.Op.EQ);
        this.StateSearch.done();

        this.HostSearch = createSearchBuilder();
        this.HostSearch.and("host", this.HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        this.HostSearch.and("role", this.HostSearch.entity().getRole(), SearchCriteria.Op.EQ);
        this.HostSearch.done();

        this.InstanceSearch = createSearchBuilder();
        this.InstanceSearch.and("instanceName", this.InstanceSearch.entity().getInstanceName(), SearchCriteria.Op.EQ);
        this.InstanceSearch.done();

        this.LastHostSearch = createSearchBuilder();
        this.LastHostSearch.and("lastHost", this.LastHostSearch.entity().getLastHostId(), SearchCriteria.Op.EQ);
        this.LastHostSearch.and("state", this.LastHostSearch.entity().getState(), SearchCriteria.Op.EQ);
        this.LastHostSearch.and("role", this.LastHostSearch.entity().getRole(), SearchCriteria.Op.EQ);
        this.LastHostSearch.done();

        this.HostUpSearch = createSearchBuilder();
        this.HostUpSearch.and("host", this.HostUpSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        this.HostUpSearch.and("states", this.HostUpSearch.entity().getState(), SearchCriteria.Op.NIN);
        this.HostUpSearch.and("role", this.HostUpSearch.entity().getRole(), SearchCriteria.Op.EQ);
        this.HostUpSearch.done();

        this.ZoneSearch = createSearchBuilder();
        this.ZoneSearch.and("zone", this.ZoneSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.ZoneSearch.and("role", this.ZoneSearch.entity().getRole(), SearchCriteria.Op.EQ);
        this.ZoneSearch.done();

        this.StateChangeSearch = createSearchBuilder();
        this.StateChangeSearch.and("id", this.StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        this.StateChangeSearch.and("states", this.StateChangeSearch.entity().getState(), SearchCriteria.Op.EQ);
        this.StateChangeSearch.and("host", this.StateChangeSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        this.StateChangeSearch.and("update", this.StateChangeSearch.entity().getUpdated(), SearchCriteria.Op.EQ);
        this.StateChangeSearch.and("role", this.StateChangeSearch.entity().getUpdated(), SearchCriteria.Op.EQ);
        this.StateChangeSearch.done();

        this._updateTimeAttr = this._allAttributes.get("updateTime");
        assert this._updateTimeAttr != null : "Couldn't get this updateTime attribute";
    }

    @Override
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SecondaryStorageVmVO proxy = createForUpdate();
        proxy.setPublicIpAddress(null);
        proxy.setPrivateIpAddress(null);

        final UpdateBuilder ub = getUpdateBuilder(proxy);
        ub.set(proxy, "state", State.Destroyed);
        ub.set(proxy, "privateIpAddress", null);
        update(id, ub, proxy);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }

    @Override
    public List<SecondaryStorageVmVO> getSecStorageVmListInStates(final SecondaryStorageVmRole role, final long dataCenterId, final State... states) {
        final SearchCriteria<SecondaryStorageVmVO> sc = this.DataCenterStatusSearch.create();
        sc.setParameters("states", (Object[]) states);
        sc.setParameters("dc", dataCenterId);
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> getSecStorageVmListInStates(final SecondaryStorageVmRole role, final State... states) {
        final SearchCriteria<SecondaryStorageVmVO> sc = this.StateSearch.create();
        sc.setParameters("states", (Object[]) states);
        if (role != null) {
            sc.setParameters("role", role);
        }

        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listByHostId(final SecondaryStorageVmRole role, final long hostId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = this.HostSearch.create();
        sc.setParameters("host", hostId);
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listByLastHostId(final SecondaryStorageVmRole role, final long hostId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = this.LastHostSearch.create();
        sc.setParameters("lastHost", hostId);
        sc.setParameters("state", State.Stopped);
        if (role != null) {
            sc.setParameters("role", role);
        }

        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listUpByHostId(final SecondaryStorageVmRole role, final long hostId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = this.HostUpSearch.create();
        sc.setParameters("host", hostId);
        sc.setParameters("states", new Object[]{State.Destroyed, State.Stopped, State.Expunging});
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listByZoneId(final SecondaryStorageVmRole role, final long zoneId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = this.ZoneSearch.create();
        sc.setParameters("zone", zoneId);
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public SecondaryStorageVmVO findByInstanceName(final String instanceName) {
        final SearchCriteria<SecondaryStorageVmVO> sc = this.InstanceSearch.create();
        sc.setParameters("instanceName", instanceName);
        final List<SecondaryStorageVmVO> list = listBy(sc);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
