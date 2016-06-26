package org.apache.cloudstack.api.response;

import com.cloud.network.rules.StickinessPolicy;
import com.cloud.serializer.Param;
import com.cloud.utils.Pair;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class LBStickinessPolicyResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the LB Stickiness policy ID")
    private String id;

    @SerializedName("name")
    @Param(description = "the name of the Stickiness policy")
    private String name;

    @SerializedName("methodname")
    @Param(description = "the method name of the Stickiness policy")
    private final String methodName;

    @SerializedName("description")
    @Param(description = "the description of the Stickiness policy")
    private String description;

    @SerializedName("state")
    @Param(description = "the state of the policy")
    private String state;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is policy for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    // FIXME : if prams with the same name exists more then once then value are concatinated with ":" as delimitor .
    // Reason: Map does not support duplicate keys, need to look for the alernate data structure
    // Example: <params>{indirect=null, name=testcookie, nocache=null, domain=www.yahoo.com:www.google.com, postonly=null}</params>
    // in the above there are two domains with values www.yahoo.com and www.google.com
    @SerializedName("params")
    @Param(description = "the params of the policy")
    private final Map<String, String> params;

    public LBStickinessPolicyResponse(final StickinessPolicy stickinesspolicy) {
        this.name = stickinesspolicy.getName();
        final List<Pair<String, String>> paramsList = stickinesspolicy.getParams();
        this.methodName = stickinesspolicy.getMethodName();
        this.description = stickinesspolicy.getDescription();
        this.forDisplay = stickinesspolicy.isDisplay();
        if (stickinesspolicy.isRevoke()) {
            this.setState("Revoked");
        }
        if (stickinesspolicy.getUuid() != null) {
            setId(stickinesspolicy.getUuid());
        }

        /* Get the param and values from the database and fill the response object
         *  The following loop is to
         *    1) convert from List of Pair<String,String> to Map<String, String>
         *    2)  combine all params with name with ":" , currently we have one param called "domain" that can appear multiple times.
         * */

        final Map<String, String> tempParamList = new HashMap<>();
        for (final Pair<String, String> paramKV : paramsList) {
            final String key = paramKV.first();
            final String value = paramKV.second();
            final StringBuilder sb = new StringBuilder();
            sb.append(value);
            if (tempParamList.get(key) != null) {
                sb.append(":").append(tempParamList.get(key));
            }

            tempParamList.put(key, sb.toString());
        }

        this.params = tempParamList;
        setObjectName("stickinesspolicy");
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
