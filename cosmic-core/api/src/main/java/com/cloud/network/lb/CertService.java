package com.cloud.network.lb;

import org.apache.cloudstack.api.command.user.loadbalancer.DeleteSslCertCmd;
import org.apache.cloudstack.api.command.user.loadbalancer.ListSslCertsCmd;
import org.apache.cloudstack.api.command.user.loadbalancer.UploadSslCertCmd;
import org.apache.cloudstack.api.response.SslCertResponse;

import java.util.List;

public interface CertService {

    public SslCertResponse uploadSslCert(UploadSslCertCmd certCmd);

    public void deleteSslCert(DeleteSslCertCmd deleteSslCertCmd);

    public List<SslCertResponse> listSslCerts(ListSslCertsCmd listSslCertCmd);
}
