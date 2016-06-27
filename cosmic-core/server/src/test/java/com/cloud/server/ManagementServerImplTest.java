package com.cloud.server;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.user.Account;
import com.cloud.user.SSHKeyPair;
import com.cloud.user.SSHKeyPairVO;
import com.cloud.user.dao.SSHKeyPairDao;
import org.apache.cloudstack.api.command.user.ssh.RegisterSSHKeyPairCmd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManagementServerImplTest {

    @Mock
    RegisterSSHKeyPairCmd regCmd;

    @Mock
    SSHKeyPairVO existingPair;

    @Mock
    Account account;

    @Mock
    SSHKeyPairDao sshKeyPairDao;
    ManagementServerImpl ms = new ManagementServerImpl();

    @Mock
    SSHKeyPair sshKeyPair;

    @Spy
    ManagementServerImpl spy;

    @Test(expected = InvalidParameterValueException.class)
    public void testDuplicateRegistraitons() {
        final String accountName = "account";
        final String publicKeyString = "ssh-rsa very public";
        final String publicKeyMaterial = spy.getPublicKeyFromKeyKeyMaterial(publicKeyString);

        Mockito.doReturn(account).when(spy).getCaller();
        Mockito.doReturn(account).when(spy).getOwner(regCmd);

        Mockito.doNothing().when(spy).checkForKeyByName(regCmd, account);
        Mockito.doReturn(accountName).when(regCmd).getAccountName();

        Mockito.doReturn(publicKeyString).when(regCmd).getPublicKey();
        Mockito.doReturn("name").when(regCmd).getName();

        spy._sshKeyPairDao = sshKeyPairDao;
        Mockito.doReturn(1L).when(account).getAccountId();
        Mockito.doReturn(1L).when(account).getDomainId();
        Mockito.doReturn(Mockito.mock(SSHKeyPairVO.class)).when(sshKeyPairDao).persist(any(SSHKeyPairVO.class));

        when(sshKeyPairDao.findByName(1L, 1L, "name")).thenReturn(null).thenReturn(null);
        when(sshKeyPairDao.findByPublicKey(1L, 1L, publicKeyMaterial)).thenReturn(null).thenReturn(existingPair);

        spy.registerSSHKeyPair(regCmd);
        spy.registerSSHKeyPair(regCmd);
    }

    @Test
    public void testSuccess() {
        final String accountName = "account";
        final String publicKeyString = "ssh-rsa very public";
        final String publicKeyMaterial = spy.getPublicKeyFromKeyKeyMaterial(publicKeyString);

        Mockito.doReturn(1L).when(account).getAccountId();
        Mockito.doReturn(1L).when(account).getAccountId();
        spy._sshKeyPairDao = sshKeyPairDao;

        //Mocking the DAO object functions - NO object found in DB
        Mockito.doReturn(Mockito.mock(SSHKeyPairVO.class)).when(sshKeyPairDao).findByPublicKey(1L, 1L, publicKeyMaterial);
        Mockito.doReturn(Mockito.mock(SSHKeyPairVO.class)).when(sshKeyPairDao).findByName(1L, 1L, accountName);
        Mockito.doReturn(Mockito.mock(SSHKeyPairVO.class)).when(sshKeyPairDao).persist(any(SSHKeyPairVO.class));

        //Mocking the User Params
        Mockito.doReturn(accountName).when(regCmd).getName();
        Mockito.doReturn(publicKeyString).when(regCmd).getPublicKey();
        Mockito.doReturn(account).when(spy).getOwner(regCmd);

        spy.registerSSHKeyPair(regCmd);
        Mockito.verify(spy, Mockito.times(3)).getPublicKeyFromKeyKeyMaterial(anyString());
    }
}
