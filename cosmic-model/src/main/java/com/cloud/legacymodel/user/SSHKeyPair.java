package com.cloud.legacymodel.user;

import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

public interface SSHKeyPair extends ControlledEntity, InternalIdentity {

    String getName();

    String getFingerprint();

    String getPublicKey();

    String getPrivateKey();
}
