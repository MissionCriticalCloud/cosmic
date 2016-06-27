package com.cloud.server.auth;

import com.cloud.utils.Pair;
import com.cloud.utils.component.Adapter;

import java.util.Map;

/**
 * which UserAuthenticator to user in components.xml.
 */
public interface UserAuthenticator extends Adapter {
    /**
     * @param username
     * @param password
     * @param domainId
     * @return the pair of 2 booleans - first identifies the success of authenciation, the second - whether to increase incorrect login attempts count in case of failed
     * authentication
     */
    public Pair<Boolean, ActionOnFailedAuthentication> authenticate(String username, String password, Long domainId, Map<String, Object[]> requestParameters);

    /**
     * @param password
     * @return the encoded password
     */
    public String encode(String password);

    public enum ActionOnFailedAuthentication {
        INCREMENT_INCORRECT_LOGIN_ATTEMPT_COUNT
    }
}
