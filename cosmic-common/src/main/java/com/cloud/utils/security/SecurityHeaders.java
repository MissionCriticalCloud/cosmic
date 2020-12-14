package com.cloud.utils.security;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.Headers;

public class SecurityHeaders {
    static Map<String, String> headers  = new HashMap<String, String>() {{
        put("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        put("X-XSS-Protection", "0");
        put("Content-Security-Policy", "unsafe-inline");
        put("X-Content-Type-Options", "nosniff");
    }};

    public static <T>void addSecurityHeaders(final T hds) {
        if (hds instanceof Headers) {
            headers.forEach(((Headers) hds)::set);
        }
        if (hds instanceof HttpServletResponse) {
            headers.forEach((k, v) -> {
                if (((HttpServletResponse) hds).containsHeader(k)) {
                    ((HttpServletResponse) hds).setHeader(k, v);
                } else {
                    ((HttpServletResponse) hds).addHeader(k, v);
                }
            });
        }
    }

    public static void addSecurityHeaders(final Headers hds, Map<String, String> addHeaders) {
        addSecurityHeaders(hds);
        addHeaders.forEach(hds::set);
    }
}
