package src.com.cloud.server.auth.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.cloud.server.auth.SHA256SaltedUserAuthenticator;
import com.cloud.user.UserAccount;
import com.cloud.user.dao.UserAccountDao;

import javax.naming.ConfigurationException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatorTest {

    @Mock
    UserAccount adminAccount;

    @Mock
    UserAccount adminAccount20Byte;

    @Mock
    UserAccountDao _userAccountDao;

    @InjectMocks
    SHA256SaltedUserAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        try {
            authenticator.configure("SHA256", Collections.<String, Object>emptyMap());
        } catch (final ConfigurationException e) {
            fail(e.toString());
        }

        when(_userAccountDao.getUserAccount("admin", 0L)).thenReturn(adminAccount);
        when(_userAccountDao.getUserAccount("admin20Byte", 0L)).thenReturn(adminAccount20Byte);
        when(_userAccountDao.getUserAccount("fake", 0L)).thenReturn(null);
        //32 byte salt, and password="password"
        when(adminAccount.getPassword()).thenReturn("WS3UHhBPKHZeV+G3jnn7G2N3luXgLSfL+2ORDieXa1U=:VhuFOrOU2IpsjKYH8cH1VDaDBh/VivjMcuADjeEbIig=");
        //20 byte salt, and password="password"
        when(adminAccount20Byte.getPassword()).thenReturn("QL2NsxVEmRuDaNRkvIyADny7C5w=:JoegiytiWnoBAxmSD/PwBZZYqkr746x2KzPrZNw4NgI=");
    }

    @Test
    public void testEncode() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        final String encodedPassword = authenticator.encode("password");

        final String[] storedPassword = encodedPassword.split(":");
        assertEquals("hash must consist of two components", storedPassword.length, 2);

        final byte[] salt = Base64.decode(storedPassword[0]);
        final String hashedPassword = authenticator.encode("password", salt);

        assertEquals("compare hashes", storedPassword[1], hashedPassword);
    }

    @Test
    public void testAuthentication() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        final Map<String, Object[]> dummyMap = new HashMap<>();
        assertEquals("32 byte salt authenticated", true, authenticator.authenticate("admin", "password", 0L, dummyMap).first());
        assertEquals("20 byte salt authenticated", true, authenticator.authenticate("admin20Byte", "password", 0L, dummyMap).first());
        assertEquals("fake user not authenticated", false, authenticator.authenticate("fake", "fake", 0L, dummyMap).first());
        assertEquals("bad password not authenticated", false, authenticator.authenticate("admin", "fake", 0L, dummyMap).first());
        assertEquals("20 byte user bad password not authenticated", false, authenticator.authenticate("admin20Byte", "fake", 0L, dummyMap).first());
    }

    //    @Test
    //    public void testTiming() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    //        Map<String, Object[]> dummyMap = new HashMap<String, Object[]>();
    //        Double threshold = (double)500000; //half a millisecond
    //
    //        Long t1 = System.nanoTime();
    //        authenticator.authenticate("admin", "password", 0L, dummyMap);
    //        Long t2 = System.nanoTime();
    //        authenticator.authenticate("admin20Byte", "password", 0L, dummyMap);
    //        Long t3 = System.nanoTime();
    //        authenticator.authenticate("fake", "fake", 0L, dummyMap);
    //        Long t4 = System.nanoTime();
    //        authenticator.authenticate("admin", "fake", 0L, dummyMap);
    //        Long t5 = System.nanoTime();
    //        Long diff1 = t2 - t1;
    //        Long diff2 = t3 - t2;
    //        Long diff3 = t4 - t3;
    //        Long diff4 = t5 - t4;
    //        Assert.assertTrue("All computation times within " + threshold / 1000000 + " milisecond",
    //                (diff1 <= threshold) && (diff2 <= threshold) && (diff3 <= threshold) && (diff4 <= threshold));
    //    }
}
