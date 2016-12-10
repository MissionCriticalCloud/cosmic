package com.cloud.network.lb;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface SslCert extends InternalIdentity, Identity, ControlledEntity {

    public String getCertificate();

    public String getKey();

    public String getChain();

    public String getPassword();

    public String getFingerPrint();
}
