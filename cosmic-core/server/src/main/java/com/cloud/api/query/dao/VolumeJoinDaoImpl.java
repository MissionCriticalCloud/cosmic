package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.ApiResponseHelper;
import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.api.query.vo.VolumeJoinVO;
import com.cloud.offering.ServiceOffering;
import com.cloud.storage.Storage;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.Volume;
import com.cloud.user.AccountManager;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VolumeJoinDaoImpl extends GenericDaoBase<VolumeJoinVO, Long> implements VolumeJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(VolumeJoinDaoImpl.class);
    private final SearchBuilder<VolumeJoinVO> volSearch;
    private final SearchBuilder<VolumeJoinVO> volIdSearch;
    @Inject
    public AccountManager _accountMgr;
    @Inject
    private ConfigurationDao _configDao;

    protected VolumeJoinDaoImpl() {

        volSearch = createSearchBuilder();
        volSearch.and("idIN", volSearch.entity().getId(), SearchCriteria.Op.IN);
        volSearch.done();

        volIdSearch = createSearchBuilder();
        volIdSearch.and("id", volIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        volIdSearch.done();

        _count = "select count(distinct id) from volume_view WHERE ";
    }

    @Override
    public VolumeResponse newVolumeResponse(final ResponseView view, final VolumeJoinVO volume) {
        final VolumeResponse volResponse = new VolumeResponse();
        volResponse.setId(volume.getUuid());

        if (volume.getName() != null) {
            volResponse.setName(volume.getName());
        } else {
            volResponse.setName("");
        }

        volResponse.setZoneId(volume.getDataCenterUuid());
        volResponse.setZoneName(volume.getDataCenterName());

        volResponse.setVolumeType(volume.getVolumeType().toString());
        volResponse.setDeviceId(volume.getDeviceId());

        final long instanceId = volume.getVmId();
        if (instanceId > 0 && volume.getState() != Volume.State.Destroy) {
            volResponse.setVirtualMachineId(volume.getVmUuid());
            volResponse.setVirtualMachineName(volume.getVmName());
            volResponse.setVirtualMachineState(volume.getVmState().toString());
            if (volume.getVmDisplayName() != null) {
                volResponse.setVirtualMachineDisplayName(volume.getVmDisplayName());
            } else {
                volResponse.setVirtualMachineDisplayName(volume.getVmName());
            }
        }

        volResponse.setProvisioningType(volume.getProvisioningType().toString());

        // Show the virtual size of the volume
        volResponse.setSize(volume.getSize());

        volResponse.setMinIops(volume.getMinIops());
        volResponse.setMaxIops(volume.getMaxIops());

        volResponse.setCreated(volume.getCreated());
        volResponse.setState(volume.getState().toString());
        if (volume.getState() == Volume.State.UploadOp) {
            // com.cloud.storage.VolumeHostVO volumeHostRef =
            // ApiDBUtils.findVolumeHostRef(volume.getId(),
            // volume.getDataCenterId());
            volResponse.setSize(volume.getVolumeStoreSize());
            volResponse.setCreated(volume.getCreatedOnStore());

            if (view == ResponseView.Full) {
                volResponse.setHypervisor(ApiDBUtils.getHypervisorTypeFromFormat(volume.getDataCenterId(), volume.getFormat()).toString());
            }
            if (volume.getDownloadState() != Status.DOWNLOADED) {
                String volumeStatus = "Processing";
                if (volume.getDownloadState() == VMTemplateHostVO.Status.DOWNLOAD_IN_PROGRESS) {
                    if (volume.getDownloadPercent() == 100) {
                        volumeStatus = "Checking Volume";
                    } else {
                        volumeStatus = volume.getDownloadPercent() + "% Uploaded";
                    }
                    volResponse.setState("Uploading");
                } else {
                    volumeStatus = volume.getErrorString();
                    if (volume.getDownloadState() == VMTemplateHostVO.Status.NOT_DOWNLOADED) {
                        volResponse.setState("UploadNotStarted");
                    } else {
                        volResponse.setState("UploadError");
                    }
                }
                volResponse.setStatus(volumeStatus);
            } else if (volume.getDownloadState() == VMTemplateHostVO.Status.DOWNLOADED) {
                volResponse.setStatus("Upload Complete");
                volResponse.setState("Uploaded");
            } else {
                volResponse.setStatus("Successfully Installed");
            }
        }

        if (view == ResponseView.Full) {
            volResponse.setPath(volume.getPath());
        }

        // populate owner.
        ApiResponseHelper.populateOwner(volResponse, volume);

        // DiskOfferingVO diskOffering =
        // ApiDBUtils.findDiskOfferingById(volume.getDiskOfferingId());
        if (volume.getDiskOfferingId() > 0) {
            boolean isServiceOffering = false;
            if (volume.getVolumeType().equals(Volume.Type.ROOT)) {
                isServiceOffering = true;
            } else {
                // can't rely on the fact that the volume is the datadisk as it might have been created as a root, and
                // then detached later
                final long offeringId = volume.getDiskOfferingId();
                if (ApiDBUtils.findDiskOfferingById(offeringId) == null) {
                    isServiceOffering = true;
                }
            }

            if (isServiceOffering) {
                volResponse.setServiceOfferingId(volume.getDiskOfferingUuid());
                volResponse.setServiceOfferingName(volume.getDiskOfferingName());
                volResponse.setServiceOfferingDisplayText(volume.getDiskOfferingDisplayText());
            } else {
                volResponse.setDiskOfferingId(volume.getDiskOfferingUuid());
                volResponse.setDiskOfferingName(volume.getDiskOfferingName());
                volResponse.setDiskOfferingDisplayText(volume.getDiskOfferingDisplayText());
            }

            volResponse.setStorageType(volume.isUseLocalStorage() ? ServiceOffering.StorageType.local.toString() : ServiceOffering.StorageType.shared.toString());
            volResponse.setBytesReadRate(volume.getBytesReadRate());
            volResponse.setBytesWriteRate(volume.getBytesReadRate());
            volResponse.setIopsReadRate(volume.getIopsWriteRate());
            volResponse.setIopsWriteRate(volume.getIopsWriteRate());
        }

        // return hypervisor and storage pool info for ROOT and Resource domain only
        if (view == ResponseView.Full) {
            if (volume.getState() != Volume.State.UploadOp) {
                if (volume.getHypervisorType() != null) {
                    volResponse.setHypervisor(volume.getHypervisorType().toString());
                } else {
                    volResponse.setHypervisor(ApiDBUtils.getHypervisorTypeFromFormat(volume.getDataCenterId(), volume.getFormat()).toString());
                }
            }
            final Long poolId = volume.getPoolId();
            final String poolName = (poolId == null) ? "none" : volume.getPoolName();
            volResponse.setStoragePoolName(poolName);
            volResponse.setStoragePoolId(volume.getPoolUuid());
        }

        volResponse.setAttached(volume.getAttached());
        volResponse.setDestroyed(volume.getState() == Volume.State.Destroy);
        boolean isExtractable = true;
        if (volume.getVolumeType() != Volume.Type.DATADISK) { // Datadisk dont
            // have any
            // template
            // dependence.
            if (volume.getTemplateId() > 0) { // For ISO based volumes template
                // = null and we allow extraction
                // of all ISO based volumes
                isExtractable = volume.isExtractable() && volume.getTemplateType() != Storage.TemplateType.SYSTEM;
            }
        }

        // update tag information
        final long tag_id = volume.getTagId();
        if (tag_id > 0) {
            final ResourceTagJoinVO vtag = ApiDBUtils.findResourceTagViewById(tag_id);
            if (vtag != null) {
                volResponse.addTag(ApiDBUtils.newResourceTagResponse(vtag, false));
            }
        }

        volResponse.setExtractable(isExtractable);
        volResponse.setDisplayVolume(volume.isDisplayVolume());
        volResponse.setChainInfo(volume.getChainInfo());

        volResponse.setTemplateId(volume.getTemplateUuid());
        volResponse.setTemplateName(volume.getTemplateName());
        volResponse.setTemplateDisplayText(volume.getTemplateDisplayText());

        volResponse.setIsoId(volume.getIsoUuid());
        volResponse.setIsoName(volume.getIsoName());
        volResponse.setIsoDisplayText(volume.getIsoDisplayText());

        // set async job
        if (volume.getJobId() != null) {
            volResponse.setJobId(volume.getJobUuid());
            volResponse.setJobStatus(volume.getJobStatus());
        }

        volResponse.setObjectName("volume");
        return volResponse;
    }

    @Override
    public VolumeResponse setVolumeResponse(final ResponseView view, final VolumeResponse volData, final VolumeJoinVO vol) {
        final long tag_id = vol.getTagId();
        if (tag_id > 0) {
            final ResourceTagJoinVO vtag = ApiDBUtils.findResourceTagViewById(tag_id);
            if (vtag != null) {
                volData.addTag(ApiDBUtils.newResourceTagResponse(vtag, false));
            }
        }
        return volData;
    }

    @Override
    public List<VolumeJoinVO> newVolumeView(final Volume vol) {
        final SearchCriteria<VolumeJoinVO> sc = volIdSearch.create();
        sc.setParameters("id", vol.getId());
        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<VolumeJoinVO> searchByIds(final Long... volIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<VolumeJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (volIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= volIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
                    ids[k] = volIds[j];
                }
                final SearchCriteria<VolumeJoinVO> sc = volSearch.create();
                sc.setParameters("idIN", ids);
                final List<VolumeJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
                if (vms != null) {
                    uvList.addAll(vms);
                }
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < volIds.length) {
            final int batch_size = (volIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = volIds[j];
            }
            final SearchCriteria<VolumeJoinVO> sc = volSearch.create();
            sc.setParameters("idIN", ids);
            final List<VolumeJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
            if (vms != null) {
                uvList.addAll(vms);
            }
        }
        return uvList;
    }
}
