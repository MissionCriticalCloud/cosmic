package com.cloud.cluster.agentlb.dao;

import com.cloud.cluster.agentlb.HostTransferMapVO;
import com.cloud.cluster.agentlb.HostTransferMapVO.HostTransferState;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface HostTransferMapDao extends GenericDao<HostTransferMapVO, Long> {

    List<HostTransferMapVO> listHostsLeavingCluster(long currentOwnerId);

    List<HostTransferMapVO> listHostsJoiningCluster(long futureOwnerId);

    HostTransferMapVO startAgentTransfering(long hostId, long currentOwner, long futureOwner);

    boolean completeAgentTransfer(long hostId);

    List<HostTransferMapVO> listBy(long futureOwnerId, HostTransferState state);

    HostTransferMapVO findActiveHostTransferMapByHostId(long hostId, Date cutTime);

    boolean startAgentTransfer(long hostId);

    HostTransferMapVO findByIdAndFutureOwnerId(long id, long futureOwnerId);

    HostTransferMapVO findByIdAndCurrentOwnerId(long id, long currentOwnerId);
}
