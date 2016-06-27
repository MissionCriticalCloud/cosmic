package com.cloud.server.auth;

import com.cloud.user.UserAccount;
import com.cloud.user.dao.UserAccountDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHA256SaltedUserAuthenticator extends AdapterBase implements UserAuthenticator {
    public static final Logger s_logger = LoggerFactory.getLogger(SHA256SaltedUserAuthenticator.class);
    private static final String s_defaultPassword = "000000000000000000000000000=";
    private static final String s_defaultSalt = "0000000000000000000000000000000=";
    private static final int s_saltlen = 32;
    @Inject
    private UserAccountDao _userAccountDao;

    /* (non-Javadoc)
     * @see com.cloud.server.auth.UserAuthenticator#authenticate(java.lang.String, java.lang.String, java.lang.Long, java.util.Map)
     */
    @Override
    public Pair<Boolean, ActionOnFailedAuthentication> authenticate(final String username, final String password, final Long domainId, final Map<String, Object[]>
            requestParameters) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Retrieving user: " + username);
        }

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            s_logger.debug("Username or Password cannot be empty");
            return new Pair<>(false, null);
        }

        boolean realUser = true;
        final UserAccount user = _userAccountDao.getUserAccount(username, domainId);
        if (user == null) {
            s_logger.debug("Unable to find user with " + username + " in domain " + domainId);
            realUser = false;
        }
        /* Fake Data */
        String realPassword = new String(s_defaultPassword);
        byte[] salt = new String(s_defaultSalt).getBytes();
        if (realUser) {
            final String[] storedPassword = user.getPassword().split(":");
            if (storedPassword.length != 2) {
                s_logger.warn("The stored password for " + username + " isn't in the right format for this authenticator");
                realUser = false;
            } else {
                realPassword = storedPassword[1];
                salt = Base64.decode(storedPassword[0]);
            }
        }
        try {
            final String hashedPassword = encode(password, salt);
            /* constantTimeEquals comes first in boolean since we need to thwart timing attacks */
            final boolean result = constantTimeEquals(realPassword, hashedPassword) && realUser;
            ActionOnFailedAuthentication action = null;
            if (!result && realUser) {
                action = ActionOnFailedAuthentication.INCREMENT_INCORRECT_LOGIN_ATTEMPT_COUNT;
            }
            return new Pair<>(result, action);
        } catch (final NoSuchAlgorithmException e) {
            throw new CloudRuntimeException("Unable to hash password", e);
        } catch (final UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable to hash password", e);
        }
    }

    /* (non-Javadoc)
     * @see com.cloud.server.auth.UserAuthenticator#encode(java.lang.String)
     */
    @Override
    public String encode(final String password) {
        // 1. Generate the salt
        final SecureRandom randomGen;
        try {
            randomGen = SecureRandom.getInstance("SHA1PRNG");

            final byte[] salt = new byte[s_saltlen];
            randomGen.nextBytes(salt);

            final String saltString = new String(Base64.encode(salt));
            final String hashString = encode(password, salt);

            // 3. concatenate the two and return
            return saltString + ":" + hashString;
        } catch (final NoSuchAlgorithmException e) {
            throw new CloudRuntimeException("Unable to hash password", e);
        } catch (final UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable to hash password", e);
        }
    }

    public String encode(final String password, final byte[] salt) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        final byte[] passwordBytes = password.getBytes("UTF-8");
        final byte[] hashSource = new byte[passwordBytes.length + salt.length];
        System.arraycopy(passwordBytes, 0, hashSource, 0, passwordBytes.length);
        System.arraycopy(salt, 0, hashSource, passwordBytes.length, salt.length);

        // 2. Hash the password with the salt
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(hashSource);
        final byte[] digest = md.digest();

        return new String(Base64.encode(digest));
    }

    private static boolean constantTimeEquals(final String a, final String b) {
        final byte[] aBytes = a.getBytes();
        final byte[] bBytes = b.getBytes();
        int result = aBytes.length ^ bBytes.length;
        for (int i = 0; i < aBytes.length && i < bBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
