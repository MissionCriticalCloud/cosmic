package com.cloud.user;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.InternalIdentity;

public interface SSHKeyPair extends ControlledEntity, InternalIdentity {

    /**
     * @return The given name of the key pair.
     */
    public String getName();

    /**
     * @return The finger print of the public key.
     */
    public String getFingerprint();

    /**
     * @return The public key of the key pair.
     */
    public String getPublicKey();

    /**
     * @return The private key of the key pair.
     */
    public String getPrivateKey();
}
