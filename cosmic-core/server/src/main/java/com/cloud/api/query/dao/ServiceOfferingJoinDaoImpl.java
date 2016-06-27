package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.vo.ServiceOfferingJoinVO;
import com.cloud.offering.ServiceOffering;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceOfferingJoinDaoImpl extends GenericDaoBase<ServiceOfferingJoinVO, Long> implements ServiceOfferingJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(ServiceOfferingJoinDaoImpl.class);

    private final SearchBuilder<ServiceOfferingJoinVO> sofIdSearch;

    protected ServiceOfferingJoinDaoImpl() {

        sofIdSearch = createSearchBuilder();
        sofIdSearch.and("id", sofIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        sofIdSearch.done();

        this._count = "select count(distinct service_offering_view.id) from service_offering_view WHERE ";
    }

    @Override
    public ServiceOfferingResponse newServiceOfferingResponse(final ServiceOfferingJoinVO offering) {

        final ServiceOfferingResponse offeringResponse = new ServiceOfferingResponse();
        offeringResponse.setId(offering.getUuid());
        offeringResponse.setName(offering.getName());
        offeringResponse.setIsSystemOffering(offering.isSystemUse());
        offeringResponse.setDefaultUse(offering.isDefaultUse());
        offeringResponse.setSystemVmType(offering.getSystemVmType());
        offeringResponse.setDisplayText(offering.getDisplayText());
        offeringResponse.setProvisioningType(offering.getProvisioningType().toString());
        offeringResponse.setCpuNumber(offering.getCpu());
        offeringResponse.setCpuSpeed(offering.getSpeed());
        offeringResponse.setMemory(offering.getRamSize());
        offeringResponse.setCreated(offering.getCreated());
        offeringResponse.setStorageType(offering.isUseLocalStorage() ? ServiceOffering.StorageType.local.toString() : ServiceOffering.StorageType.shared.toString());
        offeringResponse.setOfferHa(offering.isOfferHA());
        offeringResponse.setLimitCpuUse(offering.isLimitCpuUse());
        offeringResponse.setVolatileVm(offering.getVolatileVm());
        offeringResponse.setTags(offering.getTags());
        offeringResponse.setDomain(offering.getDomainName());
        offeringResponse.setDomainId(offering.getDomainUuid());
        offeringResponse.setNetworkRate(offering.getRateMbps());
        offeringResponse.setHostTag(offering.getHostTag());
        offeringResponse.setDeploymentPlanner(offering.getDeploymentPlanner());
        offeringResponse.setCustomizedIops(offering.isCustomizedIops());
        offeringResponse.setMinIops(offering.getMinIops());
        offeringResponse.setMaxIops(offering.getMaxIops());
        offeringResponse.setHypervisorSnapshotReserve(offering.getHypervisorSnapshotReserve());
        offeringResponse.setBytesReadRate(offering.getBytesReadRate());
        offeringResponse.setBytesWriteRate(offering.getBytesWriteRate());
        offeringResponse.setIopsReadRate(offering.getIopsReadRate());
        offeringResponse.setIopsWriteRate(offering.getIopsWriteRate());
        offeringResponse.setDetails(ApiDBUtils.getResourceDetails(offering.getId(), ResourceObjectType.ServiceOffering));
        offeringResponse.setObjectName("serviceoffering");
        offeringResponse.setIscutomized(offering.isDynamic());

        return offeringResponse;
    }

    @Override
    public ServiceOfferingJoinVO newServiceOfferingView(final ServiceOffering offering) {
        final SearchCriteria<ServiceOfferingJoinVO> sc = sofIdSearch.create();
        sc.setParameters("id", offering.getId());
        final List<ServiceOfferingJoinVO> offerings = searchIncludingRemoved(sc, null, null, false);
        assert offerings != null && offerings.size() == 1 : "No service offering found for offering id " + offering.getId();
        return offerings.get(0);
    }
}
