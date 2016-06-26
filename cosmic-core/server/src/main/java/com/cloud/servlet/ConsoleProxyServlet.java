package com.cloud.servlet;

import com.cloud.dao.EntityManager;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.host.HostVO;
import com.cloud.server.ManagementServer;
import com.cloud.storage.GuestOSVO;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.User;
import com.cloud.uservm.UserVm;
import com.cloud.utils.ConstantTimeComparator;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.vm.UserVmDetailVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.UserVmDetailsDao;
import org.apache.cloudstack.framework.security.keys.KeysManager;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Thumbnail access : /console?cmd=thumbnail&vm=xxx&w=xxx&h=xxx
 * Console access : /conosole?cmd=access&vm=xxx
 * Authentication : /console?cmd=auth&vm=xxx&sid=xxx
 */
@Component("consoleServlet")
public class ConsoleProxyServlet extends HttpServlet {
    public static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyServlet.class.getName());
    private static final long serialVersionUID = -5515382620323808168L;
    private static final int DEFAULT_THUMBNAIL_WIDTH = 144;
    private static final int DEFAULT_THUMBNAIL_HEIGHT = 110;
    static KeysManager s_keysMgr;
    private final Gson _gson = new GsonBuilder().create();
    @Inject
    AccountManager _accountMgr;
    @Inject
    VirtualMachineManager _vmMgr;
    @Inject
    ManagementServer _ms;
    @Inject
    EntityManager _entityMgr;
    @Inject
    UserVmDetailsDao _userVmDetailsDao;
    @Inject
    KeysManager _keysMgr;

    public ConsoleProxyServlet() {
    }

    // put the ugly stuff here
    static public Ternary<String, String, String> parseHostInfo(final String hostInfo) {
        String host = null;
        String tunnelUrl = null;
        String tunnelSession = null;

        s_logger.info("Parse host info returned from executing GetVNCPortCommand. host info: " + hostInfo);

        if (hostInfo != null) {
            if (hostInfo.startsWith("consoleurl")) {
                final String[] tokens = hostInfo.split("&");

                if (hostInfo.length() > 19 && hostInfo.indexOf('/', 19) > 19) {
                    host = hostInfo.substring(19, hostInfo.indexOf('/', 19)).trim();
                    tunnelUrl = tokens[0].substring("consoleurl=".length());
                    tunnelSession = tokens[1].split("=")[1];
                } else {
                    host = "";
                }
            } else if (hostInfo.startsWith("instanceId")) {
                host = hostInfo.substring(hostInfo.indexOf('=') + 1);
            } else {
                host = hostInfo;
            }
        } else {
            host = hostInfo;
        }

        return new Ternary<>(host, tunnelUrl, tunnelSession);
    }

    public static String genAccessTicket(final String host, final String port, final String sid, final String tag) {
        return genAccessTicket(host, port, sid, tag, new Date());
    }

    public static String genAccessTicket(final String host, final String port, final String sid, final String tag, final Date normalizedHashTime) {
        final String params = "host=" + host + "&port=" + port + "&sid=" + sid + "&tag=" + tag;

        try {
            final Mac mac = Mac.getInstance("HmacSHA1");

            long ts = normalizedHashTime.getTime();
            ts = ts / 60000;        // round up to 1 minute
            final String secretKey = s_keysMgr.getHashKey();

            final SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            mac.init(keySpec);
            mac.update(params.getBytes());
            mac.update(String.valueOf(ts).getBytes());

            final byte[] encryptedBytes = mac.doFinal();

            return Base64.encodeBase64String(encryptedBytes);
        } catch (final Exception e) {
            s_logger.error("Unexpected exception ", e);
        }
        return "";
    }

    public static final String escapeHTML(final String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            final char c = content.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case ' ':
                    sb.append("&nbsp;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
        s_keysMgr = _keysMgr;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {

        try {
            if (_accountMgr == null || _vmMgr == null || _ms == null) {
                sendResponse(resp, "Service is not ready");
                return;
            }

            if (_keysMgr.getHashKey() == null) {
                s_logger.debug("Console/thumbnail access denied. Ticket service is not ready yet");
                sendResponse(resp, "Service is not ready");
                return;
            }

            String userId = null;
            String account = null;
            Account accountObj = null;

            final Map<String, Object[]> params = new HashMap<>();
            params.putAll(req.getParameterMap());

            final HttpSession session = req.getSession(false);
            if (session == null) {
                if (verifyRequest(params)) {
                    userId = (String) params.get("userid")[0];
                    account = (String) params.get("account")[0];
                    accountObj = (Account) params.get("accountobj")[0];
                } else {
                    s_logger.debug("Invalid web session or API key in request, reject console/thumbnail access");
                    sendResponse(resp, "Access denied. Invalid web session or API key in request");
                    return;
                }
            } else {
                // adjust to latest API refactoring changes
                if (session.getAttribute("userid") != null) {
                    userId = ((Long) session.getAttribute("userid")).toString();
                }

                accountObj = (Account) session.getAttribute("accountobj");
                if (accountObj != null) {
                    account = "" + accountObj.getId();
                }
            }

            // Do a sanity check here to make sure the user hasn't already been deleted
            if ((userId == null) || (account == null) || (accountObj == null) || !verifyUser(Long.valueOf(userId))) {
                s_logger.debug("Invalid user/account, reject console/thumbnail access");
                sendResponse(resp, "Access denied. Invalid or inconsistent account is found");
                return;
            }

            final String cmd = req.getParameter("cmd");
            if (cmd == null || !isValidCmd(cmd)) {
                s_logger.debug("invalid console servlet command: " + cmd);
                sendResponse(resp, "");
                return;
            }

            final String vmIdString = req.getParameter("vm");
            final VirtualMachine vm = _entityMgr.findByUuid(VirtualMachine.class, vmIdString);
            if (vm == null) {
                s_logger.info("invalid console servlet command parameter: " + vmIdString);
                sendResponse(resp, "");
                return;
            }

            final Long vmId = vm.getId();

            if (!checkSessionPermision(req, vmId, accountObj)) {
                sendResponse(resp, "Permission denied");
                return;
            }

            if (cmd.equalsIgnoreCase("thumbnail")) {
                handleThumbnailRequest(req, resp, vmId);
            } else if (cmd.equalsIgnoreCase("access")) {
                handleAccessRequest(req, resp, vmId);
            } else {
                handleAuthRequest(req, resp, vmId);
            }
        } catch (final Throwable e) {
            s_logger.error("Unexepected exception in ConsoleProxyServlet", e);
            sendResponse(resp, "Server Internal Error");
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        doGet(req, resp);
    }

    private void handleThumbnailRequest(final HttpServletRequest req, final HttpServletResponse resp, final long vmId) {
        final VirtualMachine vm = _vmMgr.findById(vmId);
        if (vm == null) {
            s_logger.warn("VM " + vmId + " does not exist, sending blank response for thumbnail request");
            sendResponse(resp, "");
            return;
        }

        if (vm.getHostId() == null) {
            s_logger.warn("VM " + vmId + " lost host info, sending blank response for thumbnail request");
            sendResponse(resp, "");
            return;
        }

        final HostVO host = _ms.getHostBy(vm.getHostId());
        if (host == null) {
            s_logger.warn("VM " + vmId + "'s host does not exist, sending blank response for thumbnail request");
            sendResponse(resp, "");
            return;
        }

        final String rootUrl = _ms.getConsoleAccessUrlRoot(vmId);
        if (rootUrl == null) {
            sendResponse(resp, "");
            return;
        }

        int w = DEFAULT_THUMBNAIL_WIDTH;
        int h = DEFAULT_THUMBNAIL_HEIGHT;

        String value = req.getParameter("w");
        try {
            w = Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            s_logger.info("[ignored] not a number: " + value);
        }

        value = req.getParameter("h");
        try {
            h = Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            s_logger.info("[ignored] not a number: " + value);
        }

        try {
            resp.sendRedirect(composeThumbnailUrl(rootUrl, vm, host, w, h));
        } catch (final IOException e) {
            s_logger.info("Client may already close the connection", e);
        }
    }

    private void handleAccessRequest(final HttpServletRequest req, final HttpServletResponse resp, final long vmId) {
        final VirtualMachine vm = _vmMgr.findById(vmId);
        if (vm == null) {
            s_logger.warn("VM " + vmId + " does not exist, sending blank response for console access request");
            sendResponse(resp, "");
            return;
        }

        if (vm.getHostId() == null) {
            s_logger.warn("VM " + vmId + " lost host info, sending blank response for console access request");
            sendResponse(resp, "");
            return;
        }

        final HostVO host = _ms.getHostBy(vm.getHostId());
        if (host == null) {
            s_logger.warn("VM " + vmId + "'s host does not exist, sending blank response for console access request");
            sendResponse(resp, "");
            return;
        }

        final String rootUrl = _ms.getConsoleAccessUrlRoot(vmId);
        if (rootUrl == null) {
            sendResponse(resp, "<html><body><p>Console access will be ready in a few minutes. Please try it again later.</p></body></html>");
            return;
        }

        String vmName = vm.getHostName();
        if (vm.getType() == VirtualMachine.Type.User) {
            final UserVm userVm = _entityMgr.findById(UserVm.class, vmId);
            final String displayName = userVm.getDisplayName();
            if (displayName != null && !displayName.isEmpty() && !displayName.equals(vmName)) {
                vmName += "(" + displayName + ")";
            }
        }

        final StringBuffer sb = new StringBuffer();
        sb.append("<html><title>").append(escapeHTML(vmName)).append("</title><frameset><frame src=\"").append(composeConsoleAccessUrl(rootUrl, vm, host));
        sb.append("\"></frame></frameset></html>");
        s_logger.debug("the console url is :: " + sb.toString());
        sendResponse(resp, sb.toString());
    }

    private void handleAuthRequest(final HttpServletRequest req, final HttpServletResponse resp, final long vmId) {

        // TODO authentication channel between console proxy VM and management server needs to be secured,
        // the data is now being sent through private network, but this is apparently not enough
        final VirtualMachine vm = _vmMgr.findById(vmId);
        if (vm == null) {
            s_logger.warn("VM " + vmId + " does not exist, sending failed response for authentication request from console proxy");
            sendResponse(resp, "failed");
            return;
        }

        if (vm.getHostId() == null) {
            s_logger.warn("VM " + vmId + " lost host info, failed response for authentication request from console proxy");
            sendResponse(resp, "failed");
            return;
        }

        final HostVO host = _ms.getHostBy(vm.getHostId());
        if (host == null) {
            s_logger.warn("VM " + vmId + "'s host does not exist, sending failed response for authentication request from console proxy");
            sendResponse(resp, "failed");
            return;
        }

        final String sid = req.getParameter("sid");
        if (sid == null || !sid.equals(vm.getVncPassword())) {
            s_logger.warn("sid " + sid + " in url does not match stored sid.");
            sendResponse(resp, "failed");
            return;
        }

        sendResponse(resp, "success");
    }

    private String getEncryptorPassword() {
        final String key = _keysMgr.getEncryptionKey();
        final String iv = _keysMgr.getEncryptionIV();

        final ConsoleProxyPasswordBasedEncryptor.KeyIVPair keyIvPair = new ConsoleProxyPasswordBasedEncryptor.KeyIVPair(key, iv);
        return _gson.toJson(keyIvPair);
    }

    private String composeThumbnailUrl(final String rootUrl, final VirtualMachine vm, final HostVO hostVo, final int w, final int h) {
        final StringBuffer sb = new StringBuffer(rootUrl);

        final String host = hostVo.getPrivateIpAddress();

        final Pair<String, Integer> portInfo = _ms.getVncPort(vm);
        final Ternary<String, String, String> parsedHostInfo = parseHostInfo(portInfo.first());

        final String sid = vm.getVncPassword();
        final String tag = vm.getUuid();

        final int port = portInfo.second();

        final String ticket = genAccessTicket(parsedHostInfo.first(), String.valueOf(port), sid, tag);

        final ConsoleProxyPasswordBasedEncryptor encryptor = new ConsoleProxyPasswordBasedEncryptor(getEncryptorPassword());
        final ConsoleProxyClientParam param = new ConsoleProxyClientParam();
        param.setClientHostAddress(parsedHostInfo.first());
        param.setClientHostPort(portInfo.second());
        param.setClientHostPassword(sid);
        param.setClientTag(tag);
        param.setTicket(ticket);
        if (portInfo.second() == -9) {
            param.setUsername(_ms.findDetail(hostVo.getId(), "username").getValue());
            param.setPassword(_ms.findDetail(hostVo.getId(), "password").getValue());
        }
        if (parsedHostInfo.second() != null && parsedHostInfo.third() != null) {
            param.setClientTunnelUrl(parsedHostInfo.second());
            param.setClientTunnelSession(parsedHostInfo.third());
        }

        sb.append("/ajaximg?token=" + encryptor.encryptObject(ConsoleProxyClientParam.class, param));
        sb.append("&w=").append(w).append("&h=").append(h).append("&key=0");

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Compose thumbnail url: " + sb.toString());
        }
        return sb.toString();
    }

    private String composeConsoleAccessUrl(final String rootUrl, final VirtualMachine vm, final HostVO hostVo) {
        final StringBuffer sb = new StringBuffer(rootUrl);
        final String host = hostVo.getPrivateIpAddress();

        final Pair<String, Integer> portInfo = _ms.getVncPort(vm);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Port info " + portInfo.first());
        }

        final Ternary<String, String, String> parsedHostInfo = parseHostInfo(portInfo.first());

        final int port = portInfo.second();

        final String sid = vm.getVncPassword();
        final UserVmDetailVO details = _userVmDetailsDao.findDetail(vm.getId(), "keyboard");

        final String tag = vm.getUuid();

        final String ticket = genAccessTicket(parsedHostInfo.first(), String.valueOf(port), sid, tag);
        final ConsoleProxyPasswordBasedEncryptor encryptor = new ConsoleProxyPasswordBasedEncryptor(getEncryptorPassword());
        final ConsoleProxyClientParam param = new ConsoleProxyClientParam();
        param.setClientHostAddress(parsedHostInfo.first());
        param.setClientHostPort(port);
        param.setClientHostPassword(sid);
        param.setClientTag(tag);
        param.setTicket(ticket);

        if (details != null) {
            param.setLocale(details.getValue());
        }

        if (portInfo.second() == -9) {
            param.setUsername(_ms.findDetail(hostVo.getId(), "username").getValue());
            param.setPassword(_ms.findDetail(hostVo.getId(), "password").getValue());
        }
        if (parsedHostInfo.second() != null && parsedHostInfo.third() != null) {
            param.setClientTunnelUrl(parsedHostInfo.second());
            param.setClientTunnelSession(parsedHostInfo.third());
        }

        sb.append("/ajax?token=" + encryptor.encryptObject(ConsoleProxyClientParam.class, param));

        // for console access, we need guest OS type to help implement keyboard
        final long guestOs = vm.getGuestOSId();
        final GuestOSVO guestOsVo = _ms.getGuestOs(guestOs);
        if (guestOsVo.getCategoryId() == 6) {
            sb.append("&guest=windows");
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Compose console url: " + sb.toString());
        }
        return sb.toString();
    }

    private void sendResponse(final HttpServletResponse resp, final String content) {
        try {
            resp.setContentType("text/html");
            resp.getWriter().print(content);
        } catch (final IOException e) {
            s_logger.info("Client may already close the connection", e);
        }
    }

    private boolean checkSessionPermision(final HttpServletRequest req, final long vmId, final Account accountObj) {

        final VirtualMachine vm = _vmMgr.findById(vmId);
        if (vm == null) {
            s_logger.debug("Console/thumbnail access denied. VM " + vmId + " does not exist in system any more");
            return false;
        }

        // root admin can access anything
        if (_accountMgr.isRootAdmin(accountObj.getId())) {
            return true;
        }

        switch (vm.getType()) {
            case User:
                try {
                    _accountMgr.checkAccess(accountObj, null, true, vm);
                } catch (final PermissionDeniedException ex) {
                    if (_accountMgr.isNormalUser(accountObj.getId())) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("VM access is denied. VM owner account " + vm.getAccountId() + " does not match the account id in session " +
                                    accountObj.getId() + " and caller is a normal user");
                        }
                    } else if (_accountMgr.isDomainAdmin(accountObj.getId())
                            || accountObj.getType() == Account.ACCOUNT_TYPE_READ_ONLY_ADMIN) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("VM access is denied. VM owner account " + vm.getAccountId()
                                    + " does not match the account id in session " + accountObj.getId() + " and the domain-admin caller does not manage the target domain");
                        }
                    }
                    return false;
                }
                break;

            case DomainRouter:
            case ConsoleProxy:
            case SecondaryStorageVm:
                return false;

            default:
                s_logger.warn("Unrecoginized virtual machine type, deny access by default. type: " + vm.getType());
                return false;
        }

        return true;
    }

    private boolean isValidCmd(final String cmd) {
        if (cmd.equalsIgnoreCase("thumbnail") || cmd.equalsIgnoreCase("access") || cmd.equalsIgnoreCase("auth")) {
            return true;
        }

        return false;
    }

    public boolean verifyUser(final Long userId) {
        // copy from ApiServer.java, a bit ugly here
        final User user = _accountMgr.getUserIncludingRemoved(userId);
        Account account = null;
        if (user != null) {
            account = _accountMgr.getAccount(user.getAccountId());
        }

        if ((user == null) || (user.getRemoved() != null) || !user.getState().equals(Account.State.enabled) || (account == null) ||
                !account.getState().equals(Account.State.enabled)) {
            s_logger.warn("Deleted/Disabled/Locked user with id=" + userId + " attempting to access public API");
            return false;
        }
        return true;
    }

    // copied and modified from ApiServer.java.
    // TODO need to replace the whole servlet with a API command
    private boolean verifyRequest(final Map<String, Object[]> requestParameters) {
        try {
            String apiKey = null;
            String secretKey = null;
            String signature = null;
            String unsignedRequest = null;

            // - build a request string with sorted params, make sure it's all lowercase
            // - sign the request, verify the signature is the same
            final List<String> parameterNames = new ArrayList<>();

            for (final Object paramNameObj : requestParameters.keySet()) {
                parameterNames.add((String) paramNameObj); // put the name in a list that we'll sort later
            }

            Collections.sort(parameterNames);

            for (final String paramName : parameterNames) {
                // parameters come as name/value pairs in the form String/String[]
                final String paramValue = ((String[]) requestParameters.get(paramName))[0];

                if ("signature".equalsIgnoreCase(paramName)) {
                    signature = paramValue;
                } else {
                    if ("apikey".equalsIgnoreCase(paramName)) {
                        apiKey = paramValue;
                    }

                    if (unsignedRequest == null) {
                        unsignedRequest = paramName + "=" + URLEncoder.encode(paramValue, "UTF-8").replaceAll("\\+", "%20");
                    } else {
                        unsignedRequest = unsignedRequest + "&" + paramName + "=" + URLEncoder.encode(paramValue, "UTF-8").replaceAll("\\+", "%20");
                    }
                }
            }

            // if api/secret key are passed to the parameters
            if ((signature == null) || (apiKey == null)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("expired session, missing signature, or missing apiKey -- ignoring request...sig: " + signature + ", apiKey: " + apiKey);
                }
                return false; // no signature, bad request
            }

            final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
            txn.close();
            User user = null;
            // verify there is a user with this api key
            final Pair<User, Account> userAcctPair = _accountMgr.findUserByApiKey(apiKey);
            if (userAcctPair == null) {
                s_logger.debug("apiKey does not map to a valid user -- ignoring request, apiKey: " + apiKey);
                return false;
            }

            user = userAcctPair.first();
            final Account account = userAcctPair.second();

            if (!user.getState().equals(Account.State.enabled) || !account.getState().equals(Account.State.enabled)) {
                s_logger.debug("disabled or locked user accessing the api, userid = " + user.getId() + "; name = " + user.getUsername() + "; state: " + user.getState() +
                        "; accountState: " + account.getState());
                return false;
            }

            // verify secret key exists
            secretKey = user.getSecretKey();
            if (secretKey == null) {
                s_logger.debug("User does not have a secret key associated with the account -- ignoring request, username: " + user.getUsername());
                return false;
            }

            unsignedRequest = unsignedRequest.toLowerCase();

            final Mac mac = Mac.getInstance("HmacSHA1");
            final SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            mac.init(keySpec);
            mac.update(unsignedRequest.getBytes());
            final byte[] encryptedBytes = mac.doFinal();
            final String computedSignature = Base64.encodeBase64String(encryptedBytes);
            final boolean equalSig = ConstantTimeComparator.compareStrings(signature, computedSignature);
            if (!equalSig) {
                s_logger.debug("User signature: " + signature + " is not equaled to computed signature: " + computedSignature);
            }

            if (equalSig) {
                requestParameters.put("userid", new Object[]{String.valueOf(user.getId())});
                requestParameters.put("account", new Object[]{account.getAccountName()});
                requestParameters.put("accountobj", new Object[]{account});
            }
            return equalSig;
        } catch (final Exception ex) {
            s_logger.error("unable to verifty request signature", ex);
        }
        return false;
    }
}
