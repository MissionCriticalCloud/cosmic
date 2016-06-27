package com.cloud.certificate.dao;

import com.cloud.certificate.CertificateVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB
public class CertificateDaoImpl extends GenericDaoBase<CertificateVO, Long> implements CertificateDao {

    private static final Logger s_logger = LoggerFactory.getLogger(CertificateDaoImpl.class);

    public CertificateDaoImpl() {

    }

    @Override
    public Long persistCustomCertToDb(final String certStr, final CertificateVO cert, final Long managementServerId) {
        try {
            cert.setCertificate(certStr);
            cert.setUpdated("Y");
            update(cert.getId(), cert);
            return cert.getId();
        } catch (final Exception e) {
            s_logger.warn("Unable to read the certificate: " + e);
            return new Long(0);
        }
    }
}
