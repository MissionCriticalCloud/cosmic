package com.cloud.ha;

import com.cloud.host.Host;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;

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
        final VirtualMachine.Type type = vm.getType();
        if (type != VirtualMachine.Type.ConsoleProxy && type != VirtualMachine.Type.DomainRouter && type != VirtualMachine.Type.SecondaryStorageVm) {
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
