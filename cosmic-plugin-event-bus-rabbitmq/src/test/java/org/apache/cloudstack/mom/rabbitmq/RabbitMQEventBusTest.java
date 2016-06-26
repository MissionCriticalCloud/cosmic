package org.apache.cloudstack.mom.rabbitmq;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class RabbitMQEventBusTest {

    @Test
    public void testConstructorHasNoDependencies() throws Exception {
        assertThat(new RabbitMQEventBus(), notNullValue());
    }
}
