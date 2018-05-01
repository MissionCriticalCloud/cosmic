package com.cloud.legacymodel.exceptions;

import java.util.HashMap;

/**
 * CSExceptionErrorCode lists the CloudStack error codes that correspond
 * to a each exception thrown by the CloudStack API.
 */

public class CSExceptionErrorCode {
    // Declare a hashmap of CloudStack Error Codes for Exceptions.
    protected static final HashMap<String, Integer> ExceptionErrorCodeMap;

    static {
        try {
            ExceptionErrorCodeMap = new HashMap<>();
            ExceptionErrorCodeMap.put("com.cloud.legacymodel.exceptions.CloudRuntimeException", 4250);
            ExceptionErrorCodeMap.put("com.cloud.legacymodel.exceptions.ExecutionException", 4260);
            ExceptionErrorCodeMap.put("com.cloud.legacymodel.exceptions.HypervisorVersionChangedException", 4265);
            ExceptionErrorCodeMap.put("com.cloud.exception.CloudException", 4275);
            ExceptionErrorCodeMap.put("com.cloud.exception.AccountLimitException", 4280);
            ExceptionErrorCodeMap.put("com.cloud.exception.AgentUnavailableException", 4285);
            ExceptionErrorCodeMap.put("com.cloud.exception.CloudAuthenticationException", 4290);
            ExceptionErrorCodeMap.put("com.cloud.exception.ConcurrentOperationException", 4300);
            ExceptionErrorCodeMap.put("com.cloud.exception.ConflictingNetworkSettingsException", 4305);
            ExceptionErrorCodeMap.put("com.cloud.exception.DiscoveredWithErrorException", 4310);
            ExceptionErrorCodeMap.put("com.cloud.exception.HAStateException", 4315);
            ExceptionErrorCodeMap.put("com.cloud.exception.InsufficientAddressCapacityException", 4320);
            ExceptionErrorCodeMap.put("com.cloud.exception.InsufficientCapacityException", 4325);
            ExceptionErrorCodeMap.put("com.cloud.exception.InsufficientNetworkCapacityException", 4330);
            ExceptionErrorCodeMap.put("com.cloud.exception.InsufficientVirtualNetworkCapacityException", 4331);
            ExceptionErrorCodeMap.put("com.cloud.exception.InsufficientServerCapacityException", 4335);
            ExceptionErrorCodeMap.put("com.cloud.exception.InsufficientStorageCapacityException", 4340);
            ExceptionErrorCodeMap.put("com.cloud.exception.InternalErrorException", 4345);
            ExceptionErrorCodeMap.put("InvalidParameterValueException", 4350);
            ExceptionErrorCodeMap.put("com.cloud.exception.ManagementServerException", 4355);
            ExceptionErrorCodeMap.put("com.cloud.exception.NetworkRuleConflictException", 4360);
            ExceptionErrorCodeMap.put("com.cloud.exception.PermissionDeniedException", 4365);
            ExceptionErrorCodeMap.put("com.cloud.exception.ResourceAllocationException", 4370);
            ExceptionErrorCodeMap.put("com.cloud.exception.ResourceInUseException", 4375);
            ExceptionErrorCodeMap.put("com.cloud.exception.ResourceUnavailableException", 4380);
            ExceptionErrorCodeMap.put("com.cloud.exception.StorageUnavailableException", 4385);
            ExceptionErrorCodeMap.put("com.cloud.exception.UnsupportedServiceException", 4390);
            ExceptionErrorCodeMap.put("com.cloud.exception.VirtualMachineMigrationException", 4395);
            ExceptionErrorCodeMap.put("com.cloud.async.AsyncCommandQueued", 4540);
            ExceptionErrorCodeMap.put("com.cloud.exception.RequestLimitException", 4545);
            ExceptionErrorCodeMap.put("com.cloud.exception.StorageConflictException", 4550);
            ExceptionErrorCodeMap.put("com.cloud.exception.IllegalVirtualMachineException", 4555);

            // Have a special error code for ServerApiException when it is
            // thrown in a standalone manner when failing to detect any of the above
            // standard exceptions.
            ExceptionErrorCodeMap.put("com.cloud.api.ServerApiException", 9999);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static HashMap<String, Integer> getErrCodeList() {
        return ExceptionErrorCodeMap;
    }

    public static int getCSErrCode(final String exceptionName) {
        return ExceptionErrorCodeMap.getOrDefault(exceptionName, -1);
    }

    public static String getCurMethodName() {
        final StackTraceElement[] stackTraceCalls = (new Throwable()).getStackTrace();
        return stackTraceCalls[1].toString();
    }
}
