package com.cloud.api.doc;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ApiXmlDocReader {
    public static void main(final String[] args) {
        String newFile = null;
        String oldFile = null;
        String dirName = "";

        final LinkedHashMap<String, Command> commands = new LinkedHashMap<>();
        final LinkedHashMap<String, Command> oldCommands = new LinkedHashMap<>();
        final ArrayList<Command> addedCommands = new ArrayList<>();
        final ArrayList<Command> removedCommands = new ArrayList<>();
        final HashMap<String, Command> stableCommands = new HashMap<>();

        final XStream xs = new XStream(new DomDriver());
        xs.alias("command", Command.class);
        xs.alias("arg", Argument.class);

        final List<String> argsList = Arrays.asList(args);
        final Iterator<String> iter = argsList.iterator();
        while (iter.hasNext()) {
            final String arg = iter.next();
            // populate the file names
            if (arg.equals("-new")) {
                newFile = iter.next();
            }
            if (arg.equals("-old")) {
                oldFile = iter.next();
            }
            if (arg.equals("-d")) {
                dirName = iter.next();
            }
        }

        try {
            try (ObjectInputStream inOld = xs.createObjectInputStream(new FileReader(oldFile))) {
                while (true) {
                    final Command c1 = (Command) inOld.readObject();
                    oldCommands.put(c1.getName(), c1);
                }
            } catch (final EOFException ex) {
                // EOF exception shows that there is no more objects in ObjectInputStream, so do nothing here
            }

            try (ObjectInputStream inNew = xs.createObjectInputStream(new FileReader(newFile))) {
                while (true) {
                    final Command c = (Command) inNew.readObject();
                    commands.put(c.getName(), c);
                }
            } catch (final EOFException ex) {
                // EOF exception shows that there is no more objects in ObjectInputStream, so do nothing here
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        // Check if any commands got added in new version
        for (final Map.Entry<String, Command> entry : commands.entrySet()) {
            if (!oldCommands.containsKey(entry.getKey())) {
                addedCommands.add(entry.getValue());
            } else {
                stableCommands.put(entry.getValue().getName(), entry.getValue());
            }
        }

        // Check if any commands were removed in new version
        for (final Map.Entry<String, Command> entry : oldCommands.entrySet()) {
            if (!commands.containsKey(entry.getKey())) {
                removedCommands.add(entry.getValue());
                if (stableCommands.get(entry.getKey()) != null) {
                    stableCommands.remove(entry.getKey());
                }
            }
        }

        try (FileWriter fstream = new FileWriter(dirName + "/diff.txt");
             BufferedWriter out = new BufferedWriter(fstream)) {
            // Print added commands
            out.write("Added commands:\n");
            for (final Command c : addedCommands) {
                if (c.getDescription() != null && !c.getDescription().isEmpty()) {
                    out.write("\n    " + c.getName() + " (" + c.getDescription() + ")\n");
                } else {
                    out.write("\n    " + c.getName() + "\n");
                }
            }

            // Print removed commands
            out.write("\nRemoved commands:\n");
            for (final Command c : removedCommands) {
                if (c.getDescription() != null && !c.getDescription().isEmpty()) {
                    out.write("\n\t" + c.getName() + " (" + c.getDescription() + ")\n");
                } else {
                    out.write("\n\t" + c.getName() + "\n");
                }
            }

            out.write("\nChanges in command type (sync versus async)\n");
            // Verify if the command was sync and became async and vice versa
            for (final Map.Entry<String, Command> entry : stableCommands.entrySet()) {
                if (commands.get(entry.getKey()).isAsync() != oldCommands.get(entry.getKey()).isAsync()) {
                    String type = "Sync";
                    if (commands.get(entry.getKey()).isAsync()) {
                        type = "Async";
                    }
                    out.write("\n\t" + entry.getValue().getName() + " became " + type);
                }
            }

            // Print differences between commands arguments
            out.write("\n\nChanges in commands arguments:\n");
            for (final String key : stableCommands.keySet()) {
                final ArrayList<Argument> newReqArgs = new ArrayList<>();
                final ArrayList<Argument> removedReqArgs = new ArrayList<>();
                final HashMap<String, Argument> stableReqArgs = new HashMap<>();
                final ArrayList<Argument> newRespArgs = new ArrayList<>();
                final ArrayList<Argument> removedRespArgs = new ArrayList<>();

                final Command newCommand = commands.get(key);
                final Command oldCommand = oldCommands.get(key);

                // Check if any request arguments were added in new version
                for (final Argument arg : newCommand.getRequest()) {
                    if (oldCommand.getReqArgByName(arg.getName()) == null) {
                        if (!(arg.getName().equals("page") || arg.getName().equals("pagesize") || arg.getName().equals("keyword"))) {
                            newReqArgs.add(arg);
                        }
                    } else {
                        stableReqArgs.put(arg.getName(), arg);
                    }
                }

                // Check if any request arguments were removed in new version
                for (final Argument arg : oldCommand.getRequest()) {
                    if (newCommand.getReqArgByName(arg.getName()) == null) {
                        removedReqArgs.add(arg);
                        if (stableReqArgs.get(arg.getName()) != null) {
                            stableReqArgs.remove(arg.getName());
                        }
                    }
                }

                // Compare stable request arguments of old and new version
                for (final Iterator<String> i = stableReqArgs.keySet().iterator(); i.hasNext(); ) {
                    final String argName = i.next();
                    if ((oldCommand.getReqArgByName(argName) != null) && (newCommand.getReqArgByName(argName) != null)) {
                        if (oldCommand.getReqArgByName(argName).isRequired().equals(newCommand.getReqArgByName(argName).isRequired())) {
                            i.remove();
                        }
                    }
                }

                // Check if any response arguments were added in new version
                if (newCommand.getResponse() != null && oldCommand.getResponse() != null) {
                    for (final Argument arg : newCommand.getResponse()) {
                        if (oldCommand.getResArgByName(arg.getName()) == null) {
                            newRespArgs.add(arg);
                        }
                    }

                    // Check if any response arguments were removed in new version
                    for (final Argument arg : oldCommand.getResponse()) {
                        if (newCommand.getResArgByName(arg.getName()) == null) {
                            removedRespArgs.add(arg);
                        }
                    }
                }

                if (newReqArgs.size() != 0 || newRespArgs.size() != 0 || removedReqArgs.size() != 0 || removedRespArgs.size() != 0 || stableReqArgs.size() != 0) {
                    final StringBuffer commandInfo = new StringBuffer();
                    commandInfo.append("\n\t" + key);
                    out.write(commandInfo.toString());
                    out.write("\n");

                    // Request
                    if (newReqArgs.size() != 0 || removedReqArgs.size() != 0 || stableReqArgs.size() != 0) {
                        final StringBuffer request = new StringBuffer();
                        request.append("\n\t\tRequest:\n");
                        out.write(request.toString());
                        if (newReqArgs.size() != 0) {
                            final StringBuffer newParameters = new StringBuffer();
                            newParameters.append("\n\t\t\tNew parameters: ");
                            for (final Argument newArg : newReqArgs) {
                                String isRequiredParam = "optional";
                                if (newArg.isRequired()) {
                                    isRequiredParam = "required";
                                }
                                newParameters.append(newArg.getName() + " (" + isRequiredParam + "), ");
                            }
                            newParameters.delete(newParameters.length() - 2, newParameters.length() - 1);
                            out.write(newParameters.toString());
                            out.write("\n");
                        }
                        if (removedReqArgs.size() != 0) {
                            final StringBuffer removedParameters = new StringBuffer();
                            removedParameters.append("\n\t\t\tRemoved parameters: ");
                            for (final Argument removedArg : removedReqArgs) {
                                removedParameters.append(removedArg.getName() + ", ");
                            }
                            removedParameters.delete(removedParameters.length() - 2, removedParameters.length() - 1);
                            out.write(removedParameters.toString());
                            out.write("\n");
                        }

                        if (stableReqArgs.size() != 0) {
                            final StringBuffer changedParameters = new StringBuffer();
                            changedParameters.append("\n\t\t\tChanged parameters: ");
                            for (final Argument stableArg : stableReqArgs.values()) {
                                String newRequired = "optional";
                                String oldRequired = "optional";
                                if ((oldCommand.getReqArgByName(stableArg.getName()) != null) && (oldCommand.getReqArgByName(stableArg.getName()).isRequired() == true)) {
                                    oldRequired = "required";
                                }
                                if ((newCommand.getReqArgByName(stableArg.getName()) != null) && (newCommand.getReqArgByName(stableArg.getName()).isRequired() == true)) {
                                    newRequired = "required";
                                }
                                changedParameters.append(stableArg.getName() + " (old version - " + oldRequired + ", new version - " + newRequired + "), ");
                            }
                            changedParameters.delete(changedParameters.length() - 2, changedParameters.length() - 1);
                            out.write(changedParameters.toString());
                            out.write("\n");
                        }
                    }

                    // Response
                    if (newRespArgs.size() != 0 || removedRespArgs.size() != 0) {
                        final StringBuffer changedResponseParams = new StringBuffer();
                        changedResponseParams.append("\n\t\tResponse:\n");
                        out.write(changedResponseParams.toString());
                        if (newRespArgs.size() != 0) {
                            final StringBuffer newRespParams = new StringBuffer();
                            newRespParams.append("\n\t\t\tNew parameters: ");
                            for (final Argument newArg : newRespArgs) {
                                newRespParams.append(newArg.getName() + ", ");
                            }
                            newRespParams.delete(newRespParams.length() - 2, newRespParams.length() - 1);
                            out.write(newRespParams.toString());
                            out.write("\n");
                        }
                        if (removedRespArgs.size() != 0) {
                            final StringBuffer removedRespParams = new StringBuffer();
                            removedRespParams.append("\n\t\t\tRemoved parameters: ");
                            for (final Argument removedArg : removedRespArgs) {
                                removedRespParams.append(removedArg.getName() + ", ");
                            }
                            removedRespParams.delete(removedRespParams.length() - 2, removedRespParams.length() - 1);
                            out.write(removedRespParams.toString());
                            out.write("\n");
                        }
                    }
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
