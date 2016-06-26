package com.cloud.api;

import com.cloud.server.ManagementServer;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.auth.APIAuthenticationManager;
import org.apache.cloudstack.api.auth.APIAuthenticationType;
import org.apache.cloudstack.api.auth.APIAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApiServletTest {

    @Mock
    ApiServer apiServer;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    AccountService accountService;

    @Mock
    APIAuthenticationManager authManager;

    @Mock
    APIAuthenticator authenticator;

    @Mock
    User user;

    @Mock
    Account account;

    @Mock
    HttpSession session;

    @Mock
    ManagementServer managementServer;

    StringWriter responseWriter;

    ApiServlet servlet;

    @Before
    public void setup() throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, IOException, UnknownHostException {
        servlet = new ApiServlet();
        responseWriter = new StringWriter();
        Mockito.when(response.getWriter()).thenReturn(
                new PrintWriter(responseWriter));
        Mockito.when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Mockito.when(accountService.getSystemUser()).thenReturn(user);
        Mockito.when(accountService.getSystemAccount()).thenReturn(account);

        final Field accountMgrField = ApiServlet.class
                .getDeclaredField("_accountMgr");
        accountMgrField.setAccessible(true);
        accountMgrField.set(servlet, accountService);

        Mockito.when(authManager.getAPIAuthenticator(Mockito.anyString())).thenReturn(authenticator);
        Mockito.when(authenticator.authenticate(Mockito.anyString(), Mockito.anyMap(), Mockito.isA(HttpSession.class),
                Mockito.same(InetAddress.getByName("127.0.0.1")), Mockito.anyString(), Mockito.isA(StringBuilder.class), Mockito.isA(HttpServletRequest.class), Mockito.isA
                        (HttpServletResponse.class))).thenReturn("{\"loginresponse\":{}");

        final Field authManagerField = ApiServlet.class.getDeclaredField("_authManager");
        authManagerField.setAccessible(true);
        authManagerField.set(servlet, authManager);

        final Field apiServerField = ApiServlet.class.getDeclaredField("_apiServer");
        apiServerField.setAccessible(true);
        apiServerField.set(servlet, apiServer);
    }

    /**
     * These are envinonment hacks, actually getting into the behavior of other
     * classes, but there is no other way to run the test.
     */
    @Before
    public void hackEnvironment() throws Exception {
        final Field smsField = ApiDBUtils.class.getDeclaredField("s_ms");
        smsField.setAccessible(true);
        smsField.set(null, managementServer);
        Mockito.when(managementServer.getVersion()).thenReturn(
                "LATEST-AND-GREATEST");
    }

    @After
    public void cleanupEnvironmentHacks() throws Exception {
        final Field smsField = ApiDBUtils.class.getDeclaredField("s_ms");
        smsField.setAccessible(true);
        smsField.set(null, null);
    }

    @Test
    public void utf8Fixup() {
        Mockito.when(request.getQueryString()).thenReturn(
                "foo=12345&bar=blah&baz=&param=param");
        final HashMap<String, Object[]> params = new HashMap<>();
        servlet.utf8Fixup(request, params);
        Assert.assertEquals("12345", params.get("foo")[0]);
        Assert.assertEquals("blah", params.get("bar")[0]);
    }

    @Test
    public void utf8FixupNull() {
        Mockito.when(request.getQueryString()).thenReturn("&&=a&=&&a&a=a=a=a");
        servlet.utf8Fixup(request, new HashMap<>());
    }

    @Test
    public void utf8FixupStrangeInputs() {
        Mockito.when(request.getQueryString()).thenReturn("&&=a&=&&a&a=a=a=a");
        final HashMap<String, Object[]> params = new HashMap<>();
        servlet.utf8Fixup(request, params);
        Assert.assertTrue(params.containsKey(""));
    }

    @Test
    public void utf8FixupUtf() throws UnsupportedEncodingException {
        Mockito.when(request.getQueryString()).thenReturn(
                URLEncoder.encode("防水镜钻孔机", "UTF-8") + "="
                        + URLEncoder.encode("árvíztűrőtükörfúró", "UTF-8"));
        final HashMap<String, Object[]> params = new HashMap<>();
        servlet.utf8Fixup(request, params);
        Assert.assertEquals("árvíztűrőtükörfúró", params.get("防水镜钻孔机")[0]);
    }

    @Test
    public void processRequestInContextUnauthorizedGET() {
        Mockito.when(request.getMethod()).thenReturn("GET");
        Mockito.when(
                apiServer.verifyRequest(Mockito.anyMap(), Mockito.anyLong()))
               .thenReturn(false);
        servlet.processRequestInContext(request, response);
        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verify(apiServer, Mockito.never()).handleRequest(
                Mockito.anyMap(), Mockito.anyString(),
                Mockito.any(StringBuilder.class));
    }

    @Test
    public void processRequestInContextAuthorizedGet() {
        Mockito.when(request.getMethod()).thenReturn("GET");
        Mockito.when(
                apiServer.verifyRequest(Mockito.anyMap(), Mockito.anyLong()))
               .thenReturn(true);
        servlet.processRequestInContext(request, response);
        Mockito.verify(response).setStatus(HttpServletResponse.SC_OK);
        Mockito.verify(apiServer, Mockito.times(1)).handleRequest(
                Mockito.anyMap(), Mockito.anyString(),
                Mockito.any(StringBuilder.class));
    }

    @Test
    public void processRequestInContextLogout() throws UnknownHostException {
        Mockito.when(request.getMethod()).thenReturn("GET");
        Mockito.when(request.getSession(Mockito.anyBoolean())).thenReturn(
                session);
        Mockito.when(session.getAttribute("userid")).thenReturn(1l);
        Mockito.when(session.getAttribute("accountobj")).thenReturn(account);
        final HashMap<String, String[]> params = new HashMap<>();
        params.put(ApiConstants.COMMAND, new String[]{"logout"});
        Mockito.when(request.getParameterMap()).thenReturn(params);

        Mockito.when(authenticator.getAPIType()).thenReturn(APIAuthenticationType.LOGOUT_API);

        servlet.processRequestInContext(request, response);

        Mockito.verify(authManager).getAPIAuthenticator("logout");
        Mockito.verify(authenticator).authenticate(Mockito.anyString(), Mockito.anyMap(), Mockito.isA(HttpSession.class),
                Mockito.eq(InetAddress.getByName("127.0.0.1")), Mockito.anyString(), Mockito.isA(StringBuilder.class), Mockito.isA(HttpServletRequest.class), Mockito.isA
                        (HttpServletResponse.class));
        Mockito.verify(session).invalidate();
    }

    @Test
    public void processRequestInContextLogin() throws UnknownHostException {
        Mockito.when(request.getMethod()).thenReturn("GET");
        Mockito.when(request.getSession(Mockito.anyBoolean())).thenReturn(
                session);
        final HashMap<String, String[]> params = new HashMap<>();
        params.put(ApiConstants.COMMAND, new String[]{"login"});
        params.put(ApiConstants.USERNAME, new String[]{"TEST"});
        params.put(ApiConstants.PASSWORD, new String[]{"TEST-PWD"});
        params.put(ApiConstants.DOMAIN_ID, new String[]{"42"});
        params.put(ApiConstants.DOMAIN, new String[]{"TEST-DOMAIN"});
        Mockito.when(request.getParameterMap()).thenReturn(params);

        servlet.processRequestInContext(request, response);

        Mockito.verify(authManager).getAPIAuthenticator("login");
        Mockito.verify(authenticator).authenticate(Mockito.anyString(), Mockito.anyMap(), Mockito.isA(HttpSession.class),
                Mockito.eq(InetAddress.getByName("127.0.0.1")), Mockito.anyString(), Mockito.isA(StringBuilder.class), Mockito.isA(HttpServletRequest.class), Mockito.isA
                        (HttpServletResponse.class));
    }

    @Test
    public void getClientAddressWithXForwardedFor() {
        Mockito.when(request.getHeader(Mockito.eq("X-Forwarded-For"))).thenReturn("192.168.1.1");
        Assert.assertEquals("192.168.1.1", ApiServlet.getClientAddress(request));
    }

    @Test
    public void getClientAddressWithHttpXForwardedFor() {
        Mockito.when(request.getHeader(Mockito.eq("HTTP_X_FORWARDED_FOR"))).thenReturn("192.168.1.1");
        Assert.assertEquals("192.168.1.1", ApiServlet.getClientAddress(request));
    }

    @Test
    public void getClientAddressWithXRemoteAddr() {
        Mockito.when(request.getHeader(Mockito.eq("Remote_Addr"))).thenReturn("192.168.1.1");
        Assert.assertEquals("192.168.1.1", ApiServlet.getClientAddress(request));
    }

    @Test
    public void getClientAddressWithHttpClientIp() {
        Mockito.when(request.getHeader(Mockito.eq("HTTP_CLIENT_IP"))).thenReturn("192.168.1.1");
        Assert.assertEquals("192.168.1.1", ApiServlet.getClientAddress(request));
    }

    @Test
    public void getClientAddressDefault() {
        Mockito.when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Assert.assertEquals("127.0.0.1", ApiServlet.getClientAddress(request));
    }
}
