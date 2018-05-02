package com.cloud.agent.transport;

import static org.junit.Assert.assertEquals;

import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.SecStorageFirewallCfgCommand;
import com.cloud.agent.api.UpdateHostPasswordCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.exceptions.UnsupportedVersionException;
import com.cloud.legacymodel.to.NfsTO;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.serializer.GsonHelper;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.command.DownloadCommand;
import com.cloud.storage.to.TemplateObjectTO;
import com.cloud.template.VirtualMachineTemplate;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestTest {
    private static final Logger s_logger = LoggerFactory.getLogger(RequestTest.class);

    @Test
    @Ignore
    public void testSerDeser() {
        s_logger.info("Testing serializing and deserializing works as expected");

        s_logger.info("UpdateHostPasswordCommand should have two parameters that doesn't show in logging");
        final UpdateHostPasswordCommand cmd1 = new UpdateHostPasswordCommand("abc", "def");
        s_logger.info("SecStorageFirewallCfgCommand has a context map that shouldn't show up in debug level");
        final SecStorageFirewallCfgCommand cmd2 = new SecStorageFirewallCfgCommand();
        s_logger.info("GetHostStatsCommand should not show up at all in debug level");
        final GetHostStatsCommand cmd3 = new GetHostStatsCommand("hostguid", "hostname", 101);
        cmd2.addPortConfig("abc", "24", true, "eth0");
        cmd2.addPortConfig("127.0.0.1", "44", false, "eth1");
        final Request sreq = new Request(2, 3, new Command[]{cmd1, cmd2, cmd3}, true, true);
        sreq.setSequence(892403717);

        final Logger logger = LoggerFactory.getLogger(GsonHelper.class);

        //logger.setLevel(Level.DEBUG);
        String log = sreq.log("Debug", true);
        assert (log.contains(UpdateHostPasswordCommand.class.getSimpleName()));
        assert (log.contains(SecStorageFirewallCfgCommand.class.getSimpleName()));
        assert (!log.contains(GetHostStatsCommand.class.getSimpleName()));
        assert (!log.contains("username"));
        assert (!log.contains("password"));

        //logger.setLevel(Level.TRACE);
        log = sreq.log("Trace", true);
        assert (log.contains(UpdateHostPasswordCommand.class.getSimpleName()));
        assert (log.contains(SecStorageFirewallCfgCommand.class.getSimpleName()));
        assert (log.contains(GetHostStatsCommand.class.getSimpleName()));
        assert (!log.contains("username"));
        assert (!log.contains("password"));

        //logger.setLevel(Level.INFO);
        log = sreq.log("Info", true);
        assert (log == null);

        //logger.setLevel(level);

        byte[] bytes = sreq.getBytes();

        assert Request.getSequence(bytes) == 892403717;
        assert Request.getManagementServerId(bytes) == 3;
        assert Request.getAgentId(bytes) == 2;
        assert Request.getViaAgentId(bytes) == 2;
        Request creq = null;
        try {
            creq = Request.parse(bytes);
        } catch (final ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (final UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assert creq != null : "Couldn't get the request back";

        compareRequest(creq, sreq);

        final Answer ans = new Answer(cmd1, true, "No Problem");
        final Response cresp = new Response(creq, ans);

        bytes = cresp.getBytes();

        Response sresp = null;
        try {
            sresp = Response.parse(bytes);
        } catch (final ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (final UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assert sresp != null : "Couldn't get the response back";

        compareRequest(cresp, sresp);
    }

    protected void compareRequest(final Request req1, final Request req2) {
        assert req1.getSequence() == req2.getSequence();
        assert req1.getAgentId() == req2.getAgentId();
        assert req1.getManagementServerId() == req2.getManagementServerId();
        assert req1.isControl() == req2.isControl();
        assert req1.isFromServer() == req2.isFromServer();
        assert req1.executeInSequence() == req2.executeInSequence();
        assert req1.stopOnError() == req2.stopOnError();
        assert req1.getVersion().equals(req2.getVersion());
        assert req1.getViaAgentId() == req2.getViaAgentId();
        final Command[] cmd1 = req1.getCommands();
        final Command[] cmd2 = req2.getCommands();
        for (int i = 0; i < cmd1.length; i++) {
            assert cmd1[i].getClass().equals(cmd2[i].getClass());
        }
    }

    @Test
    public void testSerDeserTO() {
        s_logger.info("Testing serializing and deserializing interface TO works as expected");

        final NfsTO nfs = new NfsTO("nfs://192.168.56.10/opt/storage/secondary", DataStoreRole.Image);
        // SecStorageSetupCommand cmd = new SecStorageSetupCommand(nfs, "nfs://192.168.56.10/opt/storage/secondary", null);
        final ListTemplateCommand cmd = new ListTemplateCommand(nfs);
        final Request sreq = new Request(2, 3, cmd, true);
        sreq.setSequence(892403718);

        final byte[] bytes = sreq.getBytes();

        assert Request.getSequence(bytes) == 892403718;
        assert Request.getManagementServerId(bytes) == 3;
        assert Request.getAgentId(bytes) == 2;
        assert Request.getViaAgentId(bytes) == 2;
        Request creq = null;
        try {
            creq = Request.parse(bytes);
        } catch (final ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (final UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assert creq != null : "Couldn't get the request back";

        compareRequest(creq, sreq);
        assertEquals("nfs://192.168.56.10/opt/storage/secondary", ((NfsTO) ((ListTemplateCommand) creq.getCommand()).getDataStore()).getUrl());
    }

    @Test
    public void testDownload() {
        s_logger.info("Testing Download answer");
        final VirtualMachineTemplate template = Mockito.mock(VirtualMachineTemplate.class);
        Mockito.when(template.getId()).thenReturn(1L);
        Mockito.when(template.getFormat()).thenReturn(ImageFormat.QCOW2);
        Mockito.when(template.getName()).thenReturn("templatename");
        Mockito.when(template.getTemplateType()).thenReturn(TemplateType.USER);
        Mockito.when(template.getDisplayText()).thenReturn("displayText");
        Mockito.when(template.getHypervisorType()).thenReturn(HypervisorType.KVM);
        Mockito.when(template.getUrl()).thenReturn("url");

        final NfsTO nfs = new NfsTO("secUrl", DataStoreRole.Image);
        final TemplateObjectTO to = new TemplateObjectTO(template);
        to.setImageDataStore(nfs);
        final DownloadCommand cmd = new DownloadCommand(to, 30000000l);
        final Request req = new Request(1, 1, cmd, true);

        req.logD("Debug for Download");

        final DownloadAnswer answer = new DownloadAnswer("jobId", 50, "errorString", Status.ABANDONED, "filesystempath", "installpath", 10000000, 20000000, "chksum");
        final Response resp = new Response(req, answer);
        resp.logD("Debug for Download");
    }

    @Test
    public void testCompress() {
        s_logger.info("testCompress");
        final int len = 800000;
        final ByteBuffer inputBuffer = ByteBuffer.allocate(len);
        for (int i = 0; i < len; i++) {
            inputBuffer.array()[i] = 1;
        }
        inputBuffer.limit(len);
        ByteBuffer compressedBuffer = ByteBuffer.allocate(len);
        compressedBuffer = Request.doCompress(inputBuffer, len);
        s_logger.info("compressed length: " + compressedBuffer.limit());
        ByteBuffer decompressedBuffer = ByteBuffer.allocate(len);
        decompressedBuffer = Request.doDecompress(compressedBuffer, len);
        for (int i = 0; i < len; i++) {
            if (inputBuffer.array()[i] != decompressedBuffer.array()[i]) {
                Assert.fail("Fail at " + i);
            }
        }
    }

    @Test
    @Ignore
    public void testLogging() {
        s_logger.info("Testing Logging");
        final GetHostStatsCommand cmd3 = new GetHostStatsCommand("hostguid", "hostname", 101);
        final Request sreq = new Request(2, 3, new Command[]{cmd3}, true, true);
        sreq.setSequence(1);
        final Logger logger = LoggerFactory.getLogger(GsonHelper.class);
        //final Level level = logger.getLevel();

        //logger.setLevel(Level.DEBUG);
        String log = sreq.log("Debug", true);
        assert (log == null);

        log = sreq.log("Debug", false);
        assert (log != null);

        //logger.setLevel(Level.TRACE);
        log = sreq.log("Trace", true);
        assert (log.contains(GetHostStatsCommand.class.getSimpleName()));
        s_logger.debug(log);

        //logger.setLevel(level);
    }
}
