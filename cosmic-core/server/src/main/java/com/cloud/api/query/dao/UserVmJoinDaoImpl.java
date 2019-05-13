package com.cloud.api.query.dao;

import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.api.query.vo.UserVmJoinVO;
import com.cloud.api.response.NicResponse;
import com.cloud.api.response.NicSecondaryIpResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.gpu.GPU;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.legacymodel.vm.VmStats;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.service.ServiceOfferingDetailsVO;
import com.cloud.user.AccountManager;
import com.cloud.user.dao.UserDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.vm.UserVmDetailVO;
import com.cloud.vm.VmDetailConstants;
import com.cloud.vm.dao.NicSecondaryIpVO;
import com.cloud.vm.dao.UserVmDetailsDao;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserVmJoinDaoImpl extends GenericDaoBase<UserVmJoinVO, Long> implements UserVmJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UserVmJoinDaoImpl.class);
    private final SearchBuilder<UserVmJoinVO> VmDetailSearch;
    private final SearchBuilder<UserVmJoinVO> activeVmByIsoSearch;
    @Inject
    public AccountManager _accountMgr;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private UserVmDetailsDao _userVmDetailsDao;
    @Inject
    private UserDao _userDao;

    protected UserVmJoinDaoImpl() {

        VmDetailSearch = createSearchBuilder();
        VmDetailSearch.and("idIN", VmDetailSearch.entity().getId(), SearchCriteria.Op.IN);
        VmDetailSearch.done();

        _count = "select count(distinct id) from user_vm_view WHERE ";

        activeVmByIsoSearch = createSearchBuilder();
        activeVmByIsoSearch.and("isoId", activeVmByIsoSearch.entity().getIsoId(), SearchCriteria.Op.EQ);
        activeVmByIsoSearch.and("stateNotIn", activeVmByIsoSearch.entity().getState(), SearchCriteria.Op.NIN);
        activeVmByIsoSearch.done();
    }

    @Override
    public UserVmResponse newUserVmResponse(final ResponseView view, final String objectName, final UserVmJoinVO userVm, final EnumSet<VMDetails> details, final Account caller) {
        final UserVmResponse userVmResponse = new UserVmResponse();

        if (userVm.getHypervisorType() != null) {
            userVmResponse.setHypervisor(userVm.getHypervisorType().toString());
        }
        userVmResponse.setId(userVm.getUuid());
        userVmResponse.setName(userVm.getName());

        if (userVm.getDisplayName() != null) {
            userVmResponse.setDisplayName(userVm.getDisplayName());
        } else {
            userVmResponse.setDisplayName(userVm.getName());
        }

        if (userVm.getAccountType() == Account.ACCOUNT_TYPE_PROJECT) {
            userVmResponse.setProjectId(userVm.getProjectUuid());
            userVmResponse.setProjectName(userVm.getProjectName());
        } else {
            userVmResponse.setAccountName(userVm.getAccountName());
        }

        final User user = _userDao.getUser(userVm.getUserId());
        if (user != null) {
            userVmResponse.setUserId(user.getUuid());
            userVmResponse.setUserName(user.getUsername());
        }
        userVmResponse.setDomainId(userVm.getDomainUuid());
        userVmResponse.setDomainName(userVm.getDomainName());

        userVmResponse.setCreated(userVm.getCreated());
        userVmResponse.setDisplayVm(userVm.isDisplayVm());
        if (userVm.getMaintenancePolicy() != null) {
            userVmResponse.setMaintenancePolicy(userVm.getMaintenancePolicy());
        }
        if (userVm.getBootMenuTimeout() != null) {
            userVmResponse.setBootMenuTimeout(userVm.getBootMenuTimeout());
        }
        if (userVm.getBootOrder() != null) {
            userVmResponse.setBootOrder(userVm.getBootOrder());
        }
        if (userVm.getLastStartDateTime() != null) {
            userVmResponse.setLastStartDateTime(userVm.getLastStartDateTime());
        }
        if (userVm.getLastStartVersion() != null) {
            userVmResponse.setLastStartVersion(userVm.getLastStartVersion());
        }
        if (userVm.getState() != null) {
            userVmResponse.setState(userVm.getState().toString());
        }
        userVmResponse.setHaEnable(userVm.isHaEnabled());
        if (details.contains(VMDetails.all) || details.contains(VMDetails.group)) {
            userVmResponse.setGroupId(userVm.getInstanceGroupUuid());
            userVmResponse.setGroup(userVm.getInstanceGroupName());
        }
        userVmResponse.setZoneId(userVm.getDataCenterUuid());
        userVmResponse.setZoneName(userVm.getDataCenterName());
        if (view == ResponseView.Full) {
            userVmResponse.setHostId(userVm.getHostUuid());
        }
        userVmResponse.setInstanceName(userVm.getInstanceName());
        userVmResponse.setHostName(userVm.getHostName());

        if (details.contains(VMDetails.all) || details.contains(VMDetails.tmpl)) {
            userVmResponse.setTemplateId(userVm.getTemplateUuid());
            userVmResponse.setTemplateName(userVm.getTemplateName());
            userVmResponse.setTemplateDisplayText(userVm.getTemplateDisplayText());
            userVmResponse.setPasswordEnabled(userVm.isPasswordEnabled());
        }
        if (details.contains(VMDetails.all) || details.contains(VMDetails.iso)) {
            userVmResponse.setIsoId(userVm.getIsoUuid());
            userVmResponse.setIsoName(userVm.getIsoName());
            userVmResponse.setIsoDisplayText(userVm.getIsoDisplayText());
        }
        if (details.contains(VMDetails.all) || details.contains(VMDetails.servoff)) {
            userVmResponse.setServiceOfferingId(userVm.getServiceOfferingUuid());
            userVmResponse.setServiceOfferingName(userVm.getServiceOfferingName());
        }
        if (details.contains(VMDetails.all) || details.contains(VMDetails.diskoff)) {
            userVmResponse.setDiskOfferingId(userVm.getDiskOfferingUuid());
            userVmResponse.setDiskOfferingName(userVm.getDiskOfferingName());
        }
        if (details.contains(VMDetails.all) || details.contains(VMDetails.servoff) || details.contains(VMDetails.stats)) {
            userVmResponse.setCpuNumber(userVm.getCpu());
            userVmResponse.setMemory(userVm.getRamSize());
            final ServiceOfferingDetailsVO serviceOfferingDetail = ApiDBUtils.findServiceOfferingDetail(userVm.getServiceOfferingId(), GPU.Keys.vgpuType.toString());
            if (serviceOfferingDetail != null) {
                userVmResponse.setVgpu(serviceOfferingDetail.getValue());
            }
        }
        userVmResponse.setGuestOsId(userVm.getGuestOsUuid());
        if (details.contains(VMDetails.all) || details.contains(VMDetails.volume)) {
            userVmResponse.setRootDeviceId(userVm.getVolumeDeviceId());
            if (userVm.getVolumeType() != null) {
                userVmResponse.setRootDeviceType(userVm.getVolumeType().toString());
            }
            userVmResponse.setRootDeviceController(userVm.getVolumeDiskController());
        }
        userVmResponse.setPassword(userVm.getPassword());
        if (userVm.getJobId() != null) {
            userVmResponse.setJobId(userVm.getJobUuid());
            userVmResponse.setJobStatus(userVm.getJobStatus());
        }
        //userVmResponse.setForVirtualNetwork(userVm.getForVirtualNetwork());

        userVmResponse.setPublicIpId(userVm.getPublicIpUuid());
        userVmResponse.setPublicIp(userVm.getPublicIpAddress());
        userVmResponse.setKeyPairName(userVm.getKeypairName());
        userVmResponse.setOsTypeId(userVm.getGuestOsId());
        userVmResponse.setManufacturerString(userVm.getManufacturerString());
        userVmResponse.setOptimiseFor(userVm.getOptimiseFor());
        userVmResponse.setRequiresRestart(userVm.getRequiresRestart());

        if (details.contains(VMDetails.all) || details.contains(VMDetails.stats)) {
            // stats calculation
            final VmStats vmStats = ApiDBUtils.getVmStatistics(userVm.getId());
            if (vmStats != null) {
                userVmResponse.setCpuUsed(new DecimalFormat("#.##").format(vmStats.getCPUUtilization()) + "%");

                userVmResponse.setNetworkKbsRead((long) vmStats.getNetworkReadKBs());

                userVmResponse.setNetworkKbsWrite((long) vmStats.getNetworkWriteKBs());

                if ((userVm.getHypervisorType() != null) && (userVm.getHypervisorType().equals(HypervisorType.KVM) || userVm.getHypervisorType().equals(HypervisorType.XenServer)
                )) { // support KVM and XenServer only util 2013.06.25
                    userVmResponse.setDiskKbsRead((long) vmStats.getDiskReadKBs());

                    userVmResponse.setDiskKbsWrite((long) vmStats.getDiskWriteKBs());

                    userVmResponse.setDiskIORead((long) vmStats.getDiskReadIOs());

                    userVmResponse.setDiskIOWrite((long) vmStats.getDiskWriteIOs());
                }
            }
        }

        if (details.contains(VMDetails.all) || details.contains(VMDetails.nics)) {
            createUserVmNicResponse(userVm, userVmResponse);
        }

        // update tag information
        createUserVmTagsResponse(userVmResponse, userVm);

        if (details.contains(VMDetails.all) || details.contains(VMDetails.affgrp)) {
            createUserVmAffinityResponse(userVm, userVmResponse);
        }

        // set resource details map
        // only hypervisortoolsversion can be returned to the end user
        final UserVmDetailVO hypervisorToolsVersion = _userVmDetailsDao.findDetail(userVm.getId(), VmDetailConstants.HYPERVISOR_TOOLS_VERSION);
        if (hypervisorToolsVersion != null) {
            final Map<String, String> resourceDetails = new HashMap<>();
            resourceDetails.put(hypervisorToolsVersion.getName(), hypervisorToolsVersion.getValue());
            userVmResponse.setDetails(resourceDetails);
        }

        userVmResponse.setObjectName(objectName);
        if (userVm.isDynamicallyScalable() == null) {
            userVmResponse.setDynamicallyScalable(false);
        } else {
            userVmResponse.setDynamicallyScalable(userVm.isDynamicallyScalable());
        }

        return userVmResponse;
    }

    private void createUserVmAffinityResponse(final UserVmJoinVO userVm, final UserVmResponse userVmResponse) {
        final Long affinityGroupId = userVm.getAffinityGroupId();
        if (affinityGroupId != null && affinityGroupId.longValue() != 0) {
            final AffinityGroupResponse resp = new AffinityGroupResponse();
            resp.setId(userVm.getAffinityGroupUuid());
            resp.setName(userVm.getAffinityGroupName());
            resp.setDescription(userVm.getAffinityGroupDescription());
            resp.setObjectName("affinitygroup");
            resp.setAccountName(userVm.getAccountName());
            userVmResponse.addAffinityGroup(resp);
        }
    }

    private void createUserVmNicResponse(final UserVmJoinVO userVm, final UserVmResponse userVmResponse) {
        final long nic_id = userVm.getNicId();
        if (nic_id > 0) {
            final NicResponse nicResponse = new NicResponse();
            nicResponse.setId(userVm.getNicUuid());
            nicResponse.setIpaddress(userVm.getIpAddress());
            nicResponse.setGateway(userVm.getGateway());
            nicResponse.setNetmask(userVm.getNetmask());
            nicResponse.setNetworkid(userVm.getNetworkUuid());
            nicResponse.setNetworkName(userVm.getNetworkName());
            nicResponse.setMacAddress(userVm.getMacAddress());
            nicResponse.setIp6Address(userVm.getIp6Address());
            nicResponse.setIp6Gateway(userVm.getIp6Gateway());
            nicResponse.setIp6Cidr(userVm.getIp6Cidr());
            if (userVm.getBroadcastUri() != null) {
                nicResponse.setBroadcastUri(userVm.getBroadcastUri().toString());
            }
            if (userVm.getIsolationUri() != null) {
                nicResponse.setIsolationUri(userVm.getIsolationUri().toString());
            }
            if (userVm.getMirrorIpAddress() != null) {
                nicResponse.setMirrorIpAddress(userVm.getMirrorIpAddress());
            }
            if (userVm.getMirrorKey() != null) {
                nicResponse.setMirrorKey(userVm.getMirrorKey());
            }
            if (userVm.getTrafficType() != null) {
                nicResponse.setTrafficType(userVm.getTrafficType().toString());
            }
            if (userVm.getGuestType() != null) {
                nicResponse.setType(userVm.getGuestType().toString());
            }
            nicResponse.setIsDefault(userVm.isDefaultNic());
            final List<NicSecondaryIpVO> secondaryIps = ApiDBUtils.findNicSecondaryIps(userVm.getNicId());
            if (secondaryIps != null) {
                final List<NicSecondaryIpResponse> ipList = new ArrayList<>();
                for (final NicSecondaryIpVO ip : secondaryIps) {
                    final NicSecondaryIpResponse ipRes = new NicSecondaryIpResponse();
                    ipRes.setId(ip.getUuid());
                    ipRes.setIpAddr(ip.getIp4Address());
                    ipList.add(ipRes);
                }
                nicResponse.setSecondaryIps(ipList);
            }
            nicResponse.setObjectName("nic");
            userVmResponse.addNic(nicResponse);
        }
    }

    @Override
    public UserVmResponse setUserVmResponse(final ResponseView view, final UserVmResponse userVmData, final UserVmJoinVO uvo) {
        createUserVmNicResponse(uvo, userVmData);

        createUserVmTagsResponse(userVmData, uvo);

        createUserVmAffinityResponse(uvo, userVmData);

        return userVmData;
    }

    private void createUserVmTagsResponse(final UserVmResponse userVmData, final UserVmJoinVO uvo) {
        final long tag_id = uvo.getTagId();
        if (tag_id > 0 && !userVmData.containTag(tag_id)) {
            final ResourceTagJoinVO vtag = ApiDBUtils.findResourceTagViewById(tag_id);
            if (vtag != null) {
                userVmData.addTag(ApiDBUtils.newResourceTagResponse(vtag, false));
            }
        }
    }

    @Override
    public List<UserVmJoinVO> newUserVmView(final UserVm... userVms) {

        final Hashtable<Long, UserVm> userVmDataHash = new Hashtable<>();
        for (final UserVm vm : userVms) {
            if (!userVmDataHash.containsKey(vm.getId())) {
                userVmDataHash.put(vm.getId(), vm);
            }
        }

        final Set<Long> vmIdSet = userVmDataHash.keySet();
        final List<UserVmJoinVO> uvms = searchByIds(vmIdSet.toArray(new Long[vmIdSet.size()]));
        // populate transit password field from UserVm
        if (uvms != null) {
            for (final UserVmJoinVO uvm : uvms) {
                final UserVm v = userVmDataHash.get(uvm.getId());
                uvm.setPassword(v.getPassword());
            }
        }
        return uvms;
    }

    @Override
    public List<UserVmJoinVO> searchByIds(final Long... vmIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<UserVmJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (vmIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= vmIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                createUserVmList(DETAILS_BATCH_SIZE, uvList, curr_index, ids, vmIds);
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < vmIds.length) {
            final int batch_size = (vmIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            createUserVmList(batch_size, uvList, curr_index, ids, vmIds);
        }
        return uvList;
    }

    private void createUserVmList(final int DETAILS_BATCH_SIZE, final List<UserVmJoinVO> uvList, final int curr_index, final Long[] ids, final Long[] vmIds) {
        for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
            ids[k] = vmIds[j];
        }
        final SearchCriteria<UserVmJoinVO> sc = VmDetailSearch.create();
        sc.setParameters("idIN", ids);
        final List<UserVmJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
        if (vms != null) {
            uvList.addAll(vms);
        }
    }

    @Override
    public List<UserVmJoinVO> listActiveByIsoId(final Long isoId) {
        final SearchCriteria<UserVmJoinVO> sc = activeVmByIsoSearch.create();
        sc.setParameters("isoId", isoId);
        final State[] states = new State[2];
        states[0] = State.Error;
        states[1] = State.Expunging;
        return listBy(sc);
    }
}
