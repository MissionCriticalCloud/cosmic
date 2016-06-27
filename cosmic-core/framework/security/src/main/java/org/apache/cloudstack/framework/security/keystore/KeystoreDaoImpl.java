package org.apache.cloudstack.framework.security.keystore;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class KeystoreDaoImpl extends GenericDaoBase<KeystoreVO, Long> implements KeystoreDao {
    protected final SearchBuilder<KeystoreVO> FindByNameSearch;
    protected final SearchBuilder<KeystoreVO> CertChainSearch;
    protected final SearchBuilder<KeystoreVO> CertChainSearchForDomainSuffix;

    public KeystoreDaoImpl() {
        FindByNameSearch = createSearchBuilder();
        FindByNameSearch.and("name", FindByNameSearch.entity().getName(), Op.EQ);
        FindByNameSearch.done();

        CertChainSearch = createSearchBuilder();
        CertChainSearch.and("key", CertChainSearch.entity().getKey(), Op.NULL);
        CertChainSearch.done();

        CertChainSearchForDomainSuffix = createSearchBuilder();
        CertChainSearchForDomainSuffix.and("key", CertChainSearchForDomainSuffix.entity().getKey(), Op.NULL);
        CertChainSearchForDomainSuffix.and("domainSuffix", CertChainSearchForDomainSuffix.entity().getDomainSuffix(), Op.EQ);
        CertChainSearchForDomainSuffix.done();
    }

    @Override
    public List<KeystoreVO> findCertChain() {
        final SearchCriteria<KeystoreVO> sc = CertChainSearch.create();
        final List<KeystoreVO> ks = listBy(sc);
        Collections.sort(ks, new Comparator() {
            @Override
            public int compare(final Object o1, final Object o2) {
                final Integer seq1 = ((KeystoreVO) o1).getIndex();
                final Integer seq2 = ((KeystoreVO) o2).getIndex();
                return seq1.compareTo(seq2);
            }
        });
        return ks;
    }

    @Override
    public List<KeystoreVO> findCertChain(final String domainSuffix) {
        final SearchCriteria<KeystoreVO> sc = CertChainSearchForDomainSuffix.create();
        sc.setParameters("domainSuffix", domainSuffix);
        final List<KeystoreVO> ks = listBy(sc);
        Collections.sort(ks, new Comparator() {
            public int compare(final Object o1, final Object o2) {
                final Integer seq1 = ((KeystoreVO) o1).getIndex();
                final Integer seq2 = ((KeystoreVO) o2).getIndex();
                return seq1.compareTo(seq2);
            }
        });
        return ks;
    }

    @Override
    public KeystoreVO findByName(final String name) {
        assert (name != null);

        final SearchCriteria<KeystoreVO> sc = FindByNameSearch.create();
        sc.setParameters("name", name);
        return findOneBy(sc);
    }

    @Override
    @DB
    public void save(final String name, final String certificate, final String key, final String domainSuffix) {
        KeystoreVO keystore = findByName(name);
        if (keystore != null) {
            keystore.setCertificate(certificate);
            keystore.setKey(key);
            keystore.setDomainSuffix(domainSuffix);
            this.update(keystore.getId(), keystore);
        } else {
            keystore = new KeystoreVO();
            keystore.setName(name);
            keystore.setCertificate(certificate);
            keystore.setKey(key);
            keystore.setDomainSuffix(domainSuffix);
            this.persist(keystore);
        }
    }

    @Override
    @DB
    public void save(final String alias, final String certificate, final Integer index, final String domainSuffix) {
        KeystoreVO ks = findByName(alias);
        if (ks != null) {
            ks.setCertificate(certificate);
            ks.setName(alias);
            ks.setIndex(index);
            ks.setDomainSuffix(domainSuffix);
            this.update(ks.getId(), ks);
        } else {
            ks = new KeystoreVO();
            ks.setCertificate(certificate);
            ks.setName(alias);
            ks.setIndex(index);
            ks.setDomainSuffix(domainSuffix);
            this.persist(ks);
        }
    }
}
