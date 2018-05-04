package com.cloud.network.lb;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

public interface SslCert extends InternalIdentity, Identity, ControlledEntity {

    public String getCertificate();

    public String getKey();

    public String getChain();

    public String getPassword();

    public String getFingerPrint();
}
