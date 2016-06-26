package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmGroupVmMapVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AutoScaleVmGroupVmMapDaoImpl extends GenericDaoBase<AutoScaleVmGroupVmMapVO, Long> implements AutoScaleVmGroupVmMapDao {

    @Override
    public Integer countByGroup(final long vmGroupId) {

        final SearchCriteria<AutoScaleVmGroupVmMapVO> sc = createSearchCriteria();
        sc.addAnd("vmGroupId", SearchCriteria.Op.EQ, vmGroupId);
        return getCount(sc);
    }

    @Override
    public List<AutoScaleVmGroupVmMapVO> listByGroup(final long vmGroupId) {
        final SearchCriteria<AutoScaleVmGroupVmMapVO> sc = createSearchCriteria();
        sc.addAnd("vmGroupId", SearchCriteria.Op.EQ, vmGroupId);
        return listBy(sc);
    }

    @Override
    public int remove(final long vmGroupId, final long vmId) {
        final SearchCriteria<AutoScaleVmGroupVmMapVO> sc = createSearchCriteria();
        sc.addAnd("vmGroupId", SearchCriteria.Op.EQ, vmGroupId);
        sc.addAnd("instanceId", SearchCriteria.Op.EQ, vmId);
        return remove(sc);
    }
}
