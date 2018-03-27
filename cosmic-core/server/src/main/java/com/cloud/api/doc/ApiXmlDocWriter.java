package com.cloud.api.doc;

import com.cloud.alert.AlertManager;
import com.cloud.api.APICommand;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.BaseResponse;
import com.cloud.api.Parameter;
import com.cloud.api.response.AsyncJobResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.SnapshotResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.serializer.Param;
import com.cloud.utils.IteratorUtil;
import com.cloud.utils.ReflectUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.annotations.SerializedName;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiXmlDocWriter {
    public static final Logger s_logger = LoggerFactory.getLogger(ApiXmlDocWriter.class.getName());

    private static final short DOMAIN_ADMIN_COMMAND = 4;
    private static final short USER_COMMAND = 8;
    private static final List<String> AsyncResponses = setAsyncResponses();
    private static final Map<String, Class<?>> s_apiNameCmdClassMap = new HashMap<>();
    private static final LinkedHashMap<Object, String> s_allApiCommands = new LinkedHashMap<>();
    private static final LinkedHashMap<Object, String> s_domainAdminApiCommands = new LinkedHashMap<>();
    private static final LinkedHashMap<Object, String> s_regularUserApiCommands = new LinkedHashMap<>();
    private static final TreeMap<Object, String> s_allApiCommandsSorted = new TreeMap<>();
    private static final TreeMap<Object, String> s_domainAdminApiCommandsSorted = new TreeMap<>();
    private static final TreeMap<Object, String> s_regularUserApiCommandsSorted = new TreeMap<>();
    private static String s_dirName = "";

    private static List<String> setAsyncResponses() {
        final List<String> asyncResponses = new ArrayList<>();
        asyncResponses.add(TemplateResponse.class.getName());
        asyncResponses.add(VolumeResponse.class.getName());
        asyncResponses.add(HostResponse.class.getName());
        asyncResponses.add(IPAddressResponse.class.getName());
        asyncResponses.add(StoragePoolResponse.class.getName());
        asyncResponses.add(UserVmResponse.class.getName());
        asyncResponses.add(SnapshotResponse.class.getName());

        return asyncResponses;
    }

    public static void main(final String[] args) {
        final Set<Class<?>> cmdClasses = ReflectUtil.getClassesWithAnnotation(APICommand.class, new String[]{"com.cloud.api", "com.cloud.api",
                "com.cloud.api.commands", "com.cloud.api.command.admin.zone", "com.cloud.network.contrail.api.command"});

        for (final Class<?> cmdClass : cmdClasses) {
            if (cmdClass.getAnnotation(APICommand.class) == null) {
                System.out.println("Warning, API Cmd class " + cmdClass.getName() + " has no APICommand annotation ");
                continue;
            }
            final String apiName = cmdClass.getAnnotation(APICommand.class).name();
            if (s_apiNameCmdClassMap.containsKey(apiName)) {
                // handle API cmd separation into admin cmd and user cmd with the common api name
                final Class<?> curCmd = s_apiNameCmdClassMap.get(apiName);
                if (curCmd.isAssignableFrom(cmdClass)) {
                    // api_cmd map always keep the admin cmd class to get full response and parameters
                    s_apiNameCmdClassMap.put(apiName, cmdClass);
                } else if (cmdClass.isAssignableFrom(curCmd)) {
                    // just skip this one without warning
                    continue;
                } else {
                    System.out.println("Warning, API Cmd class " + cmdClass.getName() + " has non-unique apiname " + apiName);
                    continue;
                }
            } else {
                s_apiNameCmdClassMap.put(apiName, cmdClass);
            }
        }

        final LinkedProperties preProcessedCommands = new LinkedProperties();
        String[] fileNames = null;

        final List<String> argsList = Arrays.asList(args);
        final Iterator<String> iter = argsList.iterator();
        while (iter.hasNext()) {
            final String arg = iter.next();
            // populate the file names
            if (arg.equals("-f")) {
                fileNames = iter.next().split(",");
            }
            if (arg.equals("-d")) {
                s_dirName = iter.next();
            }
        }

        if ((fileNames == null) || (fileNames.length == 0)) {
            System.out.println("Please specify input file(s) separated by coma using -f option");
            System.exit(2);
        }

        for (final String fileName : fileNames) {
            try (FileInputStream in = new FileInputStream(fileName)) {
                preProcessedCommands.load(in);
            } catch (final FileNotFoundException ex) {
                System.out.println("Can't find file " + fileName);
                System.exit(2);
            } catch (final IOException ex1) {
                System.out.println("Error reading from file " + ex1);
                System.exit(2);
            }
        }

        final Iterator<?> propertiesIterator = preProcessedCommands.keys.iterator();
        // Get command classes and response object classes
        while (propertiesIterator.hasNext()) {
            final String key = (String) propertiesIterator.next();
            final String preProcessedCommand = preProcessedCommands.getProperty(key);
            final int splitIndex = preProcessedCommand.lastIndexOf(";");
            final String commandRoleMask = preProcessedCommand.substring(splitIndex + 1);
            final Class<?> cmdClass = s_apiNameCmdClassMap.get(key);
            if (cmdClass == null) {
                System.out.println("Check, is this api part of another build profile? Null value for key: " + key + " preProcessedCommand=" + preProcessedCommand);
                continue;
            }
            final String commandName = cmdClass.getName();
            s_allApiCommands.put(key, commandName);

            short cmdPermissions = 1;
            if (commandRoleMask != null) {
                cmdPermissions = Short.parseShort(commandRoleMask);
            }

            if ((cmdPermissions & DOMAIN_ADMIN_COMMAND) != 0) {
                s_domainAdminApiCommands.put(key, commandName);
            }
            if ((cmdPermissions & USER_COMMAND) != 0) {
                s_regularUserApiCommands.put(key, commandName);
            }
        }

        s_allApiCommandsSorted.putAll(s_allApiCommands);
        s_domainAdminApiCommandsSorted.putAll(s_domainAdminApiCommands);
        s_regularUserApiCommandsSorted.putAll(s_regularUserApiCommands);

        try {
            // Create object writer
            final XStream xs = new XStream();
            xs.alias("command", Command.class);
            xs.alias("arg", Argument.class);
            final String xmlDocDir = s_dirName + "/xmldoc";
            final String rootAdminDirName = xmlDocDir + "/root_admin";
            final String domainAdminDirName = xmlDocDir + "/domain_admin";
            final String regularUserDirName = xmlDocDir + "/regular_user";
            (new File(rootAdminDirName)).mkdirs();
            (new File(domainAdminDirName)).mkdirs();
            (new File(regularUserDirName)).mkdirs();

            final ObjectOutputStream out = xs.createObjectOutputStream(new FileWriter(s_dirName + "/commands.xml"), "commands");
            final ObjectOutputStream rootAdmin = xs.createObjectOutputStream(new FileWriter(rootAdminDirName + "/" + "rootAdminSummary.xml"), "commands");
            final ObjectOutputStream rootAdminSorted = xs.createObjectOutputStream(new FileWriter(rootAdminDirName + "/" + "rootAdminSummarySorted.xml"), "commands");
            final ObjectOutputStream domainAdmin = xs.createObjectOutputStream(new FileWriter(domainAdminDirName + "/" + "domainAdminSummary.xml"), "commands");
            final ObjectOutputStream outDomainAdminSorted = xs.createObjectOutputStream(new FileWriter(domainAdminDirName + "/" + "domainAdminSummarySorted.xml"), "commands");
            final ObjectOutputStream regularUser = xs.createObjectOutputStream(new FileWriter(regularUserDirName + "/regularUserSummary.xml"), "commands");
            final ObjectOutputStream regularUserSorted = xs.createObjectOutputStream(new FileWriter(regularUserDirName + "/regularUserSummarySorted.xml"), "commands");

            // Write commands in the order they are represented in commands.properties.in file
            Iterator<?> it = s_allApiCommands.keySet().iterator();
            while (it.hasNext()) {
                final String key = (String) it.next();

                // Write admin commands
                writeCommand(out, key);
                writeCommand(rootAdmin, key);

                // Write single commands to separate xml files
                final ObjectOutputStream singleRootAdminCommandOs = xs.createObjectOutputStream(new FileWriter(rootAdminDirName + "/" + key + ".xml"), "command");
                writeCommand(singleRootAdminCommandOs, key);
                singleRootAdminCommandOs.close();

                if (s_domainAdminApiCommands.containsKey(key)) {
                    writeCommand(domainAdmin, key);
                    final ObjectOutputStream singleDomainAdminCommandOs = xs.createObjectOutputStream(new FileWriter(domainAdminDirName + "/" + key + ".xml"), "command");
                    writeCommand(singleDomainAdminCommandOs, key);
                    singleDomainAdminCommandOs.close();
                }

                if (s_regularUserApiCommands.containsKey(key)) {
                    writeCommand(regularUser, key);
                    final ObjectOutputStream singleRegularUserCommandOs = xs.createObjectOutputStream(new FileWriter(regularUserDirName + "/" + key + ".xml"), "command");
                    writeCommand(singleRegularUserCommandOs, key);
                    singleRegularUserCommandOs.close();
                }
            }

            // Write sorted commands
            it = s_allApiCommandsSorted.keySet().iterator();
            while (it.hasNext()) {
                final String key = (String) it.next();

                writeCommand(rootAdminSorted, key);

                if (s_domainAdminApiCommands.containsKey(key)) {
                    writeCommand(outDomainAdminSorted, key);
                }

                if (s_regularUserApiCommands.containsKey(key)) {
                    writeCommand(regularUserSorted, key);
                }
            }

            out.close();
            rootAdmin.close();
            rootAdminSorted.close();
            domainAdmin.close();
            outDomainAdminSorted.close();
            regularUser.close();
            regularUserSorted.close();

            // write alerttypes to xml
            writeAlertTypes(xmlDocDir);

            // gzip directory with xml doc
            // zipDir(dirName + "xmldoc.zip", xmlDocDir);

            // Delete directory
            // deleteDir(new File(xmlDocDir));

        } catch (final Exception ex) {
            ex.printStackTrace();
            System.exit(2);
        }
    }

    private static void writeCommand(final ObjectOutputStream out, final String command) throws ClassNotFoundException, IOException {
        final Class<?> clas = Class.forName(s_allApiCommands.get(command));
        final ArrayList<Argument> request;
        final ArrayList<Argument> response;

        // Create a new command, set name/description/usage
        final Command apiCommand = new Command();
        apiCommand.setName(command);

        APICommand impl = clas.getAnnotation(APICommand.class);
        if (impl == null) {
            impl = clas.getSuperclass().getAnnotation(APICommand.class);
        }

        if (impl == null) {
            throw new IllegalStateException(String.format("An %1$s annotation is required for class %2$s.", APICommand.class.getCanonicalName(), clas.getCanonicalName()));
        }

        if (impl.includeInApiDoc()) {
            final String commandDescription = impl.description();
            if (commandDescription != null && !commandDescription.isEmpty()) {
                apiCommand.setDescription(commandDescription);
            } else {
                System.out.println("Command " + apiCommand.getName() + " misses description");
            }

            final String commandUsage = impl.usage();
            if (commandUsage != null && !commandUsage.isEmpty()) {
                apiCommand.setUsage(commandUsage);
            }

            //Set version when the API is added
            if (!impl.since().isEmpty()) {
                apiCommand.setSinceVersion(impl.since());
            }

            final boolean isAsync = ReflectUtil.isCmdClassAsync(clas, new Class<?>[]{BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

            apiCommand.setAsync(isAsync);

            final Set<Field> fields = ReflectUtil.getAllFieldsForClass(clas, new Class<?>[]{BaseCmd.class, BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

            request = setRequestFields(fields);

            // Get response parameters
            final Class<?> responseClas = impl.responseObject();
            final Field[] responseFields = responseClas.getDeclaredFields();
            response = setResponseFields(responseFields, responseClas);

            apiCommand.setRequest(request);
            apiCommand.setResponse(response);

            out.writeObject(apiCommand);
        } else {
            s_logger.debug("Command " + command + " is not exposed in api doc");
        }
    }

    private static ArrayList<Argument> setRequestFields(final Set<Field> fields) {
        final ArrayList<Argument> arguments = new ArrayList<>();
        final Set<Argument> requiredArguments = new HashSet<>();
        final Set<Argument> optionalArguments = new HashSet<>();
        Argument id = null;
        for (final Field f : fields) {
            final Parameter parameterAnnotation = f.getAnnotation(Parameter.class);
            if (parameterAnnotation != null && parameterAnnotation.expose() && parameterAnnotation.includeInApiDoc()) {
                final Argument reqArg = new Argument(parameterAnnotation.name());
                reqArg.setRequired(parameterAnnotation.required());
                if (!parameterAnnotation.description().isEmpty()) {
                    reqArg.setDescription(parameterAnnotation.description());
                }

                if (parameterAnnotation.type() == BaseCmd.CommandType.LIST || parameterAnnotation.type() == BaseCmd.CommandType.MAP) {
                    reqArg.setType(parameterAnnotation.type().toString().toLowerCase());
                }

                reqArg.setDataType(parameterAnnotation.type().toString().toLowerCase());

                if (!parameterAnnotation.since().isEmpty()) {
                    reqArg.setSinceVersion(parameterAnnotation.since());
                }

                if (reqArg.isRequired()) {
                    if (parameterAnnotation.name().equals("id")) {
                        id = reqArg;
                    } else {
                        requiredArguments.add(reqArg);
                    }
                } else {
                    optionalArguments.add(reqArg);
                }
            }
        }

        // sort required and optional arguments here
        if (id != null) {
            arguments.add(id);
        }
        arguments.addAll(IteratorUtil.asSortedList(requiredArguments));
        arguments.addAll(IteratorUtil.asSortedList(optionalArguments));

        return arguments;
    }

    private static ArrayList<Argument> setResponseFields(final Field[] responseFields, final Class<?> responseClas) {
        final ArrayList<Argument> arguments = new ArrayList<>();
        final ArrayList<Argument> sortedChildlessArguments = new ArrayList<>();
        final ArrayList<Argument> sortedArguments = new ArrayList<>();

        Argument id = null;

        for (final Field responseField : responseFields) {
            final SerializedName nameAnnotation = responseField.getAnnotation(SerializedName.class);
            if (nameAnnotation != null) {
                final Param paramAnnotation = responseField.getAnnotation(Param.class);
                final Argument respArg = new Argument(nameAnnotation.value());

                boolean hasChildren = false;
                if (paramAnnotation != null && paramAnnotation.includeInApiDoc()) {
                    final String description = paramAnnotation.description();
                    final Class fieldClass = paramAnnotation.responseObject();
                    if (description != null && !description.isEmpty()) {
                        respArg.setDescription(description);
                    }

                    respArg.setDataType(responseField.getType().getSimpleName().toLowerCase());

                    if (!paramAnnotation.since().isEmpty()) {
                        respArg.setSinceVersion(paramAnnotation.since());
                    }

                    if (fieldClass != null) {
                        final Class<?> superClass = fieldClass.getSuperclass();
                        if (superClass != null) {
                            final String superName = superClass.getName();
                            if (superName.equals(BaseResponse.class.getName())) {
                                final ArrayList<Argument> fieldArguments;
                                final Field[] fields = fieldClass.getDeclaredFields();
                                fieldArguments = setResponseFields(fields, fieldClass);
                                respArg.setArguments(fieldArguments);
                                hasChildren = true;
                            }
                        }
                    }
                }

                if (paramAnnotation != null && paramAnnotation.includeInApiDoc()) {
                    if (nameAnnotation.value().equals("id")) {
                        id = respArg;
                    } else {
                        if (hasChildren) {
                            respArg.setName(nameAnnotation.value() + "(*)");
                            sortedArguments.add(respArg);
                        } else {
                            sortedChildlessArguments.add(respArg);
                        }
                    }
                }
            }
        }

        Collections.sort(sortedArguments);
        Collections.sort(sortedChildlessArguments);

        if (id != null) {
            arguments.add(id);
        }
        arguments.addAll(sortedChildlessArguments);
        arguments.addAll(sortedArguments);

        if (responseClas.getName().equalsIgnoreCase(AsyncJobResponse.class.getName())) {
            final Argument jobIdArg = new Argument("jobid", "the ID of the async job");
            arguments.add(jobIdArg);
        } else if (AsyncResponses.contains(responseClas.getName())) {
            final Argument jobIdArg = new Argument("jobid", "the ID of the latest async job acting on this object");
            final Argument jobStatusArg = new Argument("jobstatus", "the current status of the latest async job acting on this object");
            arguments.add(jobIdArg);
            arguments.add(jobStatusArg);
        }

        return arguments;
    }

    private static void zipDir(final String zipFileName, final String dir) throws Exception {
        final File dirObj = new File(dir);
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        addDir(dirObj, out);
        out.close();
    }

    static void addDir(final File dirObj, final ZipOutputStream out) throws IOException {
        final File[] files = dirObj.listFiles();
        final byte[] tmpBuf = new byte[1024];
        final String pathToDir = s_dirName;

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], out);
                continue;
            }
            try (FileInputStream in = new FileInputStream(files[i].getPath())) {
                out.putNextEntry(new ZipEntry(files[i].getPath().substring(pathToDir.length())));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    out.write(tmpBuf, 0, len);
                }
                out.closeEntry();
            } catch (final IOException ex) {
                s_logger.error("addDir:Exception:" + ex.getMessage(), ex);
            }
        }
    }

    private static void deleteDir(final File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteDir(new File(dir, children[i]));
                }
            }
        }
        dir.delete();
    }

    private static void writeAlertTypes(final String dirName) {
        final XStream xs = new XStream();
        xs.alias("alert", Alert.class);
        try (ObjectOutputStream out = xs.createObjectOutputStream(new FileWriter(dirName + "/alert_types.xml"), "alerts")) {
            for (final Field f : AlertManager.class.getFields()) {
                if (f.getClass().isAssignableFrom(Number.class)) {
                    final String name = f.getName().substring(11);
                    final Alert alert = new Alert(name, f.getInt(null));
                    out.writeObject(alert);
                }
            }
        } catch (final IOException e) {
            s_logger.error("Failed to create output stream to write an alert types ", e);
        } catch (final IllegalAccessException e) {
            s_logger.error("Failed to read alert fields ", e);
        }
    }

    private static class LinkedProperties extends Properties {
        private final LinkedList<Object> keys = new LinkedList<>();

        @Override
        public Enumeration<Object> keys() {
            return Collections.<Object>enumeration(keys);
        }

        @Override
        public Object put(final Object key, final Object value) {
            // System.out.println("Adding key" + key);
            keys.add(key);
            return super.put(key, value);
        }
    }
}
