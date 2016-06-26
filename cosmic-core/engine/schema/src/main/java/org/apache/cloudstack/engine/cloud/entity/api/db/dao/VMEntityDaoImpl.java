package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMEntityVO;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMReservationVO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMEntityDaoImpl extends GenericDaoBase<VMEntityVO, Long> implements VMEntityDao {

    public static final Logger s_logger = LoggerFactory.getLogger(VMEntityDaoImpl.class);

    @Inject
    protected VMReservationDao _vmReservationDao;

    @Inject
    protected VMComputeTagDao _vmComputeTagDao;

    @Inject
    protected VMRootDiskTagDao _vmRootDiskTagsDao;

    @Inject
    protected VMNetworkMapDao _vmNetworkMapDao;

    @Inject
    protected NetworkDao _networkDao;

    public VMEntityDaoImpl() {
    }

    @PostConstruct
    protected void init() {

    }

    @Override
    @DB
    public VMEntityVO persist(final VMEntityVO vm) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final VMEntityVO dbVO = super.persist(vm);

        saveVmNetworks(vm);
        loadVmNetworks(dbVO);
        saveVmReservation(vm);
        loadVmReservation(dbVO);
        saveComputeTags(vm.getId(), vm.getComputeTags());
        loadComputeTags(dbVO);
        saveRootDiskTags(vm.getId(), vm.getRootDiskTags());
        loadRootDiskTags(dbVO);

        txn.commit();

        return dbVO;
    }

    private void saveVmNetworks(final VMEntityVO vm) {
        final List<Long> networks = new ArrayList<>();

        final List<String> networksIds = vm.getNetworkIds();

        if (networksIds == null || (networksIds != null && networksIds.isEmpty())) {
            return;
        }

        for (final String uuid : networksIds) {
            final NetworkVO network = _networkDao.findByUuid(uuid);
            if (network != null) {
                networks.add(network.getId());
            }
        }
        _vmNetworkMapDao.persist(vm.getId(), networks);
    }

    private void loadVmNetworks(final VMEntityVO dbVO) {
        final List<Long> networksIds = _vmNetworkMapDao.getNetworks(dbVO.getId());

        final List<String> networks = new ArrayList<>();
        for (final Long networkId : networksIds) {
            final NetworkVO network = _networkDao.findById(networkId);
            if (network != null) {
                networks.add(network.getUuid());
            }
        }

        dbVO.setNetworkIds(networks);
    }

    private void saveVmReservation(final VMEntityVO vm) {
        if (vm.getVmReservation() != null) {
            _vmReservationDao.persist(vm.getVmReservation());
        }
    }

    @Override
    public void loadVmReservation(final VMEntityVO vm) {
        final VMReservationVO vmReservation = _vmReservationDao.findByVmId(vm.getId());
        vm.setVmReservation(vmReservation);
    }

    private void saveComputeTags(final long vmId, final List<String> computeTags) {
        if (computeTags == null || (computeTags != null && computeTags.isEmpty())) {
            return;
        }

        _vmComputeTagDao.persist(vmId, computeTags);
    }

    private void loadComputeTags(final VMEntityVO dbVO) {
        final List<String> computeTags = _vmComputeTagDao.getComputeTags(dbVO.getId());
        dbVO.setComputeTags(computeTags);
    }

    private void saveRootDiskTags(final long vmId, final List<String> rootDiskTags) {
        if (rootDiskTags == null || (rootDiskTags != null && rootDiskTags.isEmpty())) {
            return;
        }
        _vmRootDiskTagsDao.persist(vmId, rootDiskTags);
    }

    private void loadRootDiskTags(final VMEntityVO dbVO) {
        final List<String> rootDiskTags = _vmRootDiskTagsDao.getRootDiskTags(dbVO.getId());
        dbVO.setRootDiskTags(rootDiskTags);
    }
}
