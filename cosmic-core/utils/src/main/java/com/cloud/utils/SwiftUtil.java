//

//

package com.cloud.utils;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwiftUtil {
    private static final long SWIFT_MAX_SIZE = 5L * 1024L * 1024L * 1024L;
    private static final Logger logger = LoggerFactory.getLogger(SwiftUtil.class);

    public static boolean postMeta(final SwiftClientCfg cfg, final String container, final String object, final Map<String, String> metas) {
        final String swiftCli = getSwiftCLIPath();
        final StringBuilder cms = new StringBuilder();
        for (final Map.Entry<String, String> entry : metas.entrySet()) {
            cms.append(" -m ");
            cms.append(entry.getKey());
            cms.append(":");
            cms.append(entry.getValue());
            cms.append(" ");
        }
        final Script command = new Script("/bin/bash", logger);
        command.add("-c");
        command.add("/usr/bin/python " + swiftCli + " -A " + cfg.getEndPoint() + " -U " + cfg.getAccount() + ":" + cfg.getUserName() + " -K " + cfg.getKey() + " post " +
                container + " " + object + " " + cms.toString());
        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        final String result = command.execute(parser);
        if (result != null) {
            throw new CloudRuntimeException("Failed to post meta" + result);
        }
        return true;
    }

    private static String getSwiftCLIPath() {
        final String swiftCLI = Script.findScript("scripts/storage/secondary", "swift");
        if (swiftCLI == null) {
            logger.debug("Can't find swift cli at scripts/storage/secondary/swift");
            throw new CloudRuntimeException("Can't find swift cli at scripts/storage/secondary/swift");
        }
        return swiftCLI;
    }

    public static String putObject(final SwiftClientCfg cfg, final File srcFile, final String container, String fileName) {
        final String swiftCli = getSwiftCLIPath();
        if (fileName == null) {
            fileName = srcFile.getName();
        }
        final String srcDirectory = srcFile.getParent();
        final Script command = new Script("/bin/bash", logger);
        final long size = srcFile.length();
        command.add("-c");
        if (size <= SWIFT_MAX_SIZE) {
            command.add("cd " + srcDirectory + ";/usr/bin/python " + swiftCli + " -A " + cfg.getEndPoint() + " -U " + cfg.getAccount() + ":" + cfg.getUserName() +
                    " -K " + cfg.getKey() + " upload " + container + " " + fileName);
        } else {
            command.add("cd " + srcDirectory + ";/usr/bin/python " + swiftCli + " -A " + cfg.getEndPoint() + " -U " + cfg.getAccount() + ":" + cfg.getUserName() +
                    " -K " + cfg.getKey() + " upload -S " + SWIFT_MAX_SIZE + " " + container + " " + fileName);
        }
        final OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        final String result = command.execute(parser);
        if (result != null) {
            throw new CloudRuntimeException("Failed to upload file: " + result);
        }

        if (parser.getLines() != null) {
            final String[] lines = parser.getLines().split("\\n");
            for (final String line : lines) {
                if (line.contains("Errno") || line.contains("failed") || line.contains("not found")) {
                    throw new CloudRuntimeException("Failed to upload file: " + Arrays.toString(lines));
                }
            }
        }

        return container + File.separator + srcFile.getName();
    }

    public static String[] list(final SwiftClientCfg swift, final String container, final String rFilename) {
        getSwiftCLIPath();
        final Script command = new Script("/bin/bash", logger);
        command.add("-c");

        final StringBuilder swiftCmdBuilder = buildSwiftCmd(swift);
        swiftCmdBuilder.append(" list ");
        swiftCmdBuilder.append(container);

        if (rFilename != null) {
            swiftCmdBuilder.append(" -p ");
            swiftCmdBuilder.append(rFilename);
        }

        command.add(swiftCmdBuilder.toString());
        final OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        final String result = command.execute(parser);
        if (result == null && parser.getLines() != null && !parser.getLines().equalsIgnoreCase("")) {
            final String[] lines = parser.getLines().split("\\n");
            return lines;
        } else {
            if (result != null) {
                final String errMsg = "swiftList failed , err=" + result;
                logger.debug("Failed to list " + errMsg);
            } else {
                final String errMsg = "swiftList failed, no lines returns";
                logger.debug("Failed to list " + errMsg);
            }
        }
        return new String[0];
    }

    private static StringBuilder buildSwiftCmd(final SwiftClientCfg swift) {
        final String swiftCli = getSwiftCLIPath();
        final StringBuilder sb = new StringBuilder();
        sb.append(" /usr/bin/python ");
        sb.append(swiftCli);
        sb.append(" -A ");
        sb.append(swift.getEndPoint());
        sb.append(" -U ");
        sb.append(swift.getAccount());
        sb.append(":");
        sb.append(swift.getUserName());
        sb.append(" -K ");
        sb.append(swift.getKey());
        sb.append(" ");
        return sb;
    }

    public static File getObject(final SwiftClientCfg cfg, final File destDirectory, final String swiftPath) {
        final int firstIndexOfSeparator = swiftPath.indexOf(File.separator);
        final String container = swiftPath.substring(0, firstIndexOfSeparator);
        final String srcPath = swiftPath.substring(firstIndexOfSeparator + 1);
        String destFilePath = null;
        if (destDirectory.isDirectory()) {
            destFilePath = destDirectory.getAbsolutePath() + File.separator + srcPath;
        } else {
            destFilePath = destDirectory.getAbsolutePath();
        }
        final String swiftCli = getSwiftCLIPath();
        final Script command = new Script("/bin/bash", logger);
        command.add("-c");
        command.add("/usr/bin/python " + swiftCli + " -A " + cfg.getEndPoint() + " -U " + cfg.getAccount() + ":" + cfg.getUserName() + " -K " + cfg.getKey() +
                " download " + container + " " + srcPath + " -o " + destFilePath);
        final OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        final String result = command.execute(parser);
        if (result != null) {
            final String errMsg = "swiftDownload failed  err=" + result;
            logger.debug(errMsg);
            throw new CloudRuntimeException("failed to get object: " + swiftPath);
        }
        if (parser.getLines() != null) {
            final String[] lines = parser.getLines().split("\\n");
            for (final String line : lines) {
                if (line.contains("Errno") || line.contains("failed")) {
                    final String errMsg = "swiftDownload failed , err=" + Arrays.toString(lines);
                    logger.debug(errMsg);
                    throw new CloudRuntimeException("Failed to get object: " + swiftPath);
                }
            }
        }
        return new File(destFilePath);
    }

    public static String getContainerName(final String type, final Long id) {
        if (type.startsWith("T")) {
            return "T-" + id;
        } else if (type.startsWith("S")) {
            return "S-" + id;
        } else if (type.startsWith("V")) {
            return "V-" + id;
        }
        return null;
    }

    public static boolean deleteObject(final SwiftClientCfg cfg, final String path) {
        final Script command = new Script("/bin/bash", logger);
        command.add("-c");

        final String[] paths = splitSwiftPath(path);
        if (paths == null) {
            return false;
        }
        final String container = paths[0];
        final String objectName = paths[1];

        final StringBuilder swiftCmdBuilder = buildSwiftCmd(cfg);
        swiftCmdBuilder.append(" delete ");
        swiftCmdBuilder.append(container);
        swiftCmdBuilder.append(" ");
        swiftCmdBuilder.append(objectName);

        command.add(swiftCmdBuilder.toString());
        final OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        command.execute(parser);
        return true;
    }

    public static String[] splitSwiftPath(final String path) {
        final int index = path.indexOf(File.separator);
        if (index == -1) {
            return null;
        }
        final String[] paths = new String[2];
        paths[0] = path.substring(0, index);
        paths[1] = path.substring(index + 1);
        return paths;
    }

    public interface SwiftClientCfg {
        String getAccount();

        String getUserName();

        String getKey();

        String getEndPoint();
    }
}
