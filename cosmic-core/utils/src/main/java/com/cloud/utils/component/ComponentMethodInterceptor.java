//

//

package com.cloud.utils.component;

import java.lang.reflect.Method;

public interface ComponentMethodInterceptor {
    boolean needToIntercept(Method method);

    Object interceptStart(Method method, Object target);

    void interceptComplete(Method method, Object target, Object objReturnedInInterceptStart);

    void interceptException(Method method, Object target, Object objReturnedInInterceptStart);
}
