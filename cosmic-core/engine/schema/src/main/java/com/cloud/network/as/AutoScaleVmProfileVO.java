package com.cloud.network.as;

import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "autoscale_vmprofiles")
@Inheritance(strategy = InheritanceType.JOINED)
public class AutoScaleVmProfileVO implements AutoScaleVmProfile, Identity, InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected long id;

    @Column(name = "uuid")
    protected String uuid;

    @Column(name = "zone_id", updatable = true, nullable = false)
    protected Long zoneId;
    @Column(name = GenericDao.REMOVED_COLUMN)
    protected Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Column(name = "domain_id", updatable = true)
    private long domainId;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "autoscale_user_id")
    private long autoscaleUserId;
    @Column(name = "service_offering_id", updatable = true, nullable = false)
    private Long serviceOfferingId;
    @Column(name = "template_id", updatable = true, nullable = false, length = 17)
    private Long templateId;
    @Column(name = "other_deploy_params", updatable = true, length = 1024)
    private String otherDeployParams;
    @Column(name = "destroy_vm_grace_period", updatable = true)
    private Integer destroyVmGraceperiod = NetUtils.DEFAULT_AUTOSCALE_VM_DESTROY_TIME;
    @Column(name = "counter_params", updatable = true)
    private String counterParams;

    public AutoScaleVmProfileVO() {
    }

    public AutoScaleVmProfileVO(final long zoneId, final long domainId, final long accountId, final long serviceOfferingId, final long templateId, final String
            otherDeployParams, final Map counterParamList,
                                final Integer destroyVmGraceperiod, final long autoscaleUserId) {
        uuid = UUID.randomUUID().toString();
        this.zoneId = zoneId;
        this.domainId = domainId;
        this.accountId = accountId;
        this.serviceOfferingId = serviceOfferingId;
        this.templateId = templateId;
        this.otherDeployParams = otherDeployParams;
        this.autoscaleUserId = autoscaleUserId;
        if (destroyVmGraceperiod != null) {
            this.destroyVmGraceperiod = destroyVmGraceperiod;
        }
        setCounterParamsForUpdate(counterParamList);
    }

    public void setCounterParamsForUpdate(final Map counterParamList) {
        final StringBuilder sb = new StringBuilder("");
        boolean isFirstParam = true;
        if (counterParamList != null) {
            final Iterator<HashMap<String, String>> iter = counterParamList.values().iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> paramKVpair = iter.next();
                if (!isFirstParam) {
                    sb.append("&");
                }
                final String paramName = paramKVpair.get("name");
                final String paramValue = paramKVpair.get("value");
                sb.append(paramName + "=" + paramValue);
                isFirstParam = false;
            }
        }
        /*
         * setCounterParams(String counterParam)'s String param is caught by UpdateBuilder and stored in an internal
         * list.
         * Which is used later to update the db. The variables in a VO object is not used to update the db.
         * Hence calling the function which is intercepted.
         */
        setCounterParams(sb.toString());
    }

    @Override
    public String toString() {
        return new StringBuilder("AutoScaleVMProfileVO[").append("id").append(id).append("-").append("templateId").append("-").append(templateId).append("]").toString();
    }

    public void setAutoscaleUserId(final long autoscaleUserId) {
        this.autoscaleUserId = autoscaleUserId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final Long templateId) {
        this.templateId = templateId;
    }

    @Override
    public String getOtherDeployParams() {
        return otherDeployParams;
    }

    public void setOtherDeployParams(final String otherDeployParams) {
        this.otherDeployParams = otherDeployParams;
    }

    @Override
    public List<Pair<String, String>> getCounterParams() {
        final List<Pair<String, String>> paramsList = new ArrayList<>();
        if (counterParams != null) {
            final String[] params = counterParams.split("[=&]");
            for (int i = 0; i < (params.length - 1); i = i + 2) {
                paramsList.add(new Pair<>(params[i], params[i + 1]));
            }
        }
        return paramsList;
    }

    public void setCounterParams(final String counterParam) {
        counterParams = counterParam;
    }

    @Override
    public Integer getDestroyVmGraceperiod() {
        return destroyVmGraceperiod;
    }

    public void setDestroyVmGraceperiod(final Integer destroyVmGraceperiod) {
        this.destroyVmGraceperiod = destroyVmGraceperiod;
    }

    @Override
    public long getAutoScaleUserId() {
        return autoscaleUserId;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return AutoScaleVmProfile.class;
    }
}
