package org.apache.cloudstack.spring.module.factory;

import javax.annotation.PostConstruct;

public class InitTest {

    public static boolean initted = false;

    @PostConstruct
    public void init() {
        setInitted(true);
    }

    public boolean isInitted() {
        return initted;
    }

    public void setInitted(final boolean initted) {
        InitTest.initted = initted;
    }
}
