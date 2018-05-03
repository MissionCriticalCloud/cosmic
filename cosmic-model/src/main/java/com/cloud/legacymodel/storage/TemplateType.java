package com.cloud.legacymodel.storage;

public enum TemplateType {
    ROUTING, // Router template
    SYSTEM, /* routing, system vm template */
    BUILTIN, /* buildin template */
    PERHOST, /* every host has this template, don't need to install it in secondary storage */
    USER /* User supplied template/iso */
}
