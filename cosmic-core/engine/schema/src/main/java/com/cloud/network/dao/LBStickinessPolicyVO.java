package com.cloud.network.dao;

import com.cloud.network.rules.StickinessPolicy;
import com.cloud.utils.Pair;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = ("load_balancer_stickiness_policies"))
@PrimaryKeyJoinColumn(name = "load_balancer_id", referencedColumnName = "id")
public class LBStickinessPolicyVO implements StickinessPolicy {
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "load_balancer_id")
    private long loadBalancerId;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "method_name")
    private String methodName;
    @Column(name = "params")
    private String paramsInDB;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "revoke")
    private boolean revoke = false;

    protected LBStickinessPolicyVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    /*  get the params in Map format and converts in to string format and stores in DB
     *  paramsInDB represent the string stored in database :
     *  Format :  param1=value1&param2=value2&param3=value3&
     *  Example for App cookie method:  "name=cookapp&length=12&holdtime=3h" . Here 3 parameters name,length and holdtime with corresponding values.
     *  getParams function is used to get in List<Pair<string,String>> Format.
     *           - API response use Map format
     *           - In database plain String with DB_PARM_DELIMITER
     *           - rest of the code uses List<Pair<string,String>>
     */
    public LBStickinessPolicyVO(final long loadBalancerId, final String name, final String methodName, final Map paramList, final String description) {
        this.loadBalancerId = loadBalancerId;
        this.name = name;
        this.methodName = methodName;
        final StringBuilder sb = new StringBuilder("");

        if (paramList != null) {
            final Iterator<HashMap<String, String>> iter = paramList.values().iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> paramKVpair = iter.next();
                final String paramName = paramKVpair.get("name");
                final String paramValue = paramKVpair.get("value");
                sb.append(paramName + "=" + paramValue + "&");
            }
        }
        paramsInDB = sb.toString();
        this.description = description;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getLoadBalancerId() {
        return loadBalancerId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean isRevoke() {
        return revoke;
    }

    @Override
    public List<Pair<String, String>> getParams() {
        final List<Pair<String, String>> paramsList = new ArrayList<>();
        final String[] params = paramsInDB.split("[=&]");

        for (int i = 0; i < (params.length - 1); i = i + 2) {
            paramsList.add(new Pair<>(params[i], params[i + 1]));
        }
        return paramsList;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public void setRevoke(final boolean revoke) {
        this.revoke = revoke;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
