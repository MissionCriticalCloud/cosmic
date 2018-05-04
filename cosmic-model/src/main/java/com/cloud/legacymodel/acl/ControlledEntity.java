package com.cloud.legacymodel.acl;

import com.cloud.legacymodel.domain.PartOf;
import com.cloud.legacymodel.user.OwnedBy;

/**
 * ControlledEntity defines an object for which the access from an
 * access must inherit this interface.
 */
public interface ControlledEntity extends OwnedBy, PartOf {
    Class<?> getEntityType();

    enum ACLType {
        Account, Domain
    }
}
