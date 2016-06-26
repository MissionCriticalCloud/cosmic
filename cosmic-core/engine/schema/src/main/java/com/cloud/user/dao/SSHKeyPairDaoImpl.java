package com.cloud.user.dao;

import com.cloud.user.SSHKeyPairVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SSHKeyPairDaoImpl extends GenericDaoBase<SSHKeyPairVO, Long> implements SSHKeyPairDao {

    @Override
    public List<SSHKeyPairVO> listKeyPairs(final long accountId, final long domainId) {
        final SearchCriteria<SSHKeyPairVO> sc = createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        return listBy(sc);
    }

    @Override
    public List<SSHKeyPairVO> listKeyPairsByName(final long accountId, final long domainId, final String name) {
        final SearchCriteria<SSHKeyPairVO> sc = createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        sc.addAnd("name", SearchCriteria.Op.EQ, name);
        return listBy(sc);
    }

    @Override
    public List<SSHKeyPairVO> listKeyPairsByFingerprint(final long accountId, final long domainId, final String fingerprint) {
        final SearchCriteria<SSHKeyPairVO> sc = createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        sc.addAnd("fingerprint", SearchCriteria.Op.EQ, fingerprint);
        return listBy(sc);
    }

    @Override
    public SSHKeyPairVO findByName(final long accountId, final long domainId, final String name) {
        final SearchCriteria<SSHKeyPairVO> sc = createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        sc.addAnd("name", SearchCriteria.Op.EQ, name);
        return findOneBy(sc);
    }

    @Override
    public SSHKeyPairVO findByPublicKey(final String publicKey) {
        final SearchCriteria<SSHKeyPairVO> sc = createSearchCriteria();
        sc.addAnd("publicKey", SearchCriteria.Op.EQ, publicKey);
        return findOneBy(sc);
    }

    @Override
    public boolean deleteByName(final long accountId, final long domainId, final String name) {
        final SSHKeyPairVO pair = findByName(accountId, domainId, name);
        if (pair == null) {
            return false;
        }

        expunge(pair.getId());
        return true;
    }

    @Override
    public SSHKeyPairVO findByPublicKey(final long accountId, final long domainId, final String publicKey) {
        final SearchCriteria<SSHKeyPairVO> sc = createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        sc.addAnd("publicKey", SearchCriteria.Op.EQ, publicKey);
        return findOneBy(sc);
    }
}
