package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class ConsoleProxyHttpHandlerHelper {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyHttpHandlerHelper.class);

    public static Map<String, String> getQueryMap(final String query) {
        final String[] params = query.split("&");
        final Map<String, String> map = new HashMap<>();
        for (final String param : params) {
            final String[] paramTokens = param.split("=");
            if (paramTokens != null && paramTokens.length == 2) {
                final String name = param.split("=")[0];
                final String value = param.split("=")[1];
                map.put(name, value);
            } else if (paramTokens.length == 3) {
                // very ugly, added for Xen tunneling url
                final String name = paramTokens[0];
                final String value = paramTokens[1] + "=" + paramTokens[2];
                map.put(name, value);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Invalid paramemter in URL found. param: " + param);
                }
            }
        }

        // This is a ugly solution for now. We will do encryption/decryption translation
        // here to make it transparent to rest of the code.
        if (map.get("token") != null) {
            final ConsoleProxyPasswordBasedEncryptor encryptor = new ConsoleProxyPasswordBasedEncryptor(ConsoleProxy.getEncryptorPassword());

            final ConsoleProxyClientParam param = encryptor.decryptObject(ConsoleProxyClientParam.class, map.get("token"));

            // make sure we get information from token only
            guardUserInput(map);
            if (param != null) {
                if (param.getClientHostAddress() != null) {
                    s_logger.debug("decode token. host: " + param.getClientHostAddress());
                    map.put("host", param.getClientHostAddress());
                } else {
                    s_logger.error("decode token. host info is not found!");
                }
                if (param.getClientHostPort() != 0) {
                    s_logger.debug("decode token. port: " + param.getClientHostPort());
                    map.put("port", String.valueOf(param.getClientHostPort()));
                } else {
                    s_logger.error("decode token. port info is not found!");
                }
                if (param.getClientTag() != null) {
                    s_logger.debug("decode token. tag: " + param.getClientTag());
                    map.put("tag", param.getClientTag());
                } else {
                    s_logger.error("decode token. tag info is not found!");
                }
                if (param.getClientHostPassword() != null) {
                    map.put("sid", param.getClientHostPassword());
                } else {
                    s_logger.error("decode token. sid info is not found!");
                }
                if (param.getClientTunnelUrl() != null) {
                    map.put("consoleurl", param.getClientTunnelUrl());
                }
                if (param.getClientTunnelSession() != null) {
                    map.put("sessionref", param.getClientTunnelSession());
                }
                if (param.getTicket() != null) {
                    map.put("ticket", param.getTicket());
                }
                if (param.getLocale() != null) {
                    map.put("locale", param.getLocale());
                }
                if (param.getUsername() != null) {
                    map.put("username", param.getUsername());
                }
                if (param.getPassword() != null) {
                    map.put("password", param.getPassword());
                }
            } else {
                s_logger.error("Unable to decode token");
            }
        } else {
            // we no longer accept information from parameter other than token
            guardUserInput(map);
        }

        return map;
    }

    private static void guardUserInput(final Map<String, String> map) {
        map.remove("host");
        map.remove("port");
        map.remove("tag");
        map.remove("sid");
        map.remove("consoleurl");
        map.remove("sessionref");
        map.remove("ticket");
        map.remove("locale");
        map.remove("username");
        map.remove("password");
    }
}
