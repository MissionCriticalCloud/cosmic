//

//

package com.cloud.utils;

import org.springframework.stereotype.Component;

@Component
public class DummyImpl implements DummyInterface {

    @Override
    public void foo() {
        System.out.println("Basic foo implementation");
    }
}
