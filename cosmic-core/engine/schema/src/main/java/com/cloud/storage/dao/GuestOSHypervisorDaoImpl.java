package com.cloud.storage.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class GuestOSHypervisorDaoImpl extends GenericDaoBase<GuestOSHypervisorVO, Long> implements GuestOSHypervisorDao {

    protected final SearchBuilder<GuestOSHypervisorVO> guestOsSearch;
    protected final SearchBuilder<GuestOSHypervisorVO> mappingSearch;
    protected final SearchBuilder<GuestOSHypervisorVO> userDefinedMappingSearch;

    protected GuestOSHypervisorDaoImpl() {
        guestOsSearch = createSearchBuilder();
        guestOsSearch.and("guest_os_id", guestOsSearch.entity().getGuestOsId(), SearchCriteria.Op.EQ);
        guestOsSearch.done();

        mappingSearch = createSearchBuilder();
        mappingSearch.and("guest_os_id", mappingSearch.entity().getGuestOsId(), SearchCriteria.Op.EQ);
        mappingSearch.and("hypervisor_type", mappingSearch.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        mappingSearch.and("hypervisor_version", mappingSearch.entity().getHypervisorVersion(), SearchCriteria.Op.EQ);
        mappingSearch.done();

        userDefinedMappingSearch = createSearchBuilder();
        userDefinedMappingSearch.and("guest_os_id", userDefinedMappingSearch.entity().getGuestOsId(), SearchCriteria.Op.EQ);
        userDefinedMappingSearch.and("hypervisor_type", userDefinedMappingSearch.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        userDefinedMappingSearch.and("hypervisor_version", userDefinedMappingSearch.entity().getHypervisorVersion(), SearchCriteria.Op.EQ);
        userDefinedMappingSearch.and("is_user_defined", userDefinedMappingSearch.entity().getIsUserDefined(), SearchCriteria.Op.EQ);
        userDefinedMappingSearch.done();
    }

    @Override
    public HypervisorType findHypervisorTypeByGuestOsId(final long guestOsId) {
        final SearchCriteria<GuestOSHypervisorVO> sc = guestOsSearch.create();
        sc.setParameters("guest_os_id", guestOsId);
        final GuestOSHypervisorVO goh = findOneBy(sc);
        return HypervisorType.getType(goh.getHypervisorType());
    }

    @Override
    public GuestOSHypervisorVO findByOsIdAndHypervisor(final long guestOsId, final String hypervisorType, final String hypervisorVersion) {
        final SearchCriteria<GuestOSHypervisorVO> sc = mappingSearch.create();
        String version = "default";
        if (!(hypervisorVersion == null || hypervisorVersion.isEmpty())) {
            version = hypervisorVersion;
        }
        sc.setParameters("guest_os_id", guestOsId);
        sc.setParameters("hypervisor_type", hypervisorType);
        sc.setParameters("hypervisor_version", version);
        return findOneBy(sc);
    }

    @Override
    public boolean removeGuestOsMapping(final Long id) {
        final GuestOSHypervisorVO guestOsHypervisor = findById(id);
        createForUpdate(id);
        guestOsHypervisor.setRemoved(new Date());
        update(id, guestOsHypervisor);
        return super.remove(id);
    }

    @Override
    public GuestOSHypervisorVO findByOsIdAndHypervisorAndUserDefined(final long guestOsId, final String hypervisorType, final String hypervisorVersion, final boolean
            isUserDefined) {
        final SearchCriteria<GuestOSHypervisorVO> sc = userDefinedMappingSearch.create();
        String version = "default";
        if (!(hypervisorVersion == null || hypervisorVersion.isEmpty())) {
            version = hypervisorVersion;
        }
        sc.setParameters("guest_os_id", guestOsId);
        sc.setParameters("hypervisor_type", hypervisorType);
        sc.setParameters("hypervisor_version", version);
        sc.setParameters("is_user_defined", isUserDefined);
        return findOneBy(sc);
    }
}
