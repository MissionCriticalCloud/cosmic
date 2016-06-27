package com.cloud.cluster.dao;

import com.cloud.cluster.ManagementServerHost;
import com.cloud.cluster.ManagementServerHostPeerVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementServerHostPeerDaoImpl extends GenericDaoBase<ManagementServerHostPeerVO, Long> implements ManagementServerHostPeerDao {
    private static final Logger s_logger = LoggerFactory.getLogger(ManagementServerHostPeerDaoImpl.class);

    private final SearchBuilder<ManagementServerHostPeerVO> ClearPeerSearch;
    private final SearchBuilder<ManagementServerHostPeerVO> FindForUpdateSearch;
    private final SearchBuilder<ManagementServerHostPeerVO> CountSearch;

    public ManagementServerHostPeerDaoImpl() {
        ClearPeerSearch = createSearchBuilder();
        ClearPeerSearch.and("ownerMshost", ClearPeerSearch.entity().getOwnerMshost(), SearchCriteria.Op.EQ);
        ClearPeerSearch.done();

        FindForUpdateSearch = createSearchBuilder();
        FindForUpdateSearch.and("ownerMshost", FindForUpdateSearch.entity().getOwnerMshost(), SearchCriteria.Op.EQ);
        FindForUpdateSearch.and("peerMshost", FindForUpdateSearch.entity().getPeerMshost(), SearchCriteria.Op.EQ);
        FindForUpdateSearch.and("peerRunid", FindForUpdateSearch.entity().getPeerRunid(), SearchCriteria.Op.EQ);
        FindForUpdateSearch.done();

        CountSearch = createSearchBuilder();
        CountSearch.and("peerMshost", CountSearch.entity().getPeerMshost(), SearchCriteria.Op.EQ);
        CountSearch.and("peerRunid", CountSearch.entity().getPeerRunid(), SearchCriteria.Op.EQ);
        CountSearch.and("peerState", CountSearch.entity().getPeerState(), SearchCriteria.Op.EQ);
        CountSearch.done();
    }

    @Override
    @DB
    public void clearPeerInfo(final long ownerMshost) {
        final SearchCriteria<ManagementServerHostPeerVO> sc = ClearPeerSearch.create();
        sc.setParameters("ownerMshost", ownerMshost);

        expunge(sc);
    }

    @Override
    @DB
    public void updatePeerInfo(final long ownerMshost, final long peerMshost, final long peerRunid, final ManagementServerHost.State peerState) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();

            final SearchCriteria<ManagementServerHostPeerVO> sc = FindForUpdateSearch.create();
            sc.setParameters("ownerMshost", ownerMshost);
            sc.setParameters("peerMshost", peerMshost);
            sc.setParameters("peerRunid", peerRunid);
            final List<ManagementServerHostPeerVO> l = listBy(sc);
            if (l.size() == 1) {
                final ManagementServerHostPeerVO peer = l.get(0);
                peer.setPeerState(peerState);
                update(peer.getId(), peer);
            } else {
                final ManagementServerHostPeerVO peer = new ManagementServerHostPeerVO(ownerMshost, peerMshost, peerRunid, peerState);
                persist(peer);
            }
            txn.commit();
        } catch (final Exception e) {
            s_logger.warn("Unexpected exception, ", e);
            txn.rollback();
        }
    }

    @Override
    @DB
    public int countStateSeenInPeers(final long mshost, final long runid, final ManagementServerHost.State state) {
        final SearchCriteria<ManagementServerHostPeerVO> sc = CountSearch.create();
        sc.setParameters("peerMshost", mshost);
        sc.setParameters("peerRunid", runid);
        sc.setParameters("peerState", state);

        final List<ManagementServerHostPeerVO> l = listBy(sc);
        return l.size();
    }
}
