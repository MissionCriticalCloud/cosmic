package com.cloud.api;

import com.cloud.api.auth.APIAuthenticationManager;
import com.cloud.api.auth.APIAuthenticationType;
import com.cloud.api.auth.APIAuthenticator;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.common.managed.context.ManagedContext;
import com.cloud.user.AccountService;
import com.cloud.utils.HttpUtils;
import com.cloud.utils.StringUtils;
import com.cloud.utils.net.NetUtils;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component("apiServlet")
public class ApiServlet extends HttpServlet {
    public static final Logger s_logger = LoggerFactory.getLogger(ApiServlet.class.getName());
    private final static List<String> s_clientAddressHeaders = Collections
            .unmodifiableList(Arrays.asList("X-Forwarded-For",
                    "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "Remote_Addr"));

    @Inject
    ApiServerService _apiServer;
    @Inject
    AccountService _accountMgr;
    @Inject
    EntityManager _entityMgr;
    @Inject
    ManagedContext _managedContext;
    @Inject
    APIAuthenticationManager _authManager;

    public ApiServlet() {
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        processRequest(req, resp);
    }

    private void processRequest(final HttpServletRequest req, final HttpServletResponse resp) {
        _managedContext.runWithContext(() -> processRequestInContext(req, resp));
    }

    void processRequestInContext(final HttpServletRequest req, final HttpServletResponse resp) {
        final String remoteAddress = getClientAddress(req);
        final StringBuilder auditTrailSb = new StringBuilder(128);
        auditTrailSb.append(" ").append(remoteAddress);
        auditTrailSb.append(" -- ").append(req.getMethod()).append(' ');
        // get the response format since we'll need it in a couple of places
        String responseType = HttpUtils.RESPONSE_TYPE_XML;
        final Map<String, Object[]> params = new HashMap<>();
        params.putAll(req.getParameterMap());

        // For HTTP GET requests, it seems that HttpServletRequest.getParameterMap() actually tries
        // to unwrap URL encoded content from ISO-9959-1.
        // After failed in using setCharacterEncoding() to control it, end up with following hacking:
        // for all GET requests, we will override it with our-own way of UTF-8 based URL decoding.
        utf8Fixup(req, params);

        // logging the request start and end in management log for easy debugging
        String reqStr = "";
        final String cleanQueryString = StringUtils.cleanString(req.getQueryString());
        if (s_logger.isDebugEnabled()) {
            reqStr = auditTrailSb.toString() + " " + cleanQueryString;
            s_logger.debug("===START=== " + reqStr);
        }

        try {

            if (HttpUtils.RESPONSE_TYPE_JSON.equalsIgnoreCase(responseType)) {
                resp.setContentType(ApiServer.getJSONContentType());
            } else if (HttpUtils.RESPONSE_TYPE_XML.equalsIgnoreCase(responseType)) {
                resp.setContentType(HttpUtils.XML_CONTENT_TYPE);
            }

            HttpSession session = req.getSession(false);
            final Object[] responseTypeParam = params.get(ApiConstants.RESPONSE);
            if (responseTypeParam != null) {
                responseType = (String) responseTypeParam[0];
            }

            final Object[] commandObj = params.get(ApiConstants.COMMAND);
            if (commandObj != null) {
                final String command = (String) commandObj[0];

                final APIAuthenticator apiAuthenticator = _authManager.getAPIAuthenticator(command);
                if (apiAuthenticator != null) {
                    auditTrailSb.append("command=");
                    auditTrailSb.append(command);

                    int httpResponseCode = HttpServletResponse.SC_OK;
                    String responseString;

                    if (apiAuthenticator.getAPIType() == APIAuthenticationType.LOGIN_API) {
                        if (session != null) {
                            try {
                                session.invalidate();
                            } catch (final IllegalStateException e) {
                                s_logger.warn("Previously ignored exception", e);
                            }
                        }
                        session = req.getSession(true);
                        if (ApiServer.isSecureSessionCookieEnabled()) {
                            resp.setHeader("SET-COOKIE", String.format("JSESSIONID=%s;Secure;HttpOnly;Path=/client", session.getId()));
                            s_logger.debug("Session cookie is marked secure!");
                        }
                    }

                    try {
                        responseString = apiAuthenticator.authenticate(command, params, session, InetAddress.getByName(remoteAddress), responseType, auditTrailSb, req, resp);
                        if (session != null && session.getAttribute(ApiConstants.SESSIONKEY) != null) {
                            resp.addHeader("SET-COOKIE", String.format("%s=%s;HttpOnly", ApiConstants.SESSIONKEY, session.getAttribute(ApiConstants.SESSIONKEY)));
                        }
                    } catch (final ServerApiException e) {
                        httpResponseCode = e.getErrorCode().getHttpCode();
                        responseString = e.getMessage();
                        s_logger.debug("Authentication failure: " + e.getMessage());
                    } catch (final UnknownHostException e) {
                        httpResponseCode = 400;
                        responseString = e.getMessage();
                        s_logger.debug("Authentication failure: " + e.getMessage());
                    }

                    if (apiAuthenticator.getAPIType() == APIAuthenticationType.LOGOUT_API) {
                        if (session != null) {
                            final Long userId = (Long) session.getAttribute("userid");
                            final Account account = (Account) session.getAttribute("accountobj");
                            Long accountId = null;
                            if (account != null) {
                                accountId = account.getId();
                            }
                            auditTrailSb.insert(0, "(userId=" + userId + " accountId=" + accountId + " sessionId=" + session.getId() + ")");
                            if (userId != null) {
                                _apiServer.logoutUser(userId);
                            }
                            try {
                                session.invalidate();
                            } catch (final IllegalStateException e) {
                                s_logger.warn("Previously ignored exception", e);
                            }
                        }
                        final Cookie sessionKeyCookie = new Cookie(ApiConstants.SESSIONKEY, "");
                        sessionKeyCookie.setMaxAge(0);
                        resp.addCookie(sessionKeyCookie);
                    }
                    HttpUtils.writeHttpResponse(resp, responseString, httpResponseCode, responseType, ApiServer.getJSONContentType());
                    return;
                }
            }

            auditTrailSb.append(cleanQueryString);
            final boolean isNew = ((session == null) ? true : session.isNew());

            // Initialize an empty context and we will update it after we have verified the request below,
            // we no longer rely on web-session here, verifyRequest will populate user/account information
            // if a API key exists
            Long userId = null;

            if (!isNew) {
                userId = (Long) session.getAttribute("userid");
                final String account = (String) session.getAttribute("account");
                final Object accountObj = session.getAttribute("accountobj");
                if (!HttpUtils.validateSessionKey(session, params, req.getCookies(), ApiConstants.SESSIONKEY)) {
                    denyAuthenticationRequest(resp, auditTrailSb, responseType, params, session);
                    return;
                }

                // Do a sanity check here to make sure the user hasn't already been deleted
                if ((userId != null) && (account != null) && (accountObj != null) && _apiServer.verifyUser(userId)) {
                    final String[] command = (String[]) params.get(ApiConstants.COMMAND);
                    if (command == null) {
                        s_logger.info("missing command, ignoring request...");
                        auditTrailSb.append(" " + HttpServletResponse.SC_BAD_REQUEST + " " + "no command specified");
                        final String serializedResponse = _apiServer.getSerializedApiError(HttpServletResponse.SC_BAD_REQUEST, "no command specified", params, responseType);
                        HttpUtils.writeHttpResponse(resp, serializedResponse, HttpServletResponse.SC_BAD_REQUEST, responseType, ApiServer.getJSONContentType());
                        return;
                    }
                    final User user = _entityMgr.findById(User.class, userId);
                    CallContext.register(user, (Account) accountObj);
                } else {
                    // Invalidate the session to ensure we won't allow a request across management server
                    // restarts if the userId was serialized to the stored session
                    denyAuthenticationRequest(resp, auditTrailSb, responseType, params, session);
                }
            } else {
                CallContext.register(_accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
            }

            if (_apiServer.verifyRequest(params, userId, remoteAddress)) {
                auditTrailSb.insert(0, "(userId=" + CallContext.current().getCallingUserId() + " accountId=" + CallContext.current().getCallingAccount().getId() +
                        " sessionId=" + (session != null ? session.getId() : null) + ")");

                // Add the HTTP method (GET/POST/PUT/DELETE) as well into the params map.
                params.put("httpmethod", new String[]{req.getMethod()});
                final String response = _apiServer.handleRequest(params, responseType, auditTrailSb);
                HttpUtils.writeHttpResponse(resp, response != null ? response : "", HttpServletResponse.SC_OK, responseType, ApiServer.getJSONContentType());
            } else {
                if (session != null) {
                    try {
                        session.invalidate();
                    } catch (final IllegalStateException e) {
                        s_logger.warn("Previously ignored exception", e);
                    }
                }

                final String errorMessage = "Permission denied due to: Expired session, unavailable API command or non-authorised from ip-address '" + remoteAddress + "'";
                auditTrailSb.append(" " + HttpServletResponse.SC_UNAUTHORIZED + " " + errorMessage);
                final String serializedResponse =
                        _apiServer.getSerializedApiError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage, params, responseType);
                HttpUtils.writeHttpResponse(resp, serializedResponse, HttpServletResponse.SC_UNAUTHORIZED, responseType, ApiServer.getJSONContentType());
            }
        } catch (final ServerApiException se) {
            final String serializedResponseText = _apiServer.getSerializedApiError(se, params, responseType);
            resp.setHeader("X-Description", se.getDescription());
            HttpUtils.writeHttpResponse(resp, serializedResponseText, se.getErrorCode().getHttpCode(), responseType, ApiServer.getJSONContentType());
            auditTrailSb.append(" " + se.getErrorCode() + " " + se.getDescription());
        } catch (final CloudRuntimeException e) {
            s_logger.error("Caught runtime exception while writing api response", e);
            auditTrailSb.append("Caught runtime exception while writing api response");
        } finally {
            s_logger.info(auditTrailSb.toString());
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("===END=== " + reqStr);
            }
            // cleanup user context to prevent from being peeked in other request context
            CallContext.unregister();
        }
    }

    private void denyAuthenticationRequest(final HttpServletResponse resp, final StringBuilder auditTrailSb, final String responseType, final Map<String, Object[]> params, final
    HttpSession session) {
        try {
            session.invalidate();
        } catch (final IllegalStateException ise) {
        }
        auditTrailSb.append(" " + HttpServletResponse.SC_UNAUTHORIZED + " " + "unable to verify user credentials");
        final String serializedResponse =
                _apiServer.getSerializedApiError(HttpServletResponse.SC_UNAUTHORIZED, "unable to verify user credentials", params, responseType);
        HttpUtils.writeHttpResponse(resp, serializedResponse, HttpServletResponse.SC_UNAUTHORIZED, responseType, ApiServer.getJSONContentType());
        return;
    }

    //This method will try to get login IP of user even if servlet is behind reverseProxy or loadBalancer
    static String getClientAddress(final HttpServletRequest request) {
        for (final String header : s_clientAddressHeaders) {
            final String ip = getCorrectIPAddress(request.getHeader(header));
            if (ip != null) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    void utf8Fixup(final HttpServletRequest req, final Map<String, Object[]> params) {
        if (req.getQueryString() == null) {
            return;
        }

        final String[] paramsInQueryString = req.getQueryString().split("&");
        if (paramsInQueryString != null) {
            for (final String param : paramsInQueryString) {
                final String[] paramTokens = param.split("=", 2);
                if (paramTokens.length == 2) {
                    final String name = decodeUtf8(paramTokens[0]);
                    final String value = decodeUtf8(paramTokens[1]);
                    params.put(name, new String[]{value});
                } else {
                    s_logger.debug("Invalid parameter in URL found. param: " + param);
                }
            }
        }
    }

    private static String getCorrectIPAddress(final String ip) {
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }
        if (NetUtils.isValidIp4(ip) || NetUtils.isValidIp6(ip)) {
            return ip;
        }
        //it could be possible to have multiple IPs in HTTP header, this happens if there are multiple proxy in between
        //the client and the servlet, so parse the client IP
        final String[] ips = ip.split(",");
        for (final String i : ips) {
            if (NetUtils.isValidIp4(i.trim()) || NetUtils.isValidIp6(i.trim())) {
                return i.trim();
            }
        }
        return null;
    }

    private String decodeUtf8(final String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            //should never happen
            return null;
        }
    }
}
