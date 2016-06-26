package com.cloud.storage.dao;

import com.cloud.offering.DiskOffering.Type;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import javax.persistence.EntityExistsException;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DiskOfferingDaoImpl extends GenericDaoBase<DiskOfferingVO, Long> implements DiskOfferingDao {
    protected final SearchBuilder<DiskOfferingVO> UniqueNameSearch;
    private final SearchBuilder<DiskOfferingVO> DomainIdSearch;
    private final SearchBuilder<DiskOfferingVO> PrivateDiskOfferingSearch;
    private final SearchBuilder<DiskOfferingVO> PublicDiskOfferingSearch;
    private final Attribute _typeAttr;

    protected DiskOfferingDaoImpl() {
        DomainIdSearch = createSearchBuilder();
        DomainIdSearch.and("domainId", DomainIdSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        DomainIdSearch.and("removed", DomainIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        DomainIdSearch.done();

        PrivateDiskOfferingSearch = createSearchBuilder();
        PrivateDiskOfferingSearch.and("diskSize", PrivateDiskOfferingSearch.entity().getDiskSize(), SearchCriteria.Op.EQ);
        PrivateDiskOfferingSearch.done();

        PublicDiskOfferingSearch = createSearchBuilder();
        PublicDiskOfferingSearch.and("domainId", PublicDiskOfferingSearch.entity().getDomainId(), SearchCriteria.Op.NULL);
        PublicDiskOfferingSearch.and("system", PublicDiskOfferingSearch.entity().getSystemUse(), SearchCriteria.Op.EQ);
        PublicDiskOfferingSearch.and("removed", PublicDiskOfferingSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        PublicDiskOfferingSearch.done();

        UniqueNameSearch = createSearchBuilder();
        UniqueNameSearch.and("name", UniqueNameSearch.entity().getUniqueName(), SearchCriteria.Op.EQ);
        UniqueNameSearch.done();

        _typeAttr = _allAttributes.get("type");
    }

    @Override
    public List<DiskOfferingVO> listByDomainId(final long domainId) {
        final SearchCriteria<DiskOfferingVO> sc = DomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        // FIXME: this should not be exact match, but instead should find all
        // available disk offerings from parent domains
        return listBy(sc);
    }

    @Override
    public List<DiskOfferingVO> findPrivateDiskOffering() {
        final SearchCriteria<DiskOfferingVO> sc = PrivateDiskOfferingSearch.create();
        sc.setParameters("diskSize", 0);
        return listBy(sc);
    }

    @Override
    public List<DiskOfferingVO> findPublicDiskOfferings() {
        final SearchCriteria<DiskOfferingVO> sc = PublicDiskOfferingSearch.create();
        sc.setParameters("system", false);
        return listBy(sc);
    }

    @Override
    public DiskOfferingVO findByUniqueName(final String uniqueName) {
        final SearchCriteria<DiskOfferingVO> sc = UniqueNameSearch.create();
        sc.setParameters("name", uniqueName);
        final List<DiskOfferingVO> vos = search(sc, null, null, false);
        if (vos.size() == 0) {
            return null;
        }

        return vos.get(0);
    }

    @Override
    public DiskOfferingVO persistDeafultDiskOffering(final DiskOfferingVO offering) {
        assert offering.getUniqueName() != null : "unique name shouldn't be null for the disk offering";
        final DiskOfferingVO vo = findByUniqueName(offering.getUniqueName());
        if (vo != null) {
            return vo;
        }
        try {
            return persist(offering);
        } catch (final EntityExistsException e) {
            // Assume it's conflict on unique name
            return findByUniqueName(offering.getUniqueName());
        }
    }

    @Override
    public List<DiskOfferingVO> searchIncludingRemoved(final SearchCriteria<DiskOfferingVO> sc, final Filter filter, final Boolean lock, final boolean cache) {
        sc.addAnd(_typeAttr, Op.EQ, Type.Disk);
        return super.searchIncludingRemoved(sc, filter, lock, cache);
    }

    @Override
    public <K> List<K> customSearchIncludingRemoved(final SearchCriteria<K> sc, final Filter filter) {
        sc.addAnd(_typeAttr, Op.EQ, Type.Disk);
        return super.customSearchIncludingRemoved(sc, filter);
    }

    @Override
    protected List<DiskOfferingVO> executeList(final String sql, final Object... params) {
        final StringBuilder builder = new StringBuilder(sql);
        final int index = builder.indexOf("WHERE");
        if (index == -1) {
            builder.append(" WHERE type=?");
        } else {
            builder.insert(index + 6, "type=? ");
        }

        return super.executeList(sql, Type.Disk, params);
    }

    @Override
    public boolean remove(final Long id) {
        final DiskOfferingVO diskOffering = createForUpdate();
        diskOffering.setRemoved(new Date());

        return update(id, diskOffering);
    }
}
