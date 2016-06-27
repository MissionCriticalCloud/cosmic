package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMReservationVO;
import org.apache.cloudstack.engine.cloud.entity.api.db.VolumeReservationVO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class VMReservationDaoImpl extends GenericDaoBase<VMReservationVO, Long> implements VMReservationDao {

    protected SearchBuilder<VMReservationVO> VmIdSearch;

    @Inject
    protected VolumeReservationDao _volumeReservationDao;

    public VMReservationDaoImpl() {
    }

    @PostConstruct
    public void init() {
        VmIdSearch = createSearchBuilder();
        VmIdSearch.and("vmId", VmIdSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        VmIdSearch.done();
    }

    @Override
    public VMReservationVO findByVmId(final long vmId) {
        final SearchCriteria<VMReservationVO> sc = VmIdSearch.create("vmId", vmId);
        final VMReservationVO vmRes = findOneBy(sc);
        loadVolumeReservation(vmRes);
        return vmRes;
    }

    @Override
    public void loadVolumeReservation(final VMReservationVO reservation) {
        if (reservation != null) {
            final List<VolumeReservationVO> volumeResList = _volumeReservationDao.listVolumeReservation(reservation.getId());
            final Map<Long, Long> volumeReservationMap = new HashMap<>();

            for (final VolumeReservationVO res : volumeResList) {
                volumeReservationMap.put(res.getVolumeId(), res.getPoolId());
            }
            reservation.setVolumeReservation(volumeReservationMap);
        }
    }

    @Override
    public VMReservationVO findByReservationId(final String reservationId) {
        final VMReservationVO vmRes = super.findByUuid(reservationId);
        loadVolumeReservation(vmRes);
        return vmRes;
    }

    @Override
    @DB
    public VMReservationVO persist(final VMReservationVO reservation) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final VMReservationVO dbVO = super.persist(reservation);

        saveVolumeReservation(reservation);
        loadVolumeReservation(dbVO);

        txn.commit();

        return dbVO;
    }

    private void saveVolumeReservation(final VMReservationVO reservation) {
        if (reservation.getVolumeReservation() != null) {
            for (final Long volumeId : reservation.getVolumeReservation().keySet()) {
                final VolumeReservationVO volumeReservation =
                        new VolumeReservationVO(reservation.getVmId(), volumeId, reservation.getVolumeReservation().get(volumeId), reservation.getId());
                _volumeReservationDao.persist(volumeReservation);
            }
        }
    }
}
