package com.cloud.framework.security.keystore;

import com.cloud.legacymodel.auth.Certificates;
import com.cloud.utils.component.Manager;

public interface KeystoreManager extends Manager {
    boolean validateCertificate(String certificate, String key, String domainSuffix);

    void saveCertificate(String name, String certificate, String key, String domainSuffix);

    byte[] getKeystoreBits(String name, String aliasForCertificateInStore, String storePassword);

    void saveCertificate(String name, String certificate, Integer index, String domainSuffix);

    Certificates getCertificates(String name);
}
