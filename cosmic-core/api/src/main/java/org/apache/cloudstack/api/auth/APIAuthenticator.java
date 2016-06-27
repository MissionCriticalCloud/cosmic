package org.apache.cloudstack.api.auth;

import org.apache.cloudstack.api.ServerApiException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/*
* APIAuthenticator is an interface that defines method that
* a class should implement that help with Authentication and accepts
* a command string and an array of parameters. This should be used only
* in the Servlet that is doing authentication.
*
* @param command The API command name such as login, logout etc
* @param params An array of HTTP parameters
* @param session HttpSession object
* */
public interface APIAuthenticator {
    public String authenticate(String command, Map<String, Object[]> params,
                               HttpSession session, InetAddress remoteAddress, String responseType,
                               StringBuilder auditTrailSb, final HttpServletRequest req, final HttpServletResponse resp) throws ServerApiException;

    public APIAuthenticationType getAPIType();

    public void setAuthenticators(List<PluggableAPIAuthenticator> authenticators);
}
