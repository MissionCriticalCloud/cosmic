package org.apache.cloudstack.framework.security.keys;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.framework.config.ConfigDepot;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;

import javax.inject.Inject;
import javax.net.ssl.KeyManager;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To be perfectly honest, I'm not sure why we need this class.  This used
 * to be in ManagementServerImpl.  I moved the functionality because it seems
 * many features will need this.  However, the right thing will be for setup
 * and upgrade to take care of key generation.  Here, the methods appear to
 * mainly be used for dynamic generation.  I added this class because after
 * talking to Kelven, we think there will be other functionalities we need
 * to centralize to this class.  We'll see how that works out.
 * <p>
 * There's multiple problems here that we need to fix.
 * - Multiple servers can be generating keys.  This is not atomic.
 * - The functionality of generating the keys should be moved over to setup/upgrade.
 */
public class KeysManagerImpl implements KeysManager, Configurable {
    private static final Logger s_logger = LoggerFactory.getLogger(KeysManagerImpl.class);

    @Inject
    ConfigurationDao _configDao;
    @Inject
    ConfigDepot _configDepot;

    @Override
    public String getEncryptionKey() {
        final String value = EncryptionKey.value();
        if (value == null) {
            _configDao.getValueAndInitIfNotExist(EncryptionKey.key(), EncryptionKey.category(), getBase64EncodedRandomKey(128),
                    EncryptionKey.description());
        }
        return EncryptionKey.value();
    }

    @Override
    public String getEncryptionIV() {
        final String value = EncryptionIV.value();
        if (value == null) {
            _configDao.getValueAndInitIfNotExist(EncryptionIV.key(), EncryptionIV.category(), getBase64EncodedRandomKey(128),
                    EncryptionIV.description());
        }
        return EncryptionIV.value();
    }

    @Override
    @DB
    public void resetEncryptionKeyIV() {

        final SearchBuilder<ConfigurationVO> sb = _configDao.createSearchBuilder();
        sb.and("name1", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.or("name2", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.done();

        final SearchCriteria<ConfigurationVO> sc = sb.create();
        sc.setParameters("name1", EncryptionKey.key());
        sc.setParameters("name2", EncryptionIV.key());

        _configDao.expunge(sc);
    }

    @Override
    public String getHashKey() {
        final String value = HashKey.value();
        if (value == null) {
            _configDao.getValueAndInitIfNotExist(HashKey.key(), HashKey.category(), getBase64EncodedRandomKey(128), HashKey.description());
        }

        return HashKey.value();
    }

    private static String getBase64EncodedRandomKey(final int nBits) {
        final SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            final byte[] keyBytes = new byte[nBits / 8];
            random.nextBytes(keyBytes);
            return Base64.encodeBase64URLSafeString(keyBytes);
        } catch (final NoSuchAlgorithmException e) {
            s_logger.error("Unhandled exception: ", e);
        }
        return null;
    }

    @Override
    public String getConfigComponentName() {
        return KeyManager.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{EncryptionKey, EncryptionIV, HashKey};
    }
}
