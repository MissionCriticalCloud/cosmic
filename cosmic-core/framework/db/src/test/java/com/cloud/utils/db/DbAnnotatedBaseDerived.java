package com.cloud.utils.db;

import org.springframework.stereotype.Component;

@Component
public class DbAnnotatedBaseDerived extends DbAnnotatedBase {

    @DB
    public void DbAnnotatedMethod() {
    }
}
