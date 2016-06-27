//

//

package com.cloud.utils.exception;

import com.cloud.utils.Pair;

import java.util.List;

public interface ErrorContext {

    ErrorContext add(Class<?> entity, String uuid);

    List<Pair<Class<?>, String>> getEntitiesInError();
}
