package com.cloud.utils.db;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB
public class DbAnnotatedBase {
    private static final Logger s_logger = LoggerFactory.getLogger(DbAnnotatedBase.class);

    @Inject
    DummyComponent _dummy;

    @PostConstruct
    public void initTest() {
        Assert.assertTrue(true);
    }

    public void MethodWithClassDbAnnotated() {
        s_logger.info("called");
        _dummy.sayHello();
    }
}
