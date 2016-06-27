package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.engine.cloud.entity.api.db.VolumeReservationVO;

import javax.annotation.PostConstruct;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VolumeReservationDaoImpl extends GenericDaoBase<VolumeReservationVO, Long> implements VolumeReservationDao {

    protected SearchBuilder<VolumeReservationVO> VmIdSearch;
    protected SearchBuilder<VolumeReservationVO> VmReservationIdSearch;

    public VolumeReservationDaoImpl() {
    }

    @PostConstruct
    public void init() {
        VmIdSearch = createSearchBuilder();
        VmIdSearch.and("vmId", VmIdSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        VmIdSearch.done();

        VmReservationIdSearch = createSearchBuilder();
        VmReservationIdSearch.and("vmReservationId", VmReservationIdSearch.entity().getVmReservationId(), SearchCriteria.Op.EQ);
        VmReservationIdSearch.done();
    }

    @Override
    public VolumeReservationVO findByVmId(final long vmId) {
        final SearchCriteria<VolumeReservationVO> sc = VmIdSearch.create("vmId", vmId);
        return findOneBy(sc);
    }

    @Override
    public List<VolumeReservationVO> listVolumeReservation(final long vmReservationId) {
        final SearchCriteria<VolumeReservationVO> sc = VmReservationIdSearch.create("vmReservationId", vmReservationId);
        return listBy(sc);
    }
}
