//

//

package com.cloud.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

public class HttpUtilsTest {

    @Test
    public void findCookieTest() {
        Cookie[] cookies = null;
        String cookieName = null;

        // null test
        assertNull(HttpUtils.findCookie(cookies, cookieName));
        cookieName = "";
        assertNull(HttpUtils.findCookie(cookies, cookieName));

        // value test
        cookieName = "daakuBandar";
        cookies = new Cookie[]{new Cookie(cookieName, "someValue")};
        assertNull(HttpUtils.findCookie(cookies, "aalasiLangur"));
        assertNotNull(HttpUtils.findCookie(cookies, cookieName));
    }

    @Test
    public void validateSessionKeyTest() {
        HttpSession session = null;
        Map<String, Object[]> params = null;
        String sessionKeyString = null;
        Cookie[] cookies = null;
        final String sessionKeyValue = "randomUniqueSessionID";

        // session and sessionKeyString null test
        assertFalse(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));
        sessionKeyString = "sessionkey";
        assertFalse(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));

        // param and cookie null test
        session = new MockHttpSession();
        session.setAttribute(sessionKeyString, sessionKeyValue);
        assertFalse(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));

        // param null, cookies not null test
        params = null;
        cookies = new Cookie[]{new Cookie(sessionKeyString, sessionKeyValue)};
        assertFalse(HttpUtils.validateSessionKey(session, params, cookies, "randomString"));
        assertTrue(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));

        // param not null, cookies null test
        params = new HashMap<>();
        params.put(sessionKeyString, new String[]{"randomString"});
        cookies = null;
        assertFalse(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));
        params.put(sessionKeyString, new String[]{sessionKeyValue});
        assertTrue(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));

        // both param and cookies not null test
        params = new HashMap<>();
        cookies = new Cookie[]{new Cookie(sessionKeyString, sessionKeyValue)};
        params.put(sessionKeyString, new String[]{"incorrectValue"});
        assertFalse(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));
        params.put(sessionKeyString, new String[]{sessionKeyValue});
        assertTrue(HttpUtils.validateSessionKey(session, params, cookies, sessionKeyString));
    }
}
