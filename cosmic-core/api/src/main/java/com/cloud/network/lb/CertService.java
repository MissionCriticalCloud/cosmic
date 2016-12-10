package com.cloud.network.lb;

import com.cloud.api.response.SslCertResponse;
import org.apache.cloudstack.api.command.user.loadbalancer.DeleteSslCertCmd;
import org.apache.cloudstack.api.command.user.loadbalancer.ListSslCertsCmd;
import org.apache.cloudstack.api.command.user.loadbalancer.UploadSslCertCmd;

import java.util.List;

public interface CertService {

    public SslCertResponse uploadSslCert(UploadSslCertCmd certCmd);

    public void deleteSslCert(DeleteSslCertCmd deleteSslCertCmd);

    public List<SslCertResponse> listSslCerts(ListSslCertsCmd listSslCertCmd);
}
