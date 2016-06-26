package com.cloud.cluster.agentlb.dao;

import com.cloud.cluster.agentlb.HostTransferMapVO;
import com.cloud.cluster.agentlb.HostTransferMapVO.HostTransferState;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB
public class HostTransferMapDaoImpl extends GenericDaoBase<HostTransferMapVO, Long> implements HostTransferMapDao {
    private static final Logger s_logger = LoggerFactory.getLogger(HostTransferMapDaoImpl.class);

    protected SearchBuilder<HostTransferMapVO> AllFieldsSearch;
    protected SearchBuilder<HostTransferMapVO> IntermediateStateSearch;
    protected SearchBuilder<HostTransferMapVO> ActiveSearch;

    public HostTransferMapDaoImpl() {
        super();
    }

    @PostConstruct
    public void init() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("initialOwner", AllFieldsSearch.entity().getInitialOwner(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("futureOwner", AllFieldsSearch.entity().getFutureOwner(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        IntermediateStateSearch = createSearchBuilder();
        IntermediateStateSearch.and("futureOwner", IntermediateStateSearch.entity().getFutureOwner(), SearchCriteria.Op.EQ);
        IntermediateStateSearch.and("initialOwner", IntermediateStateSearch.entity().getInitialOwner(), SearchCriteria.Op.EQ);
        IntermediateStateSearch.and("state", IntermediateStateSearch.entity().getState(), SearchCriteria.Op.IN);
        IntermediateStateSearch.done();

        ActiveSearch = createSearchBuilder();
        ActiveSearch.and("created", ActiveSearch.entity().getCreated(), SearchCriteria.Op.GT);
        ActiveSearch.and("id", ActiveSearch.entity().getId(), SearchCriteria.Op.EQ);
        ActiveSearch.and("state", ActiveSearch.entity().getState(), SearchCriteria.Op.EQ);
        ActiveSearch.done();
    }

    @Override
    public List<HostTransferMapVO> listHostsLeavingCluster(final long currentOwnerId) {
        final SearchCriteria<HostTransferMapVO> sc = IntermediateStateSearch.create();
        sc.setParameters("initialOwner", currentOwnerId);

        return listBy(sc);
    }

    @Override
    public List<HostTransferMapVO> listHostsJoiningCluster(final long futureOwnerId) {
        final SearchCriteria<HostTransferMapVO> sc = IntermediateStateSearch.create();
        sc.setParameters("futureOwner", futureOwnerId);

        return listBy(sc);
    }

    @Override
    public HostTransferMapVO startAgentTransfering(final long hostId, final long initialOwner, final long futureOwner) {
        final HostTransferMapVO transfer = new HostTransferMapVO(hostId, initialOwner, futureOwner);
        return persist(transfer);
    }

    @Override
    public boolean completeAgentTransfer(final long hostId) {
        return remove(hostId);
    }

    @Override
    public List<HostTransferMapVO> listBy(final long futureOwnerId, final HostTransferState state) {
        final SearchCriteria<HostTransferMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("futureOwner", futureOwnerId);
        sc.setParameters("state", state);

        return listBy(sc);
    }

    @Override
    public HostTransferMapVO findActiveHostTransferMapByHostId(final long hostId, final Date cutTime) {
        final SearchCriteria<HostTransferMapVO> sc = ActiveSearch.create();
        sc.setParameters("id", hostId);
        sc.setParameters("state", HostTransferState.TransferRequested);
        sc.setParameters("created", cutTime);

        return findOneBy(sc);
    }

    @Override
    public boolean startAgentTransfer(final long hostId) {
        final HostTransferMapVO transfer = findById(hostId);
        transfer.setState(HostTransferState.TransferStarted);
        return update(hostId, transfer);
    }

    @Override
    public HostTransferMapVO findByIdAndFutureOwnerId(final long id, final long futureOwnerId) {
        final SearchCriteria<HostTransferMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("futureOwner", futureOwnerId);
        sc.setParameters("id", id);

        return findOneBy(sc);
    }

    @Override
    public HostTransferMapVO findByIdAndCurrentOwnerId(final long id, final long currentOwnerId) {
        final SearchCriteria<HostTransferMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("initialOwner", currentOwnerId);
        sc.setParameters("id", id);

        return findOneBy(sc);
    }
}
