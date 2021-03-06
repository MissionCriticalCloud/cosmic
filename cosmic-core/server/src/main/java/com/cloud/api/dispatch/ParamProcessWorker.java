package com.cloud.api.dispatch;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.cloud.acl.SecurityChecker;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.api.EntityReference;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.admin.resource.ArchiveAlertsCmd;
import com.cloud.api.command.admin.resource.DeleteAlertsCmd;
import com.cloud.api.command.user.event.ArchiveEventsCmd;
import com.cloud.api.command.user.event.DeleteEventsCmd;
import com.cloud.api.command.user.event.ListEventsCmd;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.DateUtil;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamProcessWorker implements DispatchWorker {

    private static final Logger s_logger = LoggerFactory.getLogger(ParamProcessWorker.class.getName());
    public final DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
    public final DateFormat newInputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Inject
    protected AccountManager _accountMgr;

    @Inject
    protected EntityManager _entityMgr;

    List<SecurityChecker> _secChecker;

    public List<SecurityChecker> getSecChecker() {
        return _secChecker;
    }

    @Inject
    public void setSecChecker(final List<SecurityChecker> secChecker) {
        _secChecker = secChecker;
    }

    @Override
    public void handle(final DispatchTask task) {
        processParameters(task.getCmd(), task.getParams());
    }

    public void processParameters(final BaseCmd cmd, final Map params) {
        final Map<Object, AccessType> entitiesToAccess = new HashMap<>();

        final List<Field> cmdFields = cmd.getParamFields();

        for (final Field field : cmdFields) {
            final Parameter parameterAnnotation = field.getAnnotation(Parameter.class);
            final Object paramObj = params.get(parameterAnnotation.name());
            if (paramObj == null) {
                if (parameterAnnotation.required()) {
                    throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "Unable to execute API command " +
                            cmd.getCommandName().substring(0, cmd.getCommandName().length() - 8) +
                            " due to missing parameter " + parameterAnnotation.name());
                }
                continue;
            }

            // marshall the parameter into the correct type and set the field value
            try {
                setFieldValue(field, cmd, paramObj, parameterAnnotation);
            } catch (final IllegalArgumentException argEx) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Unable to execute API command " + cmd.getCommandName() + " due to invalid value " + paramObj + " for parameter " +
                            parameterAnnotation.name());
                }
                throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "Unable to execute API command " +
                        cmd.getCommandName().substring(0, cmd.getCommandName().length() - 8) + " due to invalid value " + paramObj + " for parameter " +
                        parameterAnnotation.name());
            } catch (final ParseException parseEx) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Invalid date parameter " + paramObj + " passed to command " + cmd.getCommandName().substring(0, cmd.getCommandName().length() - 8));
                }
                throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "Unable to parse date " + paramObj + " for command " +
                        cmd.getCommandName().substring(0, cmd.getCommandName().length() - 8) + ", please pass dates in the format mentioned in the api documentation");
            } catch (final InvalidParameterValueException invEx) {
                throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "Unable to execute API command " +
                        cmd.getCommandName().substring(0, cmd.getCommandName().length() - 8) + " due to invalid value. " + invEx.getMessage());
            } catch (final CloudRuntimeException cloudEx) {
                s_logger.error("CloudRuntimeException", cloudEx);
                // FIXME: Better error message? This only happens if the API command is not executable, which typically
                //means
                // there was
                // and IllegalAccessException setting one of the parameters.
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Internal error executing API command " +
                        cmd.getCommandName().substring(0, cmd.getCommandName().length() - 8));
            }

            //check access on the resource this field points to
            try {
                final ACL checkAccess = field.getAnnotation(ACL.class);
                final CommandType fieldType = parameterAnnotation.type();

                if (checkAccess != null) {
                    // Verify that caller can perform actions in behalf of vm
                    // owner acumulate all Controlled Entities together.
                    // parse the array of resource types and in case of map
                    // check access on key or value or both as specified in @acl
                    // implement external dao for classes that need findByName
                    // for maps, specify access to be checkd on key or value.
                    // Find the controlled entity DBid by uuid

                    if (parameterAnnotation.entityType() != null && parameterAnnotation.entityType().length > 0
                            && parameterAnnotation.entityType()[0].getAnnotation(EntityReference.class) != null) {
                        final Class<?>[] entityList = parameterAnnotation.entityType()[0].getAnnotation(EntityReference.class).value();

                        // Check if the parameter type is a single
                        // Id or list of id's/name's
                        switch (fieldType) {
                            case LIST:
                                final CommandType listType = parameterAnnotation.collectionType();
                                switch (listType) {
                                    case LONG:
                                    case UUID:
                                        final List<Long> listParam = (List<Long>) field.get(cmd);
                                        for (final Long entityId : listParam) {
                                            for (final Class entity : entityList) {
                                                final Object entityObj = _entityMgr.findById(entity, entityId);
                                                if (entityObj != null) {
                                                    entitiesToAccess.put(entityObj, checkAccess.accessType());
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    /*
                                     * case STRING: List<String> listParam = new
                                     * ArrayList<String>(); listParam =
                                     * (List)field.get(cmd); for(String entityName:
                                     * listParam){ ControlledEntity entityObj =
                                     * (ControlledEntity )daoClassInstance(entityId);
                                     * entitiesToAccess.add(entityObj); } break;
                                     */
                                    default:
                                        break;
                                }
                                break;
                            case LONG:
                            case UUID:
                                for (final Class entity : entityList) {
                                    final Object entityObj = _entityMgr.findById(entity, (Long) field.get(cmd));
                                    if (entityObj != null) {
                                        entitiesToAccess.put(entityObj, checkAccess.accessType());
                                        break;
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (final IllegalArgumentException e) {
                s_logger.error("Error initializing command " + cmd.getCommandName() + ", field " + field.getName() + " is not accessible.");
                throw new CloudRuntimeException("Internal error initializing parameters for command " + cmd.getCommandName() + " [field " + field.getName() +
                        " is not accessible]");
            } catch (final IllegalAccessException e) {
                s_logger.error("Error initializing command " + cmd.getCommandName() + ", field " + field.getName() + " is not accessible.");
                throw new CloudRuntimeException("Internal error initializing parameters for command " + cmd.getCommandName() + " [field " + field.getName() +
                        " is not accessible]");
            }
        }

        doAccessChecks(cmd, entitiesToAccess);
    }

    private void setFieldValue(final Field field, final BaseCmd cmdObj, final Object paramObj, final Parameter annotation) throws IllegalArgumentException, ParseException {
        try {
            field.setAccessible(true);
            final CommandType fieldType = annotation.type();
            switch (fieldType) {
                case BOOLEAN:
                    field.set(cmdObj, Boolean.valueOf(paramObj.toString()));
                    break;
                case DATE:
                    // This piece of code is for maintaining backward compatibility
                    // and support both the date formats(Bug 9724)
                    if (cmdObj instanceof ListEventsCmd || cmdObj instanceof DeleteEventsCmd || cmdObj instanceof ArchiveEventsCmd ||
                            cmdObj instanceof ArchiveAlertsCmd || cmdObj instanceof DeleteAlertsCmd) {
                        final boolean isObjInNewDateFormat = isObjInNewDateFormat(paramObj.toString());
                        if (isObjInNewDateFormat) {
                            final DateFormat newFormat = newInputFormat;
                            synchronized (newFormat) {
                                field.set(cmdObj, newFormat.parse(paramObj.toString()));
                            }
                        } else {
                            final DateFormat format = inputFormat;
                            synchronized (format) {
                                Date date = format.parse(paramObj.toString());
                                if (field.getName().equals("startDate")) {
                                    date = messageDate(date, 0, 0, 0);
                                } else if (field.getName().equals("endDate")) {
                                    date = messageDate(date, 23, 59, 59);
                                }
                                field.set(cmdObj, date);
                            }
                        }
                    } else {
                        final DateFormat format = inputFormat;
                        synchronized (format) {
                            format.setLenient(false);
                            field.set(cmdObj, format.parse(paramObj.toString()));
                        }
                    }
                    break;
                case FLOAT:
                    // Assuming that the parameters have been checked for required before now,
                    // we ignore blank or null values and defer to the command to set a default
                    // value for optional parameters ...
                    if (paramObj != null && isNotBlank(paramObj.toString())) {
                        field.set(cmdObj, Float.valueOf(paramObj.toString()));
                    }
                    break;
                case DOUBLE:
                    // Assuming that the parameters have been checked for required before now,
                    // we ignore blank or null values and defer to the command to set a default
                    // value for optional parameters ...
                    if (paramObj != null && isNotBlank(paramObj.toString())) {
                        field.set(cmdObj, Double.valueOf(paramObj.toString()));
                    }
                    break;
                case INTEGER:
                    // Assuming that the parameters have been checked for required before now,
                    // we ignore blank or null values and defer to the command to set a default
                    // value for optional parameters ...
                    if (paramObj != null && isNotBlank(paramObj.toString())) {
                        field.set(cmdObj, Integer.valueOf(paramObj.toString()));
                    }
                    break;
                case LIST:
                    final List listParam = new ArrayList();
                    final StringTokenizer st = new StringTokenizer(paramObj.toString(), ",");
                    while (st.hasMoreTokens()) {
                        final String token = st.nextToken();
                        final CommandType listType = annotation.collectionType();
                        switch (listType) {
                            case INTEGER:
                                listParam.add(Integer.valueOf(token));
                                break;
                            case UUID:
                                if (token.isEmpty()) {
                                    break;
                                }
                                final Long internalId = translateUuidToInternalId(token, annotation);
                                listParam.add(internalId);
                                break;
                            case LONG: {
                                listParam.add(Long.valueOf(token));
                            }
                            break;
                            case SHORT:
                                listParam.add(Short.valueOf(token));
                                break;
                            case STRING:
                                listParam.add(token);
                                break;
                        }
                    }
                    field.set(cmdObj, listParam);
                    break;
                case UUID:
                    final Long internalId = translateUuidToInternalId(paramObj.toString(), annotation);
                    field.set(cmdObj, internalId);
                    break;
                case LONG:
                    field.set(cmdObj, Long.valueOf(paramObj.toString()));
                    break;
                case SHORT:
                    field.set(cmdObj, Short.valueOf(paramObj.toString()));
                    break;
                case STRING:
                    if ((paramObj != null)) {
                        if (paramObj.toString().length() > annotation.length()) {
                            s_logger.error("Value greater than max allowed length " + annotation.length() + " for param: " + field.getName());
                            throw new InvalidParameterValueException("Value greater than max allowed length " + annotation.length() + " for param: " + field.getName());
                        } else {
                            field.set(cmdObj, paramObj.toString());
                        }
                    }
                    break;
                case TZDATE:
                    field.set(cmdObj, DateUtil.parseTZDateString(paramObj.toString()));
                    break;
                case MAP:
                default:
                    field.set(cmdObj, paramObj);
                    break;
            }
        } catch (final IllegalAccessException ex) {
            s_logger.error("Error initializing command " + cmdObj.getCommandName() + ", field " + field.getName() + " is not accessible.");
            throw new CloudRuntimeException("Internal error initializing parameters for command " + cmdObj.getCommandName() + " [field " + field.getName() +
                    " is not accessible]");
        }
    }

    private void doAccessChecks(final BaseCmd cmd, final Map<Object, AccessType> entitiesToAccess) {
        final Account caller = CallContext.current().getCallingAccount();
        // due to deleteAccount design flaw CLOUDSTACK-6588, we should still include those removed account as well to clean up leftover resources from that account
        final Account owner = _accountMgr.getAccount(cmd.getEntityOwnerId());

        if (cmd instanceof BaseAsyncCreateCmd) {
            // check that caller can access the owner account.
            _accountMgr.checkAccess(caller, null, false, owner);
        }

        if (!entitiesToAccess.isEmpty()) {
            // check that caller can access the owner account.
            _accountMgr.checkAccess(caller, null, false, owner);
            for (final Map.Entry<Object, AccessType> entry : entitiesToAccess.entrySet()) {
                final Object entity = entry.getKey();
                if (entity instanceof ControlledEntity) {
                    _accountMgr.checkAccess(caller, entry.getValue(), true, (ControlledEntity) entity);
                }
            }
        }
    }

    private boolean isObjInNewDateFormat(final String string) {
        final Matcher matcher = BaseCmd.newInputDateFormat.matcher(string);
        return matcher.matches();
    }

    private Date messageDate(final Date date, final int hourOfDay, final int minute, final int second) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        return cal.getTime();
    }

    private Long translateUuidToInternalId(final String uuid, final Parameter annotation) {
        if (uuid.equals("-1")) {
            // FIXME: This is to handle a lot of hardcoded special cases where -1 is sent
            // APITODO: Find and get rid of all hardcoded params in API Cmds and service layer
            return -1L;
        }
        Long internalId = null;
        // Match against Java's UUID regex to check if input is uuid string
        final boolean isUuid = uuid.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        // Enforce that it's uuid
        if (!isUuid) {
            throw new InvalidParameterValueException("Invalid parameter value: Not a uuid: " + uuid);
        }

        // There may be multiple entities defined on the @EntityReference of a Response.class
        // UUID CommandType would expect only one entityType, so use the first entityType
        final Class<?>[] entities = annotation.entityType()[0].getAnnotation(EntityReference.class).value();

        // Go through each entity which is an interface to a VO class and get a VO object
        // Try to getId() for the object using reflection, break on first non-null value
        for (final Class<?> entity : entities) {
            // For backward compatibility, we search within removed entities and let service layer deal
            // with removed ones, return empty response or error
            final Object objVO = _entityMgr.findByUuidIncludingRemoved(entity, uuid);
            if (objVO == null) {
                continue;
            }
            // Invoke the getId method, get the internal long ID
            // If that fails hide exceptions as the uuid may not exist                                         s
            try {
                internalId = ((InternalIdentity) objVO).getId();
            } catch (final IllegalArgumentException e) {
            } catch (final NullPointerException e) {
            }
            // Return on first non-null Id for the uuid entity
            if (internalId != null) {
                CallContext.current().putContextParameter(entity, uuid);
                break;
            }
        }
        if (internalId == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Object entity uuid = " + uuid + " does not exist in the database.");
            }
            throw new InvalidParameterValueException("Invalid parameter " + annotation.name() + " value=" + uuid +
                    " due to incorrect long value format, or entity does not exist or due to incorrect parameter annotation for the field in api cmd class.");
        }
        return internalId;
    }
}
