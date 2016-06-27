package com.cloud.network.rules;

public class RuleApplierWrapper<T> {

    private final T _ruleType;

    public RuleApplierWrapper(final T ruleApplier) {
        this._ruleType = ruleApplier;
    }

    public T getRuleType() {
        return _ruleType;
    }
}
