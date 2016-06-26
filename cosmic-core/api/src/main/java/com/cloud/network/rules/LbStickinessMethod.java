package com.cloud.network.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class LbStickinessMethod {
    @SerializedName("methodname")
    private final String _methodName;
    @SerializedName("paramlist")
    private List<LbStickinessMethodParam> _paramList;
    @SerializedName("description")
    private String _description;

    public LbStickinessMethod(final StickinessMethodType methodType, final String description) {
        this._methodName = methodType.getName();
        this._description = description;
        this._paramList = new ArrayList<>(1);
    }

    public void addParam(final String name, final Boolean required, final String description, final Boolean isFlag) {
        /* FIXME : UI is breaking if the capability string length is larger , temporarily description is commented out */
        // LbStickinessMethodParam param = new LbStickinessMethodParam(name, required, description);
        final LbStickinessMethodParam param = new LbStickinessMethodParam(name, required, " ", isFlag);
        _paramList.add(param);
        return;
    }

    public String getMethodName() {
        return _methodName;
    }

    public List<LbStickinessMethodParam> getParamList() {
        return _paramList;
    }

    public void setParamList(final List<LbStickinessMethodParam> paramList) {
        this._paramList = paramList;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        /* FIXME : UI is breaking if the capability string length is larger , temporarily description is commented out */
        //this.description = description;
        this._description = " ";
    }

    public static class StickinessMethodType {
        public static final StickinessMethodType LBCookieBased = new StickinessMethodType("LbCookie");
        public static final StickinessMethodType AppCookieBased = new StickinessMethodType("AppCookie");
        public static final StickinessMethodType SourceBased = new StickinessMethodType("SourceBased");
        private final String _name;

        public StickinessMethodType(final String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
    }

    public class LbStickinessMethodParam {
        @SerializedName("paramname")
        private String _paramName;

        @SerializedName("required")
        private Boolean _required;

        @SerializedName("isflag")
        private Boolean _isFlag;

        @SerializedName("description")
        private String _description;

        public LbStickinessMethodParam(final String name, final Boolean required, final String description, final Boolean flag) {
            this._paramName = name;
            this._required = required;
            this._description = description;
            this._isFlag = flag;
        }

        public String getParamName() {
            return _paramName;
        }

        public void setParamName(final String paramName) {
            this._paramName = paramName;
        }

        public Boolean getIsflag() {
            return _isFlag;
        }

        public void setIsflag(final Boolean isFlag) {
            this._isFlag = isFlag;
        }

        public Boolean getRequired() {
            return _required;
        }

        public void setRequired(final Boolean required) {
            this._required = required;
        }

        public String getDescription() {
            return _description;
        }

        public void setDescription(final String description) {
            this._description = description;
        }
    }
}
