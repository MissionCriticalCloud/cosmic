package org.apache.cloudstack.region;

import com.cloud.domain.DomainVO;
import com.cloud.user.UserAccount;
import com.cloud.user.UserAccountVO;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for making API calls between peer Regions
 */
public class RegionsApiUtil {
    public static final Logger s_logger = LoggerFactory.getLogger(RegionsApiUtil.class);

    /**
     * Makes an api call using region service end_point, api command and params
     *
     * @param region
     * @param command
     * @param params
     * @return True, if api is successful
     */
    protected static boolean makeAPICall(final Region region, final String command, final List<NameValuePair> params) {
        try {
            final String apiParams = buildParams(command, params);
            final String url = buildUrl(apiParams, region);
            final HttpClient client = new HttpClient();
            final HttpMethod method = new GetMethod(url);
            if (client.executeMethod(method) == 200) {
                return true;
            } else {
                return false;
            }
        } catch (final HttpException e) {
            s_logger.error(e.getMessage());
            return false;
        } catch (final IOException e) {
            s_logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Builds parameters string with command and encoded param values
     *
     * @param command
     * @param params
     * @return
     */
    protected static String buildParams(final String command, final List<NameValuePair> params) {
        final StringBuffer paramString = new StringBuffer("command=" + command);
        final Iterator<NameValuePair> iter = params.iterator();
        try {
            while (iter.hasNext()) {
                final NameValuePair param = iter.next();
                if (param.getValue() != null && !(param.getValue().isEmpty())) {
                    paramString.append("&" + param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8"));
                }
            }
        } catch (final UnsupportedEncodingException e) {
            s_logger.error(e.getMessage());
            return null;
        }
        return paramString.toString();
    }

    /**
     * Build URL for api call using region end_point
     * Parameters are sorted and signed using secret_key
     *
     * @param apiParams
     * @param region
     * @return
     */
    private static String buildUrl(final String apiParams, final Region region) {

        final String apiKey = "";
        final String secretKey = "";
        final String encodedApiKey;
        try {
            encodedApiKey = URLEncoder.encode(apiKey, "UTF-8");

            final List<String> sortedParams = new ArrayList<>();
            sortedParams.add("apikey=" + encodedApiKey.toLowerCase());
            final StringTokenizer st = new StringTokenizer(apiParams, "&");
            String url = null;
            boolean first = true;
            while (st.hasMoreTokens()) {
                final String paramValue = st.nextToken();
                final String param = paramValue.substring(0, paramValue.indexOf("="));
                final String value = paramValue.substring(paramValue.indexOf("=") + 1, paramValue.length());
                if (first) {
                    url = param + "=" + value;
                    first = false;
                } else {
                    url = url + "&" + param + "=" + value;
                }
                sortedParams.add(param.toLowerCase() + "=" + value.toLowerCase());
            }
            Collections.sort(sortedParams);

            //Construct the sorted URL and sign and URL encode the sorted URL with your secret key
            String sortedUrl = null;
            first = true;
            for (final String param : sortedParams) {
                if (first) {
                    sortedUrl = param;
                    first = false;
                } else {
                    sortedUrl = sortedUrl + "&" + param;
                }
            }
            final String encodedSignature = signRequest(sortedUrl, secretKey);

            final String finalUrl = region.getEndPoint() + "?" + apiParams + "&apiKey=" + apiKey + "&signature=" + encodedSignature;

            return finalUrl;
        } catch (final UnsupportedEncodingException e) {
            s_logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 1. Signs a string with a secret key using SHA-1 2. Base64 encode the result 3. URL encode the final result
     *
     * @param request
     * @param key
     * @return
     */
    private static String signRequest(final String request, final String key) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA1");
            final SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            mac.init(keySpec);
            mac.update(request.getBytes());
            final byte[] encryptedBytes = mac.doFinal();
            return URLEncoder.encode(Base64.encodeBase64String(encryptedBytes), "UTF-8");
        } catch (final Exception ex) {
            s_logger.error(ex.getMessage());
            return null;
        }
    }

    /**
     * Makes an api call using region service end_point, api command and params
     * Returns Account object on success
     *
     * @param region
     * @param command
     * @param params
     * @return
     */
    protected static RegionAccount makeAccountAPICall(final Region region, final String command, final List<NameValuePair> params) {
        try {
            final String url = buildUrl(buildParams(command, params), region);
            final HttpClient client = new HttpClient();
            final HttpMethod method = new GetMethod(url);
            if (client.executeMethod(method) == 200) {
                final InputStream is = method.getResponseBodyAsStream();
                //Translate response to Account object
                final XStream xstream = new XStream(new DomDriver());
                xstream.alias("account", RegionAccount.class);
                xstream.alias("user", RegionUser.class);
                xstream.aliasField("id", RegionAccount.class, "uuid");
                xstream.aliasField("name", RegionAccount.class, "accountName");
                xstream.aliasField("accounttype", RegionAccount.class, "type");
                xstream.aliasField("domainid", RegionAccount.class, "domainUuid");
                xstream.aliasField("networkdomain", RegionAccount.class, "networkDomain");
                xstream.aliasField("id", RegionUser.class, "uuid");
                xstream.aliasField("accountId", RegionUser.class, "accountUuid");
                try (ObjectInputStream in = xstream.createObjectInputStream(is)) {
                    return (RegionAccount) in.readObject();
                } catch (final IOException e) {
                    s_logger.error(e.getMessage());
                    return null;
                }
            } else {
                return null;
            }
        } catch (final HttpException e) {
            s_logger.error(e.getMessage());
            return null;
        } catch (final IOException e) {
            s_logger.error(e.getMessage());
            return null;
        } catch (final ClassNotFoundException e) {
            s_logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Makes an api call using region service end_point, api command and params
     * Returns Domain object on success
     *
     * @param region
     * @param command
     * @param params
     * @return
     */
    protected static RegionDomain makeDomainAPICall(final Region region, final String command, final List<NameValuePair> params) {
        try {
            final String url = buildUrl(buildParams(command, params), region);
            final HttpClient client = new HttpClient();
            final HttpMethod method = new GetMethod(url);
            if (client.executeMethod(method) == 200) {
                final InputStream is = method.getResponseBodyAsStream();
                final XStream xstream = new XStream(new DomDriver());
                //Translate response to Domain object
                xstream.alias("domain", RegionDomain.class);
                xstream.aliasField("id", RegionDomain.class, "uuid");
                xstream.aliasField("parentdomainid", RegionDomain.class, "parentUuid");
                xstream.aliasField("networkdomain", DomainVO.class, "networkDomain");
                try (ObjectInputStream in = xstream.createObjectInputStream(is)) {
                    return (RegionDomain) in.readObject();
                } catch (final IOException e) {
                    s_logger.error(e.getMessage());
                    return null;
                }
            } else {
                return null;
            }
        } catch (final HttpException e) {
            s_logger.error(e.getMessage());
            return null;
        } catch (final IOException e) {
            s_logger.error(e.getMessage());
            return null;
        } catch (final ClassNotFoundException e) {
            s_logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Makes an api call using region service end_point, api command and params
     * Returns UserAccount object on success
     *
     * @param region
     * @param command
     * @param params
     * @return
     */
    protected static UserAccount makeUserAccountAPICall(final Region region, final String command, final List<NameValuePair> params) {
        try {
            final String url = buildUrl(buildParams(command, params), region);
            final HttpClient client = new HttpClient();
            final HttpMethod method = new GetMethod(url);
            if (client.executeMethod(method) == 200) {
                final InputStream is = method.getResponseBodyAsStream();
                final XStream xstream = new XStream(new DomDriver());
                xstream.alias("useraccount", UserAccountVO.class);
                xstream.aliasField("id", UserAccountVO.class, "uuid");
                try (ObjectInputStream in = xstream.createObjectInputStream(is)) {
                    return (UserAccountVO) in.readObject();
                } catch (final IOException e) {
                    s_logger.error(e.getMessage());
                    return null;
                }
            } else {
                return null;
            }
        } catch (final HttpException e) {
            s_logger.error(e.getMessage());
            return null;
        } catch (final IOException e) {
            s_logger.error(e.getMessage());
            return null;
        } catch (final ClassNotFoundException e) {
            s_logger.error(e.getMessage());
            return null;
        }
    }
}
