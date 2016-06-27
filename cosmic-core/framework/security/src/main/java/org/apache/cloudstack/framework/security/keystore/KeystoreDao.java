package org.apache.cloudstack.framework.security.keystore;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface KeystoreDao extends GenericDao<KeystoreVO, Long> {
    KeystoreVO findByName(String name);

    void save(String name, String certificate, String key, String domainSuffix);

    void save(String alias, String certificate, Integer index, String domainSuffix);

    List<KeystoreVO> findCertChain();

    List<KeystoreVO> findCertChain(String domainSuffix);
}
