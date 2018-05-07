package com.cloud.legacymodel.network;

public class StickinessMethodType {
    public static final StickinessMethodType LBCookieBased = new StickinessMethodType("LbCookie");
    public static final StickinessMethodType AppCookieBased = new StickinessMethodType("AppCookie");
    public static final StickinessMethodType SourceBased = new StickinessMethodType("SourceBased");
    private final String _name;

    public StickinessMethodType(final String name) {
        this._name = name;
    }

    public String getName() {
        return this._name;
    }
}
