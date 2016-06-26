package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.vo.DomainJoinVO;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.domain.Domain;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.ResourceLimitAndCountResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DomainJoinDaoImpl extends GenericDaoBase<DomainJoinVO, Long> implements DomainJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(DomainJoinDaoImpl.class);

    private final SearchBuilder<DomainJoinVO> domainIdSearch;

    protected DomainJoinDaoImpl() {

        domainIdSearch = createSearchBuilder();
        domainIdSearch.and("id", domainIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        domainIdSearch.done();

        this._count = "select count(distinct id) from domain_view WHERE ";
    }

    @Override
    public DomainResponse newDomainResponse(final ResponseView view, final DomainJoinVO domain) {
        final DomainResponse domainResponse = new DomainResponse();
        domainResponse.setDomainName(domain.getName());
        domainResponse.setId(domain.getUuid());
        domainResponse.setLevel(domain.getLevel());
        domainResponse.setNetworkDomain(domain.getNetworkDomain());
        final Domain parentDomain = ApiDBUtils.findDomainById(domain.getParent());
        if (parentDomain != null) {
            domainResponse.setParentDomainId(parentDomain.getUuid());
        }
        final StringBuilder domainPath = new StringBuilder("ROOT");
        (domainPath.append(domain.getPath())).deleteCharAt(domainPath.length() - 1);
        domainResponse.setPath(domainPath.toString());
        if (domain.getParent() != null) {
            domainResponse.setParentDomainName(ApiDBUtils.findDomainById(domain.getParent()).getName());
        }
        if (domain.getChildCount() > 0) {
            domainResponse.setHasChild(true);
        }

        domainResponse.setState(domain.getState().toString());
        domainResponse.setNetworkDomain(domain.getNetworkDomain());

        final boolean fullView = (view == ResponseView.Full && domain.getId() == Domain.ROOT_DOMAIN);
        setResourceLimits(domain, fullView, domainResponse);

        //get resource limits for projects
        final long projectLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getProjectLimit(), fullView, ResourceType.project, domain.getId());
        final String projectLimitDisplay = (fullView || projectLimit == -1) ? "Unlimited" : String.valueOf(projectLimit);
        final long projectTotal = (domain.getProjectTotal() == null) ? 0 : domain.getProjectTotal();
        final String projectAvail = (fullView || projectLimit == -1) ? "Unlimited" : String.valueOf(projectLimit - projectTotal);
        domainResponse.setProjectLimit(projectLimitDisplay);
        domainResponse.setProjectTotal(projectTotal);
        domainResponse.setProjectAvailable(projectAvail);

        domainResponse.setObjectName("domain");

        return domainResponse;
    }

    @Override
    public DomainJoinVO newDomainView(final Domain domain) {
        final SearchCriteria<DomainJoinVO> sc = domainIdSearch.create();
        sc.setParameters("id", domain.getId());
        final List<DomainJoinVO> domains = searchIncludingRemoved(sc, null, null, false);
        assert domains != null && domains.size() == 1 : "No domain found for domain id " + domain.getId();
        return domains.get(0);
    }

    @Override
    public void setResourceLimits(final DomainJoinVO domain, final boolean fullView, final ResourceLimitAndCountResponse response) {
        // Get resource limits and counts
        final long vmLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getVmLimit(), fullView, ResourceType.user_vm, domain.getId());
        final String vmLimitDisplay = (fullView || vmLimit == -1) ? "Unlimited" : String.valueOf(vmLimit);
        final long vmTotal = (domain.getVmTotal() == null) ? 0 : domain.getVmTotal();
        final String vmAvail = (fullView || vmLimit == -1) ? "Unlimited" : String.valueOf(vmLimit - vmTotal);
        response.setVmLimit(vmLimitDisplay);
        response.setVmTotal(vmTotal);
        response.setVmAvailable(vmAvail);

        final long ipLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getIpLimit(), fullView, ResourceType.public_ip, domain.getId());
        final String ipLimitDisplay = (fullView || ipLimit == -1) ? "Unlimited" : String.valueOf(ipLimit);
        final long ipTotal = (domain.getIpTotal() == null) ? 0 : domain.getIpTotal();
        final String ipAvail = ((fullView || ipLimit == -1)) ? "Unlimited" : String.valueOf(ipLimit - ipTotal);
        response.setIpLimit(ipLimitDisplay);
        response.setIpTotal(ipTotal);
        response.setIpAvailable(ipAvail);

        final long volumeLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getVolumeLimit(), fullView, ResourceType.volume, domain.getId());
        final String volumeLimitDisplay = (fullView || volumeLimit == -1) ? "Unlimited" : String.valueOf(volumeLimit);
        final long volumeTotal = (domain.getVolumeTotal() == null) ? 0 : domain.getVolumeTotal();
        final String volumeAvail = (fullView || volumeLimit == -1) ? "Unlimited" : String.valueOf(volumeLimit - volumeTotal);
        response.setVolumeLimit(volumeLimitDisplay);
        response.setVolumeTotal(volumeTotal);
        response.setVolumeAvailable(volumeAvail);

        final long snapshotLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getSnapshotLimit(), fullView, ResourceType.snapshot, domain.getId());
        final String snapshotLimitDisplay = (fullView || snapshotLimit == -1) ? "Unlimited" : String.valueOf(snapshotLimit);
        final long snapshotTotal = (domain.getSnapshotTotal() == null) ? 0 : domain.getSnapshotTotal();
        final String snapshotAvail = (fullView || snapshotLimit == -1) ? "Unlimited" : String.valueOf(snapshotLimit - snapshotTotal);
        response.setSnapshotLimit(snapshotLimitDisplay);
        response.setSnapshotTotal(snapshotTotal);
        response.setSnapshotAvailable(snapshotAvail);

        final Long templateLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getTemplateLimit(), fullView, ResourceType.template, domain.getId());
        final String templateLimitDisplay = (fullView || templateLimit == -1) ? "Unlimited" : String.valueOf(templateLimit);
        final Long templateTotal = (domain.getTemplateTotal() == null) ? 0 : domain.getTemplateTotal();
        final String templateAvail = (fullView || templateLimit == -1) ? "Unlimited" : String.valueOf(templateLimit - templateTotal);
        response.setTemplateLimit(templateLimitDisplay);
        response.setTemplateTotal(templateTotal);
        response.setTemplateAvailable(templateAvail);

        //get resource limits for networks
        final long networkLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getNetworkLimit(), fullView, ResourceType.network, domain.getId());
        final String networkLimitDisplay = (fullView || networkLimit == -1) ? "Unlimited" : String.valueOf(networkLimit);
        final long networkTotal = (domain.getNetworkTotal() == null) ? 0 : domain.getNetworkTotal();
        final String networkAvail = (fullView || networkLimit == -1) ? "Unlimited" : String.valueOf(networkLimit - networkTotal);
        response.setNetworkLimit(networkLimitDisplay);
        response.setNetworkTotal(networkTotal);
        response.setNetworkAvailable(networkAvail);

        //get resource limits for vpcs
        final long vpcLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getVpcLimit(), fullView, ResourceType.vpc, domain.getId());
        final String vpcLimitDisplay = (fullView || vpcLimit == -1) ? "Unlimited" : String.valueOf(vpcLimit);
        final long vpcTotal = (domain.getVpcTotal() == null) ? 0 : domain.getVpcTotal();
        final String vpcAvail = (fullView || vpcLimit == -1) ? "Unlimited" : String.valueOf(vpcLimit - vpcTotal);
        response.setVpcLimit(vpcLimitDisplay);
        response.setVpcTotal(vpcTotal);
        response.setVpcAvailable(vpcAvail);

        //get resource limits for cpu cores
        final long cpuLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getCpuLimit(), fullView, ResourceType.cpu, domain.getId());
        final String cpuLimitDisplay = (fullView || cpuLimit == -1) ? "Unlimited" : String.valueOf(cpuLimit);
        final long cpuTotal = (domain.getCpuTotal() == null) ? 0 : domain.getCpuTotal();
        final String cpuAvail = (fullView || cpuLimit == -1) ? "Unlimited" : String.valueOf(cpuLimit - cpuTotal);
        response.setCpuLimit(cpuLimitDisplay);
        response.setCpuTotal(cpuTotal);
        response.setCpuAvailable(cpuAvail);

        //get resource limits for memory
        final long memoryLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getMemoryLimit(), fullView, ResourceType.memory, domain.getId());
        final String memoryLimitDisplay = (fullView || memoryLimit == -1) ? "Unlimited" : String.valueOf(memoryLimit);
        final long memoryTotal = (domain.getMemoryTotal() == null) ? 0 : domain.getMemoryTotal();
        final String memoryAvail = (fullView || memoryLimit == -1) ? "Unlimited" : String.valueOf(memoryLimit - memoryTotal);
        response.setMemoryLimit(memoryLimitDisplay);
        response.setMemoryTotal(memoryTotal);
        response.setMemoryAvailable(memoryAvail);

        //get resource limits for primary storage space and convert it from Bytes to GiB
        final long primaryStorageLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getPrimaryStorageLimit(), fullView, ResourceType.primary_storage, domain.getId());
        final String primaryStorageLimitDisplay = (fullView || primaryStorageLimit == -1) ? "Unlimited" : String.valueOf(primaryStorageLimit / ResourceType.bytesToGiB);
        final long primaryStorageTotal = (domain.getPrimaryStorageTotal() == null) ? 0 : (domain.getPrimaryStorageTotal() / ResourceType.bytesToGiB);
        final String primaryStorageAvail = (fullView || primaryStorageLimit == -1) ? "Unlimited" : String.valueOf((primaryStorageLimit / ResourceType.bytesToGiB) -
                primaryStorageTotal);
        response.setPrimaryStorageLimit(primaryStorageLimitDisplay);
        response.setPrimaryStorageTotal(primaryStorageTotal);
        response.setPrimaryStorageAvailable(primaryStorageAvail);

        //get resource limits for secondary storage space and convert it from Bytes to GiB
        final long secondaryStorageLimit = ApiDBUtils.findCorrectResourceLimitForDomain(domain.getSecondaryStorageLimit(), fullView, ResourceType.secondary_storage, domain.getId
                ());
        final String secondaryStorageLimitDisplay = (fullView || secondaryStorageLimit == -1) ? "Unlimited" : String.valueOf(secondaryStorageLimit / ResourceType.bytesToGiB);
        final long secondaryStorageTotal = (domain.getSecondaryStorageTotal() == null) ? 0 : (domain.getSecondaryStorageTotal() / ResourceType.bytesToGiB);
        final String secondaryStorageAvail = (fullView || secondaryStorageLimit == -1) ? "Unlimited" : String.valueOf((secondaryStorageLimit / ResourceType.bytesToGiB) -
                secondaryStorageTotal);
        response.setSecondaryStorageLimit(secondaryStorageLimitDisplay);
        response.setSecondaryStorageTotal(secondaryStorageTotal);
        response.setSecondaryStorageAvailable(secondaryStorageAvail);
    }
}
