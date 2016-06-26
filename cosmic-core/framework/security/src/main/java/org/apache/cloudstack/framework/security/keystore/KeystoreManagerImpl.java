package org.apache.cloudstack.framework.security.keystore;

import com.cloud.utils.Ternary;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.security.CertificateHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeystoreManagerImpl extends ManagerBase implements KeystoreManager {
    private static final Logger s_logger = LoggerFactory.getLogger(KeystoreManagerImpl.class);

    @Inject
    private KeystoreDao _ksDao;

    @Override
    public boolean validateCertificate(final String certificate, final String key, final String domainSuffix) {
        if (certificate == null || certificate.isEmpty() || key == null || key.isEmpty() || domainSuffix == null || domainSuffix.isEmpty()) {
            s_logger.error("Invalid parameter found in (certificate, key, domainSuffix) tuple for domain: " + domainSuffix);
            return false;
        }

        try {
            final String ksPassword = "passwordForValidation";
            final byte[] ksBits = CertificateHelper.buildAndSaveKeystore(domainSuffix, certificate, getKeyContent(key), ksPassword);
            final KeyStore ks = CertificateHelper.loadKeystore(ksBits, ksPassword);
            if (ks != null) {
                return true;
            }

            s_logger.error("Unabled to construct keystore for domain: " + domainSuffix);
        } catch (final Exception e) {
            s_logger.error("Certificate validation failed due to exception for domain: " + domainSuffix, e);
        }
        return false;
    }

    @Override
    public void saveCertificate(final String name, final String certificate, final String key, final String domainSuffix) {
        if (name == null || name.isEmpty() || certificate == null || certificate.isEmpty() || key == null || key.isEmpty() || domainSuffix == null ||
                domainSuffix.isEmpty()) {
            throw new CloudRuntimeException("invalid parameter in saveCerticate");
        }

        _ksDao.save(name, certificate, key, domainSuffix);
    }

    @Override
    public byte[] getKeystoreBits(final String name, final String aliasForCertificateInStore, final String storePassword) {
        assert (name != null);
        assert (aliasForCertificateInStore != null);
        assert (storePassword != null);

        final KeystoreVO ksVo = _ksDao.findByName(name);
        if (ksVo == null) {
            throw new CloudRuntimeException("Unable to find keystore " + name);
        }

        final List<Ternary<String, String, String>> certs = new ArrayList<>();
        final List<KeystoreVO> certChains = _ksDao.findCertChain();

        for (final KeystoreVO ks : certChains) {
            final Ternary<String, String, String> cert = new Ternary<>(ks.getName(), ks.getCertificate(), null);
            certs.add(cert);
        }

        final Ternary<String, String, String> cert = new Ternary<>(ksVo.getName(), ksVo.getCertificate(), getKeyContent(ksVo.getKey()));
        certs.add(cert);

        try {
            return CertificateHelper.buildAndSaveKeystore(certs, storePassword);
        } catch (final KeyStoreException e) {
            s_logger.warn("Unable to build keystore for " + name + " due to KeyStoreException");
        } catch (final CertificateException e) {
            s_logger.warn("Unable to build keystore for " + name + " due to CertificateException");
        } catch (final NoSuchAlgorithmException e) {
            s_logger.warn("Unable to build keystore for " + name + " due to NoSuchAlgorithmException");
        } catch (final InvalidKeySpecException e) {
            s_logger.warn("Unable to build keystore for " + name + " due to InvalidKeySpecException");
        } catch (final IOException e) {
            s_logger.warn("Unable to build keystore for " + name + " due to IOException");
        }
        return null;
    }

    @Override
    public void saveCertificate(final String name, final String certificate, final Integer index, final String domainSuffix) {
        if (name == null || name.isEmpty() || certificate == null || certificate.isEmpty() || index == null || domainSuffix == null || domainSuffix.isEmpty()) {
            throw new CloudRuntimeException("invalid parameter in saveCerticate");
        }

        _ksDao.save(name, certificate, index, domainSuffix);
    }

    @Override
    public Certificates getCertificates(final String name) {
        final KeystoreVO ksVo = _ksDao.findByName(name);
        if (ksVo == null) {
            return null;
        }
        final String prvKey = ksVo.getKey();
        final String prvCert = ksVo.getCertificate();
        final String domainSuffix = ksVo.getDomainSuffix();
        String certChain = null;
        String rootCert = null;
        final List<KeystoreVO> certchains = _ksDao.findCertChain(domainSuffix);
        if (certchains.size() > 0) {
            final ArrayList<String> chains = new ArrayList<>();
            for (final KeystoreVO cert : certchains) {
                if (chains.size() == 0) {// For the first time it will be length 0
                    rootCert = cert.getCertificate();
                }
                chains.add(cert.getCertificate());
            }
            Collections.reverse(chains);
            certChain = StringUtils.join(chains, "\n");
        }
        final Certificates certs = new Certificates(prvKey, prvCert, certChain, rootCert);
        return certs;
    }

    private static String getKeyContent(final String key) {
        final Pattern regex = Pattern.compile("(^[\\-]+[^\\-]+[\\-]+[\\n]?)([^\\-]+)([\\-]+[^\\-]+[\\-]+$)");
        final Matcher m = regex.matcher(key);
        if (m.find()) {
            return m.group(2);
        }

        return key;
    }
}
