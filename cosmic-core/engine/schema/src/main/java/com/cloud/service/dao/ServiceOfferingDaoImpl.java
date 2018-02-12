package com.cloud.service.dao;

import com.cloud.service.ServiceOfferingDetailsVO;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB()
public class ServiceOfferingDaoImpl extends GenericDaoBase<ServiceOfferingVO, Long> implements ServiceOfferingDao {
    protected static final Logger s_logger = LoggerFactory.getLogger(ServiceOfferingDaoImpl.class);
    protected final SearchBuilder<ServiceOfferingVO> UniqueNameSearch;
    protected final SearchBuilder<ServiceOfferingVO> ServiceOfferingsByDomainIdSearch;
    protected final SearchBuilder<ServiceOfferingVO> SystemServiceOffering;
    protected final SearchBuilder<ServiceOfferingVO> ServiceOfferingsByKeywordSearch;
    protected final SearchBuilder<ServiceOfferingVO> PublicServiceOfferingSearch;
    @Inject
    protected ServiceOfferingDetailsDao detailsDao;

    public ServiceOfferingDaoImpl() {
        super();

        UniqueNameSearch = createSearchBuilder();
        UniqueNameSearch.and("name", UniqueNameSearch.entity().getUniqueName(), SearchCriteria.Op.EQ);
        UniqueNameSearch.and("system", UniqueNameSearch.entity().getSystemUse(), SearchCriteria.Op.EQ);
        UniqueNameSearch.done();

        ServiceOfferingsByDomainIdSearch = createSearchBuilder();
        ServiceOfferingsByDomainIdSearch.and("domainId", ServiceOfferingsByDomainIdSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        ServiceOfferingsByDomainIdSearch.done();

        SystemServiceOffering = createSearchBuilder();
        SystemServiceOffering.and("domainId", SystemServiceOffering.entity().getDomainId(), SearchCriteria.Op.EQ);
        SystemServiceOffering.and("system", SystemServiceOffering.entity().getSystemUse(), SearchCriteria.Op.EQ);
        SystemServiceOffering.and("removed", SystemServiceOffering.entity().getRemoved(), SearchCriteria.Op.NULL);
        SystemServiceOffering.done();

        PublicServiceOfferingSearch = createSearchBuilder();
        PublicServiceOfferingSearch.and("domainId", PublicServiceOfferingSearch.entity().getDomainId(), SearchCriteria.Op.NULL);
        PublicServiceOfferingSearch.and("system", PublicServiceOfferingSearch.entity().getSystemUse(), SearchCriteria.Op.EQ);
        PublicServiceOfferingSearch.and("removed", PublicServiceOfferingSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        PublicServiceOfferingSearch.done();

        ServiceOfferingsByKeywordSearch = createSearchBuilder();
        ServiceOfferingsByKeywordSearch.or("name", ServiceOfferingsByKeywordSearch.entity().getName(), SearchCriteria.Op.EQ);
        ServiceOfferingsByKeywordSearch.or("displayText", ServiceOfferingsByKeywordSearch.entity().getDisplayText(), SearchCriteria.Op.EQ);
        ServiceOfferingsByKeywordSearch.done();
    }

    @Override
    public boolean remove(final Long id) {
        final ServiceOfferingVO offering = createForUpdate();
        offering.setRemoved(new Date());

        return update(id, offering);
    }

    @Override
    public ServiceOfferingVO findByName(final String name) {
        final SearchCriteria<ServiceOfferingVO> sc = UniqueNameSearch.create();
        sc.setParameters("name", name);
        sc.setParameters("system", true);
        final List<ServiceOfferingVO> vos = search(sc, null, null, false);
        if (vos.size() == 0) {
            return null;
        }

        return vos.get(0);
    }

    @Override
    @DB
    public ServiceOfferingVO persistSystemServiceOffering(final ServiceOfferingVO offering) {
        assert offering.getUniqueName() != null : "how are you going to find this later if you don't set it?";
        final ServiceOfferingVO vo = findByName(offering.getUniqueName());
        if (vo != null) {
            if (!vo.getUniqueName().endsWith("-Local")) {
                if (vo.getUseLocalStorage()) {
                    vo.setUniqueName(vo.getUniqueName() + "-Local");
                    vo.setName(vo.getName() + " - Local Storage");
                    update(vo.getId(), vo);
                }
            }
            return vo;
        }
        try {
            return persist(offering);
        } catch (final EntityExistsException e) {
            // Assume it's conflict on unique name
            return findByName(offering.getUniqueName());
        }
    }

    @Override
    public List<ServiceOfferingVO> findServiceOfferingByDomainId(final Long domainId) {
        final SearchCriteria<ServiceOfferingVO> sc = ServiceOfferingsByDomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        return listBy(sc);
    }

    @Override
    public List<ServiceOfferingVO> findSystemOffering(final Long domainId, final Boolean isSystem, final String vmType) {
        final SearchCriteria<ServiceOfferingVO> sc = SystemServiceOffering.create();
        sc.setParameters("domainId", domainId);
        sc.setParameters("system", isSystem);
        sc.setParameters("vm_type", vmType);
        return listBy(sc);
    }

    @Override
    public List<ServiceOfferingVO> findPublicServiceOfferings() {
        final SearchCriteria<ServiceOfferingVO> sc = PublicServiceOfferingSearch.create();
        sc.setParameters("system", false);
        return listBy(sc);
    }

    @Override
    @DB
    public ServiceOfferingVO persistDeafultServiceOffering(final ServiceOfferingVO offering) {
        assert offering.getUniqueName() != null : "unique name should be set for the service offering";
        final ServiceOfferingVO vo = findByName(offering.getUniqueName());
        if (vo != null) {
            return vo;
        }
        try {
            return persist(offering);
        } catch (final EntityExistsException e) {
            // Assume it's conflict on unique name
            return findByName(offering.getUniqueName());
        }
    }

    @Override
    public void loadDetails(final ServiceOfferingVO serviceOffering) {
        final Map<String, String> details = detailsDao.listDetailsKeyPairs(serviceOffering.getId());
        serviceOffering.setDetails(details);
    }

    @Override
    public void saveDetails(final ServiceOfferingVO serviceOffering) {
        final Map<String, String> details = serviceOffering.getDetails();
        if (details == null) {
            return;
        }

        final List<ServiceOfferingDetailsVO> resourceDetails = new ArrayList<>();
        for (final String key : details.keySet()) {
            resourceDetails.add(new ServiceOfferingDetailsVO(serviceOffering.getId(), key, details.get(key), true));
        }

        detailsDao.saveDetails(resourceDetails);
    }

    @Override
    public ServiceOfferingVO findById(final Long vmId, final long serviceOfferingId) {
        return super.findById(serviceOfferingId);
    }

    @Override
    public ServiceOfferingVO findByIdIncludingRemoved(final Long vmId, final long serviceOfferingId) {
        return super.findByIdIncludingRemoved(serviceOfferingId);
    }

    @Override
    public boolean isDynamic(final long serviceOfferingId) {
        final ServiceOfferingVO offering = super.findById(serviceOfferingId);
        return offering.getCpu() == null || offering.getRamSize() == null;
    }

    @Override
    public ServiceOfferingVO getcomputeOffering(final ServiceOfferingVO serviceOffering, final Map<String, String> customParameters) {
        return new ServiceOfferingVO(serviceOffering);
    }

    @Override
    public List<ServiceOfferingVO> createSystemServiceOfferings(final String name, final String uniqueName, final int cpuCount, final int ramSize,
                                                                final Integer rateMbps, final Integer multicastRateMbps, final boolean offerHA, final String displayText, final
                                                                ProvisioningType provisioningType,
                                                                final boolean recreatable, final String tags, final boolean systemUse, final VirtualMachine.Type vmType, final
                                                                boolean defaultUse) {
        final List<ServiceOfferingVO> list = new ArrayList<>();
        ServiceOfferingVO offering = new ServiceOfferingVO(name, cpuCount, ramSize, rateMbps, multicastRateMbps, offerHA, displayText,
                provisioningType, false, recreatable, tags, systemUse, vmType, defaultUse);
        offering.setUniqueName(uniqueName);
        offering = persistSystemServiceOffering(offering);
        if (offering != null) {
            list.add(offering);
        }

        boolean useLocal = true;
        if (offering.getUseLocalStorage()) { // if 1st one is already local then 2nd needs to be shared
            useLocal = false;
        }

        offering = new ServiceOfferingVO(name + (useLocal ? " - Local Storage" : ""), cpuCount, ramSize, rateMbps, multicastRateMbps, offerHA, displayText,
                provisioningType, useLocal, recreatable, tags, systemUse, vmType, defaultUse);
        offering.setUniqueName(uniqueName + (useLocal ? "-Local" : ""));
        offering = persistSystemServiceOffering(offering);
        if (offering != null) {
            list.add(offering);
        }

        return list;
    }

    @Override
    public ServiceOfferingVO findDefaultSystemOffering(final String offeringName, final Boolean useLocalStorage) {
        String name = offeringName;
        if (useLocalStorage != null && useLocalStorage.booleanValue()) {
            name += "-Local";
        }
        final ServiceOfferingVO serviceOffering = findByName(name);
        if (serviceOffering == null) {
            final String message = "System service offering " + name + " not found";
            s_logger.error(message);
            throw new CloudRuntimeException(message);
        }
        return serviceOffering;
    }
}
