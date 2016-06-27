package com.cloud.utils.db;

import com.cloud.utils.component.ComponentContext;

import javax.inject.Inject;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/com/cloud/utils/db/transactioncontextBuilderTest.xml")
public class TransactionContextBuilderTest {

    @Inject
    DbAnnotatedBaseDerived _derived;

    DbAnnotatedBase _base;

    @Inject
    List<DbAnnotatedBase> _list;

    @Test
    public void test() {
        // _derived.DbAnnotatedMethod();
        // _base.MethodWithClassDbAnnotated();

        // test @DB injection on dynamically constructed objects
        final DbAnnotatedBase base = ComponentContext.inject(new DbAnnotatedBase());
        base.MethodWithClassDbAnnotated();

        /*
                Map<String, DbAnnotatedBase> components = ComponentContext.getApplicationContext().getBeansOfType(DbAnnotatedBase.class);
                for(Map.Entry<String, DbAnnotatedBase> entry : components.entrySet()) {
                    System.out.println(entry.getKey());
                    entry.getValue().MethodWithClassDbAnnotated();
                }
        */
        for (final DbAnnotatedBase entry : _list) {
            entry.MethodWithClassDbAnnotated();
        }
    }
}
