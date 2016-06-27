package com.cloud.api;

import com.cloud.serializer.Param;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ResponseObject;
import org.apache.cloudstack.context.CallContext;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;

/**
 * The ApiResonseGsonHelper is different from ApiGsonHelper - it registers one more adapter for String type required for api response encoding
 */
public class ApiResponseGsonHelper {
    private static final GsonBuilder s_gBuilder;
    private static final GsonBuilder s_gLogBuilder;

    static {
        s_gBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        s_gBuilder.setVersion(1.3);
        s_gBuilder.registerTypeAdapter(ResponseObject.class, new ResponseObjectTypeAdapter());
        s_gBuilder.registerTypeAdapter(String.class, new EncodedStringTypeAdapter());
        s_gBuilder.setExclusionStrategies(new ApiResponseExclusionStrategy());

        s_gLogBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        s_gLogBuilder.setVersion(1.3);
        s_gLogBuilder.registerTypeAdapter(ResponseObject.class, new ResponseObjectTypeAdapter());
        s_gLogBuilder.registerTypeAdapter(String.class, new EncodedStringTypeAdapter());
        s_gLogBuilder.setExclusionStrategies(new LogExclusionStrategy());
    }

    public static GsonBuilder getBuilder() {
        return s_gBuilder;
    }

    public static GsonBuilder getLogBuilder() {
        return s_gLogBuilder;
    }

    private static class ApiResponseExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipField(final FieldAttributes f) {
            final Param param = f.getAnnotation(Param.class);
            if (param != null) {
                final RoleType[] allowedRoles = param.authorized();
                if (allowedRoles.length > 0) {
                    boolean permittedParameter = false;
                    final Account caller = CallContext.current().getCallingAccount();
                    for (final RoleType allowedRole : allowedRoles) {
                        if (allowedRole.getValue() == caller.getType()) {
                            permittedParameter = true;
                            break;
                        }
                    }
                    if (!permittedParameter) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean shouldSkipClass(final Class<?> arg0) {
            return false;
        }
    }

    private static class LogExclusionStrategy extends ApiResponseExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipField(final FieldAttributes f) {
            final Param param = f.getAnnotation(Param.class);
            boolean skip = (param != null && param.isSensitive());
            if (!skip) {
                skip = super.shouldSkipField(f);
            }
            return skip;
        }

        public boolean shouldSkipClass(final Class<?> arg0) {
            return false;
        }
    }
}
