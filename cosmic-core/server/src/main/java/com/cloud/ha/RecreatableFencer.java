package com.cloud.ha;

import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.utils.component.AdapterBase;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RecreatableFencer extends AdapterBase implements FenceBuilder {
    private static final Logger s_logger = LoggerFactory.getLogger(RecreatableFencer.class);
    @Inject
    VolumeDao _volsDao;
    @Inject
    PrimaryDataStoreDao _poolDao;

    public RecreatableFencer() {
        super();
    }

    @Override
    public Boolean fenceOff(final VirtualMachine vm, final Host host) {
        final VirtualMachineType type = vm.getType();
        if (type != VirtualMachineType.ConsoleProxy && type != VirtualMachineType.DomainRouter && type != VirtualMachineType.SecondaryStorageVm) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Don't know how to fence off " + type);
            }
            return null;
        }
        final List<VolumeVO> vols = _volsDao.findByInstance(vm.getId());
        for (final VolumeVO vol : vols) {
            if (!vol.isRecreatable()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Unable to fence off volumes that are not recreatable: " + vol);
                }
                return null;
            }
            if (vol.getPoolType().isShared()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Unable to fence off volumes that are shared: " + vol);
                }
                return null;
            }
        }
        return true;
    }
}
