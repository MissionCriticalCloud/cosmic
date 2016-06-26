package org.apache.cloudstack.api;

import java.io.Serializable;

// This interface is a contract that getId() will give the internal
// ID of an entity which extends this interface
// Any class having an internal ID in db table/schema should extend this
// For example, all ControlledEntity, OwnedBy would have an internal ID

public interface InternalIdentity extends Serializable {
    long getId();
}
