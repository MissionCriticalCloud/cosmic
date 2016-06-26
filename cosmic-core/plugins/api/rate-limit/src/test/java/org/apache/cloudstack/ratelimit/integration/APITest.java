package org.apache.cloudstack.ratelimit.integration;

import com.cloud.api.ApiGsonHelper;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.api.response.SuccessResponse;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;

/**
 * Base class for API Test
 */
public abstract class APITest {

    protected String rootUrl = "http://localhost:8080/client/api";
    protected String sessionKey = null;
    protected String cookieToSent = null;

    protected String createMD5String(final String password) {
        final MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException e) {
            throw new CloudRuntimeException("Error", e);
        }

        md5.reset();
        final BigInteger pwInt = new BigInteger(1, md5.digest(password.getBytes()));

        // make sure our MD5 hash value is 32 digits long...
        final StringBuffer sb = new StringBuffer();
        final String pwStr = pwInt.toString(16);
        final int padding = 32 - pwStr.length();
        for (int i = 0; i < padding; i++) {
            sb.append('0');
        }
        sb.append(pwStr);
        return sb.toString();
    }

    /**
     * Login call
     *
     * @param username user name
     * @param password password (plain password, we will do MD5 hash here for you)
     * @return login response string
     */
    protected void login(final String username, final String password) {
        //String md5Psw = createMD5String(password);
        // send login request
        final HashMap<String, String> params = new HashMap<>();
        params.put("response", "json");
        params.put("username", username);
        params.put("password", password);
        final String result = this.sendRequest("login", params);
        final LoginResponse loginResp = (LoginResponse) fromSerializedString(result, LoginResponse.class);
        sessionKey = loginResp.getSessionkey();
    }

    /**
     * Sending an api request through Http GET
     *
     * @param command command name
     * @param params  command query parameters in a HashMap
     * @return http request response string
     */
    protected String sendRequest(final String command, final HashMap<String, String> params) {
        try {
            // Construct query string
            final StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("command=");
            sBuilder.append(command);
            if (params != null && params.size() > 0) {
                final Iterator<String> keys = params.keySet().iterator();
                while (keys.hasNext()) {
                    final String key = keys.next();
                    sBuilder.append("&");
                    sBuilder.append(key);
                    sBuilder.append("=");
                    sBuilder.append(URLEncoder.encode(params.get(key), "UTF-8"));
                }
            }

            // Construct request url
            final String reqUrl = rootUrl + "?" + sBuilder.toString();

            // Send Http GET request
            final URL url = new URL(reqUrl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (!command.equals("login") && cookieToSent != null) {
                // add the cookie to a request
                conn.setRequestProperty("Cookie", cookieToSent);
            }
            conn.connect();

            if (command.equals("login")) {
                // if it is login call, store cookie
                String headerName = null;
                for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
                    if (headerName.equals("Set-Cookie")) {
                        String cookie = conn.getHeaderField(i);
                        cookie = cookie.substring(0, cookie.indexOf(";"));
                        final String cookieName = cookie.substring(0, cookie.indexOf("="));
                        final String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
                        cookieToSent = cookieName + "=" + cookieValue;
                    }
                }
            }

            // Get the response
            final StringBuilder response = new StringBuilder();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            try {
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
            } catch (final EOFException ex) {
                // ignore this exception
                System.out.println("EOF exception due to java bug");
            }
            rd.close();

            return response.toString();
        } catch (final Exception e) {
            throw new CloudRuntimeException("Problem with sending api request", e);
        }
    }

    protected Object fromSerializedString(final String result, final Class<?> repCls) {
        try {
            if (result != null && !result.isEmpty()) {
                // get real content
                final int start;
                final int end;
                if (repCls == LoginResponse.class || repCls == SuccessResponse.class) {

                    start = result.indexOf('{', result.indexOf('{') + 1); // find
                    // the
                    // second
                    // {

                    end = result.lastIndexOf('}', result.lastIndexOf('}') - 1); // find
                    // the
                    // second
                    // }
                    // backwards

                } else {
                    // get real content
                    start = result.indexOf('{', result.indexOf('{', result.indexOf('{') + 1) + 1); // find
                    // the
                    // third
                    // {
                    end = result.lastIndexOf('}', result.lastIndexOf('}', result.lastIndexOf('}') - 1) - 1); // find
                    // the
                    // third
                    // }
                    // backwards
                }
                if (start < 0 || end < 0) {
                    throw new CloudRuntimeException("Response format is wrong: " + result);
                }
                final String content = result.substring(start, end + 1);
                final Gson gson = ApiGsonHelper.getBuilder().create();
                return gson.fromJson(content, repCls);
            }
            return null;
        } catch (final RuntimeException e) {
            throw new CloudRuntimeException("Caught runtime exception when doing GSON deserialization on: " + result, e);
        }
    }
}
