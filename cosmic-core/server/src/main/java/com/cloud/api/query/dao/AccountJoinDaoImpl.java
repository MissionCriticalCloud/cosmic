package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.ViewResponseHelper;
import com.cloud.api.query.vo.AccountJoinVO;
import com.cloud.api.query.vo.UserAccountJoinVO;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.ResourceLimitAndCountResponse;
import org.apache.cloudstack.api.response.UserResponse;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountJoinDaoImpl extends GenericDaoBase<AccountJoinVO, Long> implements AccountJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(AccountJoinDaoImpl.class);

    private final SearchBuilder<AccountJoinVO> acctIdSearch;
    @Inject
    AccountManager _acctMgr;

    protected AccountJoinDaoImpl() {

        acctIdSearch = createSearchBuilder();
        acctIdSearch.and("id", acctIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        acctIdSearch.done();

        _count = "select count(distinct id) from account_view WHERE ";
    }

    @Override
    public AccountResponse newAccountResponse(final ResponseView view, final AccountJoinVO account) {
        final AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getUuid());
        accountResponse.setName(account.getAccountName());
        accountResponse.setAccountType(account.getType());
        accountResponse.setDomainId(account.getDomainUuid());
        accountResponse.setDomainName(account.getDomainName());
        accountResponse.setState(account.getState().toString());
        accountResponse.setNetworkDomain(account.getNetworkDomain());
        accountResponse.setDefaultZone(account.getDataCenterUuid());
        accountResponse.setIsDefault(account.isDefault());

        // get network stat
        accountResponse.setBytesReceived(account.getBytesReceived());
        accountResponse.setBytesSent(account.getBytesSent());

        final boolean fullView = (view == ResponseView.Full && _acctMgr.isRootAdmin(account.getId()));
        setResourceLimits(account, fullView, accountResponse);

        //get resource limits for projects
        final long projectLimit = ApiDBUtils.findCorrectResourceLimit(account.getProjectLimit(), account.getId(), ResourceType.project);
        final String projectLimitDisplay = (fullView || projectLimit == -1) ? "Unlimited" : String.valueOf(projectLimit);
        final long projectTotal = (account.getProjectTotal() == null) ? 0 : account.getProjectTotal();
        final String projectAvail = (fullView || projectLimit == -1) ? "Unlimited" : String.valueOf(projectLimit - projectTotal);
        accountResponse.setProjectLimit(projectLimitDisplay);
        accountResponse.setProjectTotal(projectTotal);
        accountResponse.setProjectAvailable(projectAvail);

        // set async job
        if (account.getJobId() != null) {
            accountResponse.setJobId(account.getJobUuid());
            accountResponse.setJobStatus(account.getJobStatus());
        }

        // adding all the users for an account as part of the response obj
        final List<UserAccountJoinVO> usersForAccount = ApiDBUtils.findUserViewByAccountId(account.getId());
        final List<UserResponse> userResponses = ViewResponseHelper.createUserResponse(usersForAccount.toArray(new UserAccountJoinVO[usersForAccount.size()]));
        accountResponse.setUsers(userResponses);

        // set details
        accountResponse.setDetails(ApiDBUtils.getAccountDetails(account.getId()));
        accountResponse.setObjectName("account");

        return accountResponse;
    }

    @Override
    public AccountJoinVO newAccountView(final Account acct) {
        final SearchCriteria<AccountJoinVO> sc = acctIdSearch.create();
        sc.setParameters("id", acct.getId());
        final List<AccountJoinVO> accounts = searchIncludingRemoved(sc, null, null, false);
        assert accounts != null && accounts.size() == 1 : "No account found for account id " + acct.getId();
        return accounts.get(0);
    }

    @Override
    public void setResourceLimits(final AccountJoinVO account, final boolean fullView, final ResourceLimitAndCountResponse response) {
        // Get resource limits and counts
        final long vmLimit = ApiDBUtils.findCorrectResourceLimit(account.getVmLimit(), account.getId(), ResourceType.user_vm);
        final String vmLimitDisplay = (fullView || vmLimit == -1) ? "Unlimited" : String.valueOf(vmLimit);
        final long vmTotal = (account.getVmTotal() == null) ? 0 : account.getVmTotal();
        final String vmAvail = (fullView || vmLimit == -1) ? "Unlimited" : String.valueOf(vmLimit - vmTotal);
        response.setVmLimit(vmLimitDisplay);
        response.setVmTotal(vmTotal);
        response.setVmAvailable(vmAvail);

        final long ipLimit = ApiDBUtils.findCorrectResourceLimit(account.getIpLimit(), account.getId(), ResourceType.public_ip);
        final String ipLimitDisplay = (fullView || ipLimit == -1) ? "Unlimited" : String.valueOf(ipLimit);
        final long ipTotal = (account.getIpTotal() == null) ? 0 : account.getIpTotal();

        Long ips = ipLimit - ipTotal;
        // check how many free ips are left, and if it's less than max allowed number of ips from account - use this
        // value
        final Long ipsLeft = account.getIpFree();
        boolean unlimited = true;
        if (ips.longValue() > ipsLeft.longValue()) {
            ips = ipsLeft;
            unlimited = false;
        }

        final String ipAvail = ((fullView || ipLimit == -1) && unlimited) ? "Unlimited" : String.valueOf(ips);

        response.setIpLimit(ipLimitDisplay);
        response.setIpTotal(ipTotal);
        response.setIpAvailable(ipAvail);

        final long volumeLimit = ApiDBUtils.findCorrectResourceLimit(account.getVolumeLimit(), account.getId(), ResourceType.volume);
        final String volumeLimitDisplay = (fullView || volumeLimit == -1) ? "Unlimited" : String.valueOf(volumeLimit);
        final long volumeTotal = (account.getVolumeTotal() == null) ? 0 : account.getVolumeTotal();
        final String volumeAvail = (fullView || volumeLimit == -1) ? "Unlimited" : String.valueOf(volumeLimit - volumeTotal);
        response.setVolumeLimit(volumeLimitDisplay);
        response.setVolumeTotal(volumeTotal);
        response.setVolumeAvailable(volumeAvail);

        final long snapshotLimit = ApiDBUtils.findCorrectResourceLimit(account.getSnapshotLimit(), account.getId(), ResourceType.snapshot);
        final String snapshotLimitDisplay = (fullView || snapshotLimit == -1) ? "Unlimited" : String.valueOf(snapshotLimit);
        final long snapshotTotal = (account.getSnapshotTotal() == null) ? 0 : account.getSnapshotTotal();
        final String snapshotAvail = (fullView || snapshotLimit == -1) ? "Unlimited" : String.valueOf(snapshotLimit - snapshotTotal);
        response.setSnapshotLimit(snapshotLimitDisplay);
        response.setSnapshotTotal(snapshotTotal);
        response.setSnapshotAvailable(snapshotAvail);

        final Long templateLimit = ApiDBUtils.findCorrectResourceLimit(account.getTemplateLimit(), account.getId(), ResourceType.template);
        final String templateLimitDisplay = (fullView || templateLimit == -1) ? "Unlimited" : String.valueOf(templateLimit);
        final Long templateTotal = (account.getTemplateTotal() == null) ? 0 : account.getTemplateTotal();
        final String templateAvail = (fullView || templateLimit == -1) ? "Unlimited" : String.valueOf(templateLimit - templateTotal);
        response.setTemplateLimit(templateLimitDisplay);
        response.setTemplateTotal(templateTotal);
        response.setTemplateAvailable(templateAvail);

        // Get stopped and running VMs
        response.setVmStopped(account.getVmStopped());
        response.setVmRunning(account.getVmRunning());

        //get resource limits for networks
        final long networkLimit = ApiDBUtils.findCorrectResourceLimit(account.getNetworkLimit(), account.getId(), ResourceType.network);
        final String networkLimitDisplay = (fullView || networkLimit == -1) ? "Unlimited" : String.valueOf(networkLimit);
        final long networkTotal = (account.getNetworkTotal() == null) ? 0 : account.getNetworkTotal();
        final String networkAvail = (fullView || networkLimit == -1) ? "Unlimited" : String.valueOf(networkLimit - networkTotal);
        response.setNetworkLimit(networkLimitDisplay);
        response.setNetworkTotal(networkTotal);
        response.setNetworkAvailable(networkAvail);

        //get resource limits for vpcs
        final long vpcLimit = ApiDBUtils.findCorrectResourceLimit(account.getVpcLimit(), account.getId(), ResourceType.vpc);
        final String vpcLimitDisplay = (fullView || vpcLimit == -1) ? "Unlimited" : String.valueOf(vpcLimit);
        final long vpcTotal = (account.getVpcTotal() == null) ? 0 : account.getVpcTotal();
        final String vpcAvail = (fullView || vpcLimit == -1) ? "Unlimited" : String.valueOf(vpcLimit - vpcTotal);
        response.setVpcLimit(vpcLimitDisplay);
        response.setVpcTotal(vpcTotal);
        response.setVpcAvailable(vpcAvail);

        //get resource limits for cpu cores
        final long cpuLimit = ApiDBUtils.findCorrectResourceLimit(account.getCpuLimit(), account.getId(), ResourceType.cpu);
        final String cpuLimitDisplay = (fullView || cpuLimit == -1) ? "Unlimited" : String.valueOf(cpuLimit);
        final long cpuTotal = (account.getCpuTotal() == null) ? 0 : account.getCpuTotal();
        final String cpuAvail = (fullView || cpuLimit == -1) ? "Unlimited" : String.valueOf(cpuLimit - cpuTotal);
        response.setCpuLimit(cpuLimitDisplay);
        response.setCpuTotal(cpuTotal);
        response.setCpuAvailable(cpuAvail);

        //get resource limits for memory
        final long memoryLimit = ApiDBUtils.findCorrectResourceLimit(account.getMemoryLimit(), account.getId(), ResourceType.memory);
        final String memoryLimitDisplay = (fullView || memoryLimit == -1) ? "Unlimited" : String.valueOf(memoryLimit);
        final long memoryTotal = (account.getMemoryTotal() == null) ? 0 : account.getMemoryTotal();
        final String memoryAvail = (fullView || memoryLimit == -1) ? "Unlimited" : String.valueOf(memoryLimit - memoryTotal);
        response.setMemoryLimit(memoryLimitDisplay);
        response.setMemoryTotal(memoryTotal);
        response.setMemoryAvailable(memoryAvail);

        //get resource limits for primary storage space and convert it from Bytes to GiB
        final long primaryStorageLimit = ApiDBUtils.findCorrectResourceLimit(account.getPrimaryStorageLimit(), account.getId(), ResourceType.primary_storage);
        final String primaryStorageLimitDisplay = (fullView || primaryStorageLimit == -1) ? "Unlimited" : String.valueOf(primaryStorageLimit / ResourceType.bytesToGiB);
        final long primaryStorageTotal = (account.getPrimaryStorageTotal() == null) ? 0 : (account.getPrimaryStorageTotal() / ResourceType.bytesToGiB);
        final String primaryStorageAvail = (fullView || primaryStorageLimit == -1) ? "Unlimited" : String.valueOf((primaryStorageLimit / ResourceType.bytesToGiB) -
                primaryStorageTotal);

        response.setPrimaryStorageLimit(primaryStorageLimitDisplay);
        response.setPrimaryStorageTotal(primaryStorageTotal);
        response.setPrimaryStorageAvailable(primaryStorageAvail);

        //get resource limits for secondary storage space and convert it from Bytes to GiB
        final long secondaryStorageLimit = ApiDBUtils.findCorrectResourceLimit(account.getSecondaryStorageLimit(), account.getId(), ResourceType.secondary_storage);
        final String secondaryStorageLimitDisplay = (fullView || secondaryStorageLimit == -1) ? "Unlimited" : String.valueOf(secondaryStorageLimit / ResourceType.bytesToGiB);
        final long secondaryStorageTotal = (account.getSecondaryStorageTotal() == null) ? 0 : (account.getSecondaryStorageTotal() / ResourceType.bytesToGiB);
        final String secondaryStorageAvail = (fullView || secondaryStorageLimit == -1) ? "Unlimited" : String.valueOf((secondaryStorageLimit / ResourceType.bytesToGiB)
                - secondaryStorageTotal);

        response.setSecondaryStorageLimit(secondaryStorageLimitDisplay);
        response.setSecondaryStorageTotal(secondaryStorageTotal);
        response.setSecondaryStorageAvailable(secondaryStorageAvail);
    }
}
