package org.apache.cloudstack.api;

import com.cloud.exception.CloudAuthenticationException;

import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.util.Map;

public interface ApiServerService {
    public boolean verifyRequest(Map<String, Object[]> requestParameters, Long userId) throws ServerApiException;

    public Long fetchDomainId(String domainUUID);

    public ResponseObject loginUser(HttpSession session, String username, String password, Long domainId, String domainPath, InetAddress loginIpAddress,
                                    Map<String, Object[]> requestParameters) throws CloudAuthenticationException;

    public void logoutUser(long userId);

    public boolean verifyUser(Long userId);

    public String getSerializedApiError(int errorCode, String errorText, Map<String, Object[]> apiCommandParams, String responseType);

    public String getSerializedApiError(ServerApiException ex, Map<String, Object[]> apiCommandParams, String responseType);

    public String handleRequest(Map params, String responseType, StringBuilder auditTrailSb) throws ServerApiException;

    public Class<?> getCmdClass(String cmdName);
}
