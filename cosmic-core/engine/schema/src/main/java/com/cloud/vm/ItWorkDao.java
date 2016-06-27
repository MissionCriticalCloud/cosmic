package com.cloud.vm;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.ItWorkVO.Step;
import com.cloud.vm.VirtualMachine.State;

import java.util.List;

public interface ItWorkDao extends GenericDao<ItWorkVO, String> {
    /**
     * find a work item based on the instanceId and the state.
     *
     * @param instanceId vm instance id
     * @param state      state
     * @return ItWorkVO if found; null if not.
     */
    ItWorkVO findByOutstandingWork(long instanceId, State state);

    /**
     * cleanup rows that are either Done or Cancelled and been that way
     * for at least wait time.
     */
    void cleanup(long wait);

    boolean updateStep(ItWorkVO work, Step step);

    List<ItWorkVO> listWorkInProgressFor(long nodeId);
}
