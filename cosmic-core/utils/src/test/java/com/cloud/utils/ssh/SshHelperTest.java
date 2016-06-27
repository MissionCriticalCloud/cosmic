//

//

package com.cloud.utils.ssh;

import java.io.IOException;
import java.io.InputStream;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({Thread.class, SshHelper.class})
@RunWith(PowerMockRunner.class)
public class SshHelperTest {

    @Test
    public void canEndTheSshConnectionTest() throws Exception {
        PowerMockito.spy(SshHelper.class);
        final Session mockedSession = Mockito.mock(Session.class);

        PowerMockito.doReturn(true).when(SshHelper.class, "isChannelConditionEof", Mockito.anyInt());
        Mockito.when(mockedSession.waitForCondition(ChannelCondition.EXIT_STATUS, 1l)).thenReturn(0);
        PowerMockito.doNothing().when(SshHelper.class, "throwSshExceptionIfConditionsTimeout", Mockito.anyInt());

        SshHelper.canEndTheSshConnection(1, mockedSession, 0);

        PowerMockito.verifyStatic();
        SshHelper.isChannelConditionEof(Mockito.anyInt());
        SshHelper.throwSshExceptionIfConditionsTimeout(Mockito.anyInt());

        Mockito.verify(mockedSession).waitForCondition(ChannelCondition.EXIT_STATUS, 1l);
    }

    @Test(expected = SshException.class)
    public void throwSshExceptionIfConditionsTimeout() throws SshException {
        SshHelper.throwSshExceptionIfConditionsTimeout(ChannelCondition.TIMEOUT);
    }

    @Test
    public void doNotThrowSshExceptionIfConditionsClosed() throws SshException {
        SshHelper.throwSshExceptionIfConditionsTimeout(ChannelCondition.CLOSED);
    }

    @Test
    public void doNotThrowSshExceptionIfConditionsStdout() throws SshException {
        SshHelper.throwSshExceptionIfConditionsTimeout(ChannelCondition.STDOUT_DATA);
    }

    @Test
    public void doNotThrowSshExceptionIfConditionsStderr() throws SshException {
        SshHelper.throwSshExceptionIfConditionsTimeout(ChannelCondition.STDERR_DATA);
    }

    @Test
    public void doNotThrowSshExceptionIfConditionsEof() throws SshException {
        SshHelper.throwSshExceptionIfConditionsTimeout(ChannelCondition.EOF);
    }

    @Test
    public void doNotThrowSshExceptionIfConditionsExitStatus() throws SshException {
        SshHelper.throwSshExceptionIfConditionsTimeout(ChannelCondition.EXIT_STATUS);
    }

    @Test
    public void doNotThrowSshExceptionIfConditionsExitSignal() throws SshException {
        SshHelper.throwSshExceptionIfConditionsTimeout(ChannelCondition.EXIT_SIGNAL);
    }

    @Test
    public void isChannelConditionEofTestTimeout() {
        Assert.assertFalse(SshHelper.isChannelConditionEof(ChannelCondition.TIMEOUT));
    }

    @Test
    public void isChannelConditionEofTestClosed() {
        Assert.assertFalse(SshHelper.isChannelConditionEof(ChannelCondition.CLOSED));
    }

    @Test
    public void isChannelConditionEofTestStdout() {
        Assert.assertFalse(SshHelper.isChannelConditionEof(ChannelCondition.STDOUT_DATA));
    }

    @Test
    public void isChannelConditionEofTestStderr() {
        Assert.assertFalse(SshHelper.isChannelConditionEof(ChannelCondition.STDERR_DATA));
    }

    @Test
    public void isChannelConditionEofTestEof() {
        Assert.assertTrue(SshHelper.isChannelConditionEof(ChannelCondition.EOF));
    }

    @Test
    public void isChannelConditionEofTestExitStatus() {
        Assert.assertFalse(SshHelper.isChannelConditionEof(ChannelCondition.EXIT_STATUS));
    }

    @Test
    public void isChannelConditionEofTestExitSignal() {
        Assert.assertFalse(SshHelper.isChannelConditionEof(ChannelCondition.EXIT_SIGNAL));
    }

    @Test(expected = SshException.class)
    public void throwSshExceptionIfStdoutOrStdeerIsNullTestNull() throws SshException {
        SshHelper.throwSshExceptionIfStdoutOrStdeerIsNull(null, null);
    }

    @Test
    public void throwSshExceptionIfStdoutOrStdeerIsNullTestNotNull() throws SshException {
        final InputStream inputStream = Mockito.mock(InputStream.class);
        SshHelper.throwSshExceptionIfStdoutOrStdeerIsNull(inputStream, inputStream);
    }

    @Test
    public void openConnectionSessionTest() throws IOException, InterruptedException {
        final Connection conn = Mockito.mock(Connection.class);
        PowerMockito.mockStatic(Thread.class);
        SshHelper.openConnectionSession(conn);

        Mockito.verify(conn).openSession();

        PowerMockito.verifyStatic();
        Thread.sleep(Mockito.anyLong());
    }
}
