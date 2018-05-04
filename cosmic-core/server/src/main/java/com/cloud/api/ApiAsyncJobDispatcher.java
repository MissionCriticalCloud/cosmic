package com.cloud.api;

import com.cloud.api.response.ExceptionResponse;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.framework.jobs.AsyncJob;
import com.cloud.framework.jobs.AsyncJobDispatcher;
import com.cloud.framework.jobs.AsyncJobManager;
import com.cloud.jobs.JobInfo;
import com.cloud.legacymodel.exceptions.CloudException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.ComponentContext;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiAsyncJobDispatcher extends AdapterBase implements AsyncJobDispatcher {
    private static final Logger s_logger = LoggerFactory.getLogger(ApiAsyncJobDispatcher.class);

    @Inject
    private ApiDispatcher _dispatcher;

    @Inject
    private AsyncJobManager _asyncJobMgr;
    @Inject
    private EntityManager _entityMgr;

    public ApiAsyncJobDispatcher() {
    }

    @Override
    public void runJob(final AsyncJob job) {
        BaseAsyncCmd cmdObj = null;
        try {
            final Class<?> cmdClass = Class.forName(job.getCmd());
            cmdObj = (BaseAsyncCmd) cmdClass.newInstance();
            cmdObj = ComponentContext.inject(cmdObj);
            cmdObj.configure();
            cmdObj.setJob(job);

            final Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            final Gson gson = ApiGsonHelper.getBuilder().create();
            final Map<String, String> params = gson.fromJson(job.getCmdInfo(), mapType);

            // whenever we deserialize, the UserContext needs to be updated
            final String userIdStr = params.get("ctxUserId");
            final String acctIdStr = params.get("ctxAccountId");
            final String contextDetails = params.get("ctxDetails");

            final Long userId;
            Account accountObject = null;

            if (cmdObj instanceof BaseAsyncCreateCmd) {
                final BaseAsyncCreateCmd create = (BaseAsyncCreateCmd) cmdObj;
                create.setEntityId(Long.parseLong(params.get("id")));
                create.setEntityUuid(params.get("uuid"));
            }

            User user = null;
            if (userIdStr != null) {
                userId = Long.parseLong(userIdStr);
                user = _entityMgr.findById(User.class, userId);
            }

            if (acctIdStr != null) {
                accountObject = _entityMgr.findById(Account.class, Long.parseLong(acctIdStr));
            }

            final CallContext ctx = CallContext.register(user, accountObject);
            if (contextDetails != null) {
                final Type objectMapType = new TypeToken<Map<Object, Object>>() {
                }.getType();
                ctx.putContextParameters((Map<Object, Object>) gson.fromJson(contextDetails, objectMapType));
            }

            try {
                // dispatch could ultimately queue the job
                _dispatcher.dispatch(cmdObj, params, true);

                // serialize this to the async job table
                _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.SUCCEEDED, 0, ApiSerializerHelper.toSerializedString(cmdObj.getResponseObject()));
            } catch (final InvalidParameterValueException | CloudException e) {
                throw new ServerApiException(ApiErrorCode.PARAM_ERROR, e.getMessage());
            } finally {
                CallContext.unregister();
            }
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | ServerApiException e) {
            final String errorMsg = ExceptionUtils.getRootCauseMessage(e);
            final int errorCode = ApiErrorCode.INTERNAL_ERROR.getHttpCode();

            final ExceptionResponse response = new ExceptionResponse();
            response.setErrorCode(errorCode);
            response.setErrorText(errorMsg);
            response.setResponseName((cmdObj == null) ? "unknowncommandresponse" : cmdObj.getCommandName());

            // FIXME:  setting resultCode to ApiErrorCode.INTERNAL_ERROR is not right, usually executors have their exception handling
            //         and we need to preserve that as much as possible here
            _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.FAILED, ApiErrorCode.INTERNAL_ERROR.getHttpCode(), ApiSerializerHelper.toSerializedString(response));
        }
    }
}
