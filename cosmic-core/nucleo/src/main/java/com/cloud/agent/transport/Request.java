//

//

package com.cloud.agent.transport;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.SecStorageFirewallCfgCommand.PortConfig;
import com.cloud.exception.UnsupportedVersionException;
import com.cloud.serializer.GsonHelper;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.StringUtils;
import com.cloud.utils.exception.CloudRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Request is a simple wrapper around command and answer to add sequencing,
 * versioning, and flags. Note that the version here represents the changes
 * in the over the wire protocol. For example, if we decide to not use Gson.
 * It does not version the changes in the actual commands. That's expected
 * to be done by adding new classes to the command and answer list.
 * <p>
 * A request looks as follows:
 * 1. Version - 1 byte;
 * 2. Flags - 3 bytes;
 * 3. Sequence - 8 bytes;
 * 4. Length - 4 bytes;
 * 5. ManagementServerId - 8 bytes;
 * 6. AgentId - 8 bytes;
 * 7. Data Package.
 */
public class Request {
    protected static final Gson s_gson = GsonHelper.getGson();
    protected static final Gson s_gogger = GsonHelper.getGsonLogger();
    protected static final Logger s_gsonLogger = GsonHelper.getLogger();
    protected static final short FLAG_RESPONSE = 0x0;
    protected static final short FLAG_REQUEST = 0x1;

    protected static final short FLAG_STOP_ON_ERROR = 0x2;
    protected static final short FLAG_IN_SEQUENCE = 0x4;
    protected static final short FLAG_FROM_SERVER = 0x20;
    protected static final short FLAG_CONTROL = 0x40;
    protected static final short FLAG_COMPRESSED = 0x80;
    private static final Logger s_logger = Logger.getLogger(Request.class);
    protected Version _ver;
    protected long _session;
    protected long _seq;
    protected short _flags;
    protected long _mgmtId;
    protected long _via;
    protected long _agentId;
    protected Command[] _cmds;
    protected String _content;
    protected String _agentName;

    protected Request() {
    }

    protected Request(final Version ver, final long seq, final long agentId, final long mgmtId, final long via, final short flags, final String content) {
        this(ver, seq, agentId, mgmtId, via, flags, (Command[]) null);
        _content = content;
    }

    protected Request(final Version ver, final long seq, final long agentId, final long mgmtId, final long via, final short flags, final Command[] cmds) {
        _ver = ver;
        _cmds = cmds;
        _flags = flags;
        _seq = seq;
        _via = via;
        _agentId = agentId;
        _mgmtId = mgmtId;
        setInSequence(cmds);
    }

    protected void setInSequence(final Command[] cmds) {
        if (cmds == null) {
            return;
        }
        for (final Command cmd : cmds) {
            if (cmd.executeInSequence()) {
                setInSequence(true);
                break;
            }
        }
    }

    private void setInSequence(final boolean inSequence) {
        _flags |= (inSequence ? FLAG_IN_SEQUENCE : 0);
    }

    public Request(final long agentId, final long mgmtId, final Command command, final boolean fromServer) {
        this(agentId, mgmtId, new Command[]{command}, true, fromServer);
    }

    public Request(final long agentId, final long mgmtId, final Command[] cmds, final boolean stopOnError, final boolean fromServer) {
        this(Version.v1, -1l, agentId, mgmtId, (short) 0, cmds);
        setStopOnError(stopOnError);
        setFromServer(fromServer);
    }

    protected Request(final Version ver, final long seq, final long agentId, final long mgmtId, final short flags, final Command[] cmds) {
        this(ver, seq, agentId, mgmtId, agentId, flags, cmds);
    }

    private void setStopOnError(final boolean stopOnError) {
        _flags |= (stopOnError ? FLAG_STOP_ON_ERROR : 0);
    }

    public Request(final long agentId, final String agentName, final long mgmtId, final Command[] cmds, final boolean stopOnError, final boolean fromServer) {
        this(agentId, mgmtId, cmds, stopOnError, fromServer);
        setAgentName(agentName);
    }

    private void setAgentName(final String agentName) {
        _agentName = agentName;
    }

    protected Request(final Request that, final Command[] cmds) {
        _ver = that._ver;
        _seq = that._seq;
        setInSequence(that.executeInSequence());
        setStopOnError(that.stopOnError());
        _cmds = cmds;
        _mgmtId = that._mgmtId;
        _via = that._via;
        _agentId = that._agentId;
        _agentName = that._agentName;
        setFromServer(!that.isFromServer());
    }

    public boolean executeInSequence() {
        return (_flags & FLAG_IN_SEQUENCE) > 0;
    }

    public boolean stopOnError() {
        return (_flags & FLAG_STOP_ON_ERROR) > 0;
    }

    public boolean isFromServer() {
        return (_flags & FLAG_FROM_SERVER) > 0;
    }

    private void setFromServer(final boolean fromServer) {
        _flags |= (fromServer ? FLAG_FROM_SERVER : 0);
    }

    /**
     * Factory method for Request and Response. It expects the bytes to be
     * correctly formed so it's possible that it throws underflow exceptions
     * but you shouldn't be concerned about that since that all bytes sent in
     * should already be formatted correctly.
     *
     * @param bytes bytes to be converted.
     * @return Request or Response depending on the data.
     * @throws ClassNotFoundException if the Command or Answer can not be formed.
     * @throws
     */
    public static Request parse(final byte[] bytes) throws ClassNotFoundException, UnsupportedVersionException {
        ByteBuffer buff = ByteBuffer.wrap(bytes);
        final byte ver = buff.get();
        final Version version = Version.get(ver);
        if (version.ordinal() != Version.v1.ordinal() && version.ordinal() != Version.v3.ordinal()) {
            throw new UnsupportedVersionException("This version is no longer supported: " + version.toString(), UnsupportedVersionException.IncompatibleVersion);
        }
        buff.get();
        final short flags = buff.getShort();
        final boolean isRequest = (flags & FLAG_REQUEST) > 0;

        final long seq = buff.getLong();
        // The size here is uncompressed size, if the data is compressed.
        final int size = buff.getInt();
        final long mgmtId = buff.getLong();
        final long agentId = buff.getLong();

        final long via;
        if (version.ordinal() == Version.v1.ordinal()) {
            via = buff.getLong();
        } else {
            via = agentId;
        }

        if ((flags & FLAG_COMPRESSED) != 0) {
            buff = doDecompress(buff, size);
        }

        byte[] command = null;
        int offset = 0;
        if (buff.hasArray()) {
            command = buff.array();
            offset = buff.arrayOffset() + buff.position();
        } else {
            command = new byte[buff.remaining()];
            buff.get(command);
            offset = 0;
        }

        final String content = new String(command, offset, command.length - offset);

        if (isRequest) {
            return new Request(version, seq, agentId, mgmtId, via, flags, content);
        } else {
            return new Response(Version.get(ver), seq, agentId, mgmtId, via, flags, content);
        }
    }

    public static ByteBuffer doDecompress(final ByteBuffer buffer, final int length) {
        final byte[] byteArrayIn = new byte[1024];
        final ByteArrayInputStream byteIn;
        if (buffer.hasArray()) {
            byteIn = new ByteArrayInputStream(buffer.array(), buffer.position() + buffer.arrayOffset(), buffer.remaining());
        } else {
            final byte[] array = new byte[buffer.limit() - buffer.position()];
            buffer.get(array);
            byteIn = new ByteArrayInputStream(array);
        }
        final ByteBuffer retBuff = ByteBuffer.allocate(length);
        int len = 0;
        try {
            final GZIPInputStream in = new GZIPInputStream(byteIn);
            while ((len = in.read(byteArrayIn)) > 0) {
                retBuff.put(byteArrayIn, 0, len);
            }
            in.close();
        } catch (final IOException e) {
            s_logger.error("Fail to decompress the request!", e);
        }
        retBuff.flip();
        return retBuff;
    }

    public static boolean requiresSequentialExecution(final byte[] bytes) {
        return (bytes[3] & FLAG_IN_SEQUENCE) > 0;
    }

    public static Version getVersion(final byte[] bytes) throws UnsupportedVersionException {
        try {
            return Version.get(bytes[0]);
        } catch (final UnsupportedVersionException e) {
            throw new CloudRuntimeException("Unsupported version: " + bytes[0]);
        }
    }

    public static long getManagementServerId(final byte[] bytes) {
        return NumbersUtil.bytesToLong(bytes, 16);
    }

    public static long getAgentId(final byte[] bytes) {
        return NumbersUtil.bytesToLong(bytes, 24);
    }

    public static long getViaAgentId(final byte[] bytes) {
        return NumbersUtil.bytesToLong(bytes, 32);
    }

    public static boolean fromServer(final byte[] bytes) {
        return (bytes[3] & FLAG_FROM_SERVER) > 0;
    }

    public static boolean isRequest(final byte[] bytes) {
        return (bytes[3] & FLAG_REQUEST) > 0;
    }

    public static long getSequence(final byte[] bytes) {
        return NumbersUtil.bytesToLong(bytes, 4);
    }

    public static boolean isControl(final byte[] bytes) {
        return (bytes[3] & FLAG_CONTROL) > 0;
    }

    public boolean isControl() {
        return (_flags & FLAG_CONTROL) > 0;
    }

    public void setControl(final boolean control) {
        _flags |= (control ? FLAG_CONTROL : 0);
    }

    public long getManagementServerId() {
        return _mgmtId;
    }

    public Version getVersion() {
        return _ver;
    }

    public void setVia(final long viaId) {
        _via = viaId;
    }

    public long getSequence() {
        return _seq;
    }

    public void setSequence(final long seq) {
        _seq = seq;
    }

    public Command getCommand() {
        getCommands();
        return _cmds[0];
    }

    public Command[] getCommands() {
        if (_cmds == null) {
            try {
                final StringReader reader = new StringReader(_content);
                final JsonReader jsonReader = new JsonReader(reader);
                jsonReader.setLenient(true);
                _cmds = s_gson.fromJson(jsonReader, (Type) Command[].class);
            } catch (final RuntimeException e) {
                s_logger.error("Caught problem with " + _content, e);
                throw e;
            }
        }
        return _cmds;
    }

    public byte[] getBytes() {
        final ByteBuffer[] buffers = toBytes();
        final int len1 = buffers[0].remaining();
        final int len2 = buffers[1].remaining();
        final byte[] bytes = new byte[len1 + len2];
        buffers[0].get(bytes, 0, len1);
        buffers[1].get(bytes, len1, len2);
        return bytes;
    }

    public ByteBuffer[] toBytes() {
        final ByteBuffer[] buffers = new ByteBuffer[2];
        ByteBuffer tmp;

        if (_content == null) {
            _content = s_gson.toJson(_cmds, _cmds.getClass());
        }
        tmp = ByteBuffer.wrap(_content.getBytes());
        final int capacity = tmp.capacity();
        /* Check if we need to compress the data */
        if (capacity >= 8192) {
            tmp = doCompress(tmp, capacity);
            _flags |= FLAG_COMPRESSED;
        }
        buffers[1] = tmp;
        buffers[0] = serializeHeader(capacity);

        return buffers;
    }

    public static ByteBuffer doCompress(final ByteBuffer buffer, final int length) {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream(length);
        final byte[] array;
        if (buffer.hasArray()) {
            array = buffer.array();
        } else {
            array = new byte[buffer.capacity()];
            buffer.get(array);
        }
        try {
            final GZIPOutputStream out = new GZIPOutputStream(byteOut, length);
            out.write(array);
            out.finish();
            out.close();
        } catch (final IOException e) {
            s_logger.error("Fail to compress the request!", e);
        }
        return ByteBuffer.wrap(byteOut.toByteArray());
    }

    protected ByteBuffer serializeHeader(final int contentSize) {
        final ByteBuffer buffer = ByteBuffer.allocate(40);
        buffer.put(getVersionInByte());
        buffer.put((byte) 0);
        buffer.putShort(getFlags());
        buffer.putLong(_seq);
        // The size here is uncompressed size, if the data is compressed.
        buffer.putInt(contentSize);
        buffer.putLong(_mgmtId);
        buffer.putLong(_agentId);
        buffer.putLong(_via);
        buffer.flip();

        return buffer;
    }

    protected byte getVersionInByte() {
        return (byte) _ver.ordinal();
    }

    protected short getFlags() {
        return (short) (((this instanceof Response) ? FLAG_RESPONSE : FLAG_REQUEST) | _flags);
    }

    public void logD(final String msg) {
        logD(msg, true);
    }

    public void logD(final String msg, final boolean logContent) {
        if (s_logger.isDebugEnabled()) {
            final String log = log(msg, logContent, Level.DEBUG);
            if (log != null) {
                s_logger.debug(log);
            }
        }
    }

    protected String log(final String msg, final boolean logContent, final Level level) {
        final StringBuilder content = new StringBuilder();
        if (logContent) {
            if (_cmds == null) {
                try {
                    _cmds = s_gson.fromJson(_content, this instanceof Response ? Answer[].class : Command[].class);
                } catch (final RuntimeException e) {
                    s_logger.error("Unable to convert to json: " + _content);
                    throw e;
                }
            }
            try {
                s_gogger.toJson(_cmds, content);
            } catch (final Throwable e) {
                final StringBuilder buff = new StringBuilder();
                for (final Command cmd : _cmds) {
                    buff.append(cmd.getClass().getSimpleName()).append("/");
                }
                s_logger.error("Gson serialization error " + buff.toString(), e);
                assert false : "More gson errors on " + buff.toString();
                return "";
            }
            if (content.length() <= (1 + _cmds.length * 3)) {
                return null;
            }
        } else {
            if (_cmds == null) {
                _cmds = s_gson.fromJson(_content, this instanceof Response ? Answer[].class : Command[].class);
            }
            content.append("{ ");
            for (final Command cmd : _cmds) {
                content.append(cmd.getClass().getSimpleName()).append(", ");
            }
            content.replace(content.length() - 2, content.length(), " }");
        }

        final StringBuilder buf = new StringBuilder("Seq ");

        buf.append(_agentId).append("-").append(_seq).append(": ");

        buf.append(msg);
        buf.append(" { ").append(getType());
        if (_agentName != null) {
            buf.append(", MgmtId: ").append(_mgmtId).append(", via: ").append(_via).append("(" + _agentName + ")");
        } else {
            buf.append(", MgmtId: ").append(_mgmtId).append(", via: ").append(_via);
        }
        buf.append(", Ver: ").append(_ver.toString());
        buf.append(", Flags: ").append(Integer.toBinaryString(getFlags())).append(", ");
        final String cleanContent = content.toString();
        if (cleanContent.contains("password")) {
            buf.append(cleanPassword(cleanContent));
        } else {
            buf.append(content);
        }
        buf.append(" }");
        return buf.toString();
    }

    protected String getType() {
        return "Cmd ";
    }

    public static String cleanPassword(final String logString) {
        String cleanLogString = null;
        if (logString != null) {
            cleanLogString = logString;
            final String[] temp = logString.split(",");
            int i = 0;
            if (temp != null) {
                while (i < temp.length) {
                    temp[i] = StringUtils.cleanString(temp[i]);
                    i++;
                }
                final List<String> stringList = new ArrayList<>();
                Collections.addAll(stringList, temp);
                cleanLogString = StringUtils.join(stringList, ",");
            }
        }
        return cleanLogString;
    }

    public void logT(final String msg, final boolean logD) {
        if (s_logger.isTraceEnabled()) {
            final String log = log(msg, true, Level.TRACE);
            if (log != null) {
                s_logger.trace(log);
            }
        } else if (logD && s_logger.isDebugEnabled()) {
            final String log = log(msg, false, Level.DEBUG);
            if (log != null) {
                s_logger.debug(log);
            }
        }
    }

    @Override
    public String toString() {
        return log("", true, Level.DEBUG);
    }

    public long getAgentId() {
        return _agentId;
    }

    public void setAgentId(final long agentId) {
        _agentId = agentId;
    }

    public long getViaAgentId() {
        return _via;
    }

    public enum Version {
        v1, // using gson to marshall
        v2, // now using gson as marshalled.
        v3; // Adding routing information into the Request data structure.

        public static Version get(final byte ver) throws UnsupportedVersionException {
            for (final Version version : Version.values()) {
                if (ver == version.ordinal()) {
                    return version;
                }
            }
            throw new UnsupportedVersionException("Can't lookup version: " + ver, UnsupportedVersionException.UnknownVersion);
        }
    }

    public static class NwGroupsCommandTypeAdaptor implements JsonDeserializer<Pair<Long, Long>>, JsonSerializer<Pair<Long, Long>> {

        public NwGroupsCommandTypeAdaptor() {
        }

        @Override
        public JsonElement serialize(final Pair<Long, Long> src, final java.lang.reflect.Type typeOfSrc, final JsonSerializationContext context) {
            final JsonArray array = new JsonArray();
            if (src.first() != null) {
                array.add(s_gson.toJsonTree(src.first()));
            } else {
                array.add(new JsonNull());
            }

            if (src.second() != null) {
                array.add(s_gson.toJsonTree(src.second()));
            } else {
                array.add(new JsonNull());
            }

            return array;
        }

        @Override
        public Pair<Long, Long> deserialize(final JsonElement json, final java.lang.reflect.Type type, final JsonDeserializationContext context) throws JsonParseException {
            final Pair<Long, Long> pairs = new Pair<>(null, null);
            final JsonArray array = json.getAsJsonArray();
            if (array.size() != 2) {
                return pairs;
            }
            JsonElement element = array.get(0);
            if (!element.isJsonNull()) {
                pairs.first(element.getAsLong());
            }

            element = array.get(1);
            if (!element.isJsonNull()) {
                pairs.second(element.getAsLong());
            }

            return pairs;
        }
    }

    public static class PortConfigListTypeAdaptor implements JsonDeserializer<List<PortConfig>>, JsonSerializer<List<PortConfig>> {

        public PortConfigListTypeAdaptor() {
        }

        @Override
        public JsonElement serialize(final List<PortConfig> src, final Type typeOfSrc, final JsonSerializationContext context) {
            if (src.size() == 0) {
                return new JsonNull();
            }
            final JsonArray array = new JsonArray();
            for (final PortConfig pc : src) {
                array.add(s_gson.toJsonTree(pc));
            }

            return array;
        }

        @Override
        public List<PortConfig> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonNull()) {
                return new ArrayList<>();
            }
            final List<PortConfig> pcs = new ArrayList<>();
            final JsonArray array = json.getAsJsonArray();
            final Iterator<JsonElement> it = array.iterator();
            while (it.hasNext()) {
                final JsonElement element = it.next();
                pcs.add(s_gson.fromJson(element, PortConfig.class));
            }
            return pcs;
        }
    }
}
