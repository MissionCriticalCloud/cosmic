package com.cloud.certificate.dao;

import com.cloud.certificate.CertificateVO;
import com.cloud.utils.db.GenericDao;

public interface CertificateDao extends GenericDao<CertificateVO, Long> {
    public Long persistCustomCertToDb(String certStr, CertificateVO cert, Long managementServerId);
}
