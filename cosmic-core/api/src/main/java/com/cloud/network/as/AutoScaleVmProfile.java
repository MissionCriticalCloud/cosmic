package com.cloud.network.as;

import com.cloud.utils.Pair;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.List;

/**
 * AutoScaleVmProfile
 */
public interface AutoScaleVmProfile extends ControlledEntity, InternalIdentity, Displayable {

    @Override
    public long getId();

    public String getUuid();

    public Long getZoneId();

    public Long getServiceOfferingId();

    public Long getTemplateId();

    public String getOtherDeployParams();

    List<Pair<String, String>> getCounterParams();

    public Integer getDestroyVmGraceperiod();

    public long getAutoScaleUserId();

    @Override
    boolean isDisplay();
}
