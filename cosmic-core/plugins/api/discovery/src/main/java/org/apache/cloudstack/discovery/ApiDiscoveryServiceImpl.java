package org.apache.cloudstack.discovery;

import com.cloud.serializer.Param;
import com.cloud.user.User;
import com.cloud.utils.ReflectUtil;
import com.cloud.utils.StringUtils;
import com.cloud.utils.component.ComponentLifecycleBase;
import com.cloud.utils.component.PluggableService;
import org.apache.cloudstack.acl.APIChecker;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.command.user.discovery.ListApisCmd;
import org.apache.cloudstack.api.response.ApiDiscoveryResponse;
import org.apache.cloudstack.api.response.ApiParameterResponse;
import org.apache.cloudstack.api.response.ApiResponseResponse;
import org.apache.cloudstack.api.response.ListResponse;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApiDiscoveryServiceImpl extends ComponentLifecycleBase implements ApiDiscoveryService {
    private static final Logger s_logger = LoggerFactory.getLogger(ApiDiscoveryServiceImpl.class);
    private static Map<String, ApiDiscoveryResponse> s_apiNameDiscoveryResponseMap = null;
    List<APIChecker> _apiAccessCheckers = null;
    List<PluggableService> _services = null;

    protected ApiDiscoveryServiceImpl() {
        super();
    }

    @Override
    public boolean start() {
        if (s_apiNameDiscoveryResponseMap == null) {
            final long startTime = System.nanoTime();
            s_apiNameDiscoveryResponseMap = new HashMap<>();
            final Set<Class<?>> cmdClasses = new HashSet<>();
            for (final PluggableService service : _services) {
                s_logger.debug(String.format("getting api commands of service: %s", service.getClass().getName()));
                cmdClasses.addAll(service.getCommands());
            }
            cmdClasses.addAll(this.getCommands());
            cacheResponseMap(cmdClasses);
            final long endTime = System.nanoTime();
            s_logger.info("Api Discovery Service: Annotation, docstrings, api relation graph processed in " + (endTime - startTime) / 1000000.0 + " ms");
        }

        return true;
    }

    protected Map<String, List<String>> cacheResponseMap(final Set<Class<?>> cmdClasses) {
        final Map<String, List<String>> responseApiNameListMap = new HashMap<>();

        for (final Class<?> cmdClass : cmdClasses) {
            APICommand apiCmdAnnotation = cmdClass.getAnnotation(APICommand.class);
            if (apiCmdAnnotation == null) {
                apiCmdAnnotation = cmdClass.getSuperclass().getAnnotation(APICommand.class);
            }
            if (apiCmdAnnotation == null || !apiCmdAnnotation.includeInApiDoc() || apiCmdAnnotation.name().isEmpty()) {
                continue;
            }

            final String apiName = apiCmdAnnotation.name();
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Found api: " + apiName);
            }
            final ApiDiscoveryResponse response = getCmdRequestMap(cmdClass, apiCmdAnnotation);

            final String responseName = apiCmdAnnotation.responseObject().getName();
            if (!responseName.contains("SuccessResponse")) {
                if (!responseApiNameListMap.containsKey(responseName)) {
                    responseApiNameListMap.put(responseName, new ArrayList<>());
                }
                responseApiNameListMap.get(responseName).add(apiName);
            }
            response.setRelated(responseName);

            final Field[] responseFields = apiCmdAnnotation.responseObject().getDeclaredFields();
            for (final Field responseField : responseFields) {
                final ApiResponseResponse responseResponse = getFieldResponseMap(responseField);
                response.addApiResponse(responseResponse);
            }

            response.setObjectName("api");
            s_apiNameDiscoveryResponseMap.put(apiName, response);
        }

        for (final String apiName : s_apiNameDiscoveryResponseMap.keySet()) {
            final ApiDiscoveryResponse response = s_apiNameDiscoveryResponseMap.get(apiName);
            final Set<ApiParameterResponse> processedParams = new HashSet<>();
            for (final ApiParameterResponse param : response.getParams()) {
                if (responseApiNameListMap.containsKey(param.getRelated())) {
                    final List<String> relatedApis = responseApiNameListMap.get(param.getRelated());
                    param.setRelated(StringUtils.join(relatedApis, ","));
                } else {
                    param.setRelated(null);
                }
                processedParams.add(param);
            }
            response.setParams(processedParams);

            if (responseApiNameListMap.containsKey(response.getRelated())) {
                final List<String> relatedApis = responseApiNameListMap.get(response.getRelated());
                relatedApis.remove(apiName);
                response.setRelated(StringUtils.join(relatedApis, ","));
            } else {
                response.setRelated(null);
            }
            s_apiNameDiscoveryResponseMap.put(apiName, response);
        }
        return responseApiNameListMap;
    }

    private ApiResponseResponse getFieldResponseMap(final Field responseField) {
        final ApiResponseResponse responseResponse = new ApiResponseResponse();
        final SerializedName serializedName = responseField.getAnnotation(SerializedName.class);
        final Param param = responseField.getAnnotation(Param.class);
        if (serializedName != null && param != null) {
            responseResponse.setName(serializedName.value());
            responseResponse.setDescription(param.description());
            responseResponse.setType(responseField.getType().getSimpleName().toLowerCase());
            //If response is not of primitive type - we have a nested entity
            final Class fieldClass = param.responseObject();
            if (fieldClass != null) {
                final Class<?> superClass = fieldClass.getSuperclass();
                if (superClass != null) {
                    final String superName = superClass.getName();
                    if (superName.equals(BaseResponse.class.getName())) {
                        final Field[] fields = fieldClass.getDeclaredFields();
                        for (final Field field : fields) {
                            final ApiResponseResponse innerResponse = getFieldResponseMap(field);
                            if (innerResponse != null) {
                                responseResponse.addApiResponse(innerResponse);
                            }
                        }
                    }
                }
            }
        }
        return responseResponse;
    }

    private ApiDiscoveryResponse getCmdRequestMap(final Class<?> cmdClass, final APICommand apiCmdAnnotation) {
        final String apiName = apiCmdAnnotation.name();
        final ApiDiscoveryResponse response = new ApiDiscoveryResponse();
        response.setName(apiName);
        response.setDescription(apiCmdAnnotation.description());
        if (!apiCmdAnnotation.since().isEmpty()) {
            response.setSince(apiCmdAnnotation.since());
        }

        final Set<Field> fields = ReflectUtil.getAllFieldsForClass(cmdClass, new Class<?>[]{BaseCmd.class, BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

        final boolean isAsync = ReflectUtil.isCmdClassAsync(cmdClass, new Class<?>[]{BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

        response.setAsync(isAsync);

        for (final Field field : fields) {
            final Parameter parameterAnnotation = field.getAnnotation(Parameter.class);
            if (parameterAnnotation != null && parameterAnnotation.expose() && parameterAnnotation.includeInApiDoc()) {

                final ApiParameterResponse paramResponse = new ApiParameterResponse();
                paramResponse.setName(parameterAnnotation.name());
                paramResponse.setDescription(parameterAnnotation.description());
                paramResponse.setType(parameterAnnotation.type().toString().toLowerCase());
                paramResponse.setLength(parameterAnnotation.length());
                paramResponse.setRequired(parameterAnnotation.required());
                if (!parameterAnnotation.since().isEmpty()) {
                    paramResponse.setSince(parameterAnnotation.since());
                }
                paramResponse.setRelated(parameterAnnotation.entityType()[0].getName());
                response.addParam(paramResponse);
            }
        }
        return response;
    }

    @Override
    public ListResponse<? extends BaseResponse> listApis(final User user, final String name) {
        final ListResponse<ApiDiscoveryResponse> response = new ListResponse<>();
        final List<ApiDiscoveryResponse> responseList = new ArrayList<>();

        if (user == null) {
            return null;
        }

        if (name != null) {
            if (!s_apiNameDiscoveryResponseMap.containsKey(name)) {
                return null;
            }

            for (final APIChecker apiChecker : _apiAccessCheckers) {
                try {
                    apiChecker.checkAccess(user, name);
                } catch (final Exception ex) {
                    s_logger.debug("API discovery access check failed for " + name + " with " + ex.getMessage());
                    return null;
                }
            }
            responseList.add(s_apiNameDiscoveryResponseMap.get(name));
        } else {
            for (final String apiName : s_apiNameDiscoveryResponseMap.keySet()) {
                boolean isAllowed = true;
                for (final APIChecker apiChecker : _apiAccessCheckers) {
                    try {
                        apiChecker.checkAccess(user, apiName);
                    } catch (final Exception ex) {
                        isAllowed = false;
                    }
                }
                if (isAllowed) {
                    responseList.add(s_apiNameDiscoveryResponseMap.get(apiName));
                }
            }
        }
        response.setResponses(responseList);
        return response;
    }

    @Override
    public List<Class<?>> getCommands() {
        final List<Class<?>> cmdList = new ArrayList<>();
        cmdList.add(ListApisCmd.class);
        return cmdList;
    }

    public List<APIChecker> getApiAccessCheckers() {
        return _apiAccessCheckers;
    }

    public void setApiAccessCheckers(final List<APIChecker> apiAccessCheckers) {
        this._apiAccessCheckers = apiAccessCheckers;
    }

    public List<PluggableService> getServices() {
        return _services;
    }

    @Inject
    public void setServices(final List<PluggableService> services) {
        this._services = services;
    }
}
