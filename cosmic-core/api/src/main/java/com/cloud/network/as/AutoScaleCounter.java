package com.cloud.network.as;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class AutoScaleCounter {
    @SerializedName("methodname")
    private final String _counterName;
    @SerializedName("paramlist")
    private List<AutoScaleCounterParam> _paramList;

    public AutoScaleCounter(final AutoScaleCounterType methodType) {
        this._counterName = methodType.getName();
        this._paramList = new ArrayList<>(1);
    }

    public void addParam(final String name, final Boolean required, final String description, final Boolean isFlag) {
        final AutoScaleCounterParam param = new AutoScaleCounterParam(name, required, description, isFlag);
        _paramList.add(param);
        return;
    }

    public String getName() {
        return _counterName;
    }

    public List<AutoScaleCounterParam> getParamList() {
        return _paramList;
    }

    public void setParamList(final List<AutoScaleCounterParam> paramList) {
        this._paramList = paramList;
    }

    public static class AutoScaleCounterType {
        public static final AutoScaleCounterType Snmp = new AutoScaleCounterType("snmp");
        public static final AutoScaleCounterType Cpu = new AutoScaleCounterType("cpu");
        public static final AutoScaleCounterType Memory = new AutoScaleCounterType("memory");
        private final String _name;

        public AutoScaleCounterType(final String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
    }

    public class AutoScaleCounterParam {
        @SerializedName("paramname")
        private String _paramName;

        @SerializedName("required")
        private Boolean _required;

        @SerializedName("isflag")
        private Boolean _isFlag;

        @SerializedName("description")
        private String _description;

        public AutoScaleCounterParam(final String name, final Boolean required, final String description, final Boolean flag) {
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
