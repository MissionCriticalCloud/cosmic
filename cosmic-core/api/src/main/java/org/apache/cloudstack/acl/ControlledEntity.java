package org.apache.cloudstack.acl;

import com.cloud.domain.PartOf;
import com.cloud.user.OwnedBy;

/**
 * ControlledEntity defines an object for which the access from an
 * access must inherit this interface.
 */
public interface ControlledEntity extends OwnedBy, PartOf {
    Class<?> getEntityType();

    public enum ACLType {
        Account, Domain
    }
}
