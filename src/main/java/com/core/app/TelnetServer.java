package com.core.app;

import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.log.LogManager;
import com.core.util.log.Logger;
import com.core.util.tcp.OneShotTCPServerSocket;
import com.core.util.tcp.OneShotTCPServerTCPServerSocketListener;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPSocketFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class TelnetServer implements OneShotTCPServerTCPServerSocketListener {
    private static final String END_LINE = "\r\n";
    private static final String INDENT = "  ";

    private final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private final OneShotTCPServerSocket socket;

    private final AppList appList;
    private final LogManager logs;
    private final Log log;

    public TelnetServer(TCPSocketFactory tcpSocketFactory, int port, AppList list, LogManager logManager) throws IOException {
        logs = logManager;
        appList = list;
        log = logs.get("TELNET");
        socket = new OneShotTCPServerSocket(tcpSocketFactory, port, this,log);

    }

    public void open() {
        socket.open();
    }

    public void close() {
        socket.close();
    }

    @Override
    public void onRead(TCPClientSocket clientSocket, ByteBuffer readBuffer) {
        writeBuffer.clear();

        readBuffer.mark();
        log.info(log.log().add("> ").add(readBuffer));
        readBuffer.reset();

        // TODO: Create a better interface for special commands
        if (BinaryUtils.compareMin(readBuffer, "q") || BinaryUtils.compareMin(readBuffer, "quit")) {
            // quit telnet
            closeConnection(clientSocket);
        }
        else if (BinaryUtils.compareMin(readBuffer, "ls") || BinaryUtils.compareMin(readBuffer, "dir")) {
            // print all the commands available
            printCommands(clientSocket);
        }
        else if (BinaryUtils.compareMin(readBuffer, "restart")) {
            // kill it!!
            sendReply(clientSocket, "!!!!RESTARTING!!!!", false);
            System.exit(0);
        }
        else if (BinaryUtils.compareMin(readBuffer, "debugOn")) {
            logs.setDebugForAll(true);
            sendReply(clientSocket, "Debug turned on for all apps", false);
        }
        else if (BinaryUtils.compareMin(readBuffer, "debugOff")) {
            logs.setDebugForAll(false);
            sendReply(clientSocket, "Debug turned off for all apps", false);
        }
        else if (BinaryUtils.compareMin(readBuffer, "gc")) {
            System.gc();
            System.gc();
            System.gc();
            sendReply(clientSocket, "3 x System.gc()", false);
        }
        else {
            // exposed command
            executeCommand(clientSocket, readBuffer);
        }
    }

    private void executeCommand(TCPClientSocket clientSocket, ByteBuffer readBuffer) {
        String commandString = BinaryUtils.toString(readBuffer);
        String[] appMethodSplit = commandString.split("\\.");

        // parse out app
        if (appMethodSplit.length < 2) {
            sendReply(clientSocket, "Poorly formatted command. Expecting AppName.MethodName(Param1,Param2)", true);
            return;
        }

        if (appMethodSplit.length > 2) {
            // We have some other dots
            appMethodSplit[1] = commandString.replace(appMethodSplit[0] + ".", "");
        }

        String appName = appMethodSplit[0].toLowerCase();
        App app = appList.get(appName);
        if (app == null) {
            sendReply(clientSocket, "Unknown app name: " + appName, true);
            return;
        }

        // parse out command
        String[] methodNameParamsSplit = appMethodSplit[1].split("\\(");
        if (methodNameParamsSplit.length != 2) {
            sendReply(clientSocket, "Poorly formatted command. Expecting AppName.MethodName(Param1,Param2)", true);
            return;
        }

        String commandName = methodNameParamsSplit[0];
        ExposedCommand command = app.getCommand(commandName);
        if (command == null) {
            sendReply(clientSocket, "Unknown command: " + appName + "." + commandName, true);
            return;
        }

        // parse out params
        String paramStr = methodNameParamsSplit[1];
        paramStr = paramStr.replace(")", "");
        String[] paramStrings = paramStr.split(",");

        if (command.getParameterNames().length > 0 && paramStrings.length != command.getParameterNames().length) {
            sendReply(clientSocket, "Invalid number of parameters. Expected " + Integer.toString(command.getParameterNames().length), true);
            return;
        }

        try {
            Object[] paramObjs = new Object[command.getParameterNames().length];

            for (int i = 0; i < paramObjs.length; i++) {
                Class<?> parameterType = command.getParameterTypes()[i];
                paramStrings[i] = paramStrings[i].trim();
                if (parameterType.equals(int.class)) {
                    paramObjs[i] = Integer.valueOf(paramStrings[i]);
                }
                else if (parameterType.equals(double.class)) {
                    paramObjs[i] = Double.valueOf(paramStrings[i]);
                }
                else if (parameterType.equals(String.class)) {
                    paramObjs[i] = paramStrings[i];
                }
                else if (parameterType.equals(boolean.class)) {
                    paramObjs[i] = Boolean.valueOf(paramStrings[i]);
                }
            }

            boolean error = false;
            Object result;
            try {
                result = command.getMethod().invoke(app.getObject(), paramObjs);
                if (result == null)
                    result = "OK";
                }
            catch(InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                result = "Error: " + targetException.getMessage();
                StackTraceElement[] stackTrace = targetException.getStackTrace();
                if (stackTrace != null) {
                    for (StackTraceElement stackTraceElement : stackTrace) {
                        result = result + END_LINE + stackTraceElement.toString();
                    }
                }
                error = true;
            }
            catch(Exception e) {
                result = "Error: " + e.getMessage();
                StackTraceElement[] stackTrace = e.getStackTrace();
                if (stackTrace != null) {
                    for (StackTraceElement stackTraceElement : stackTrace) {
                        result = result + END_LINE + stackTraceElement.toString();
                    }
                }
                error = true;
            }

            sendReply(clientSocket, result.toString(), error);
        } catch(NumberFormatException e) {
            sendReply(clientSocket, "Error parsing number: " + e.getMessage(), true);
        } catch (Exception e) {
            sendReply(clientSocket, "Error executing command: " + e.toString(), true);
        }
    }

    private void closeConnection(TCPClientSocket clientSocket) {
        sendReply(clientSocket, "Closing connection", false);
        clientSocket.close();
    }

    private void printCommands(TCPClientSocket clientSocket) {
        writeBuffer.clear();

        BinaryUtils.copy(writeBuffer, "Global: q, ls, restart, debugOn, debugOff, gc\n");

        if (appList.size() == 0) {
            BinaryUtils.copy(writeBuffer, "NO APPLICATIONS");
        }

        for (int i=0; i<appList.size(); i++) {
            App app = appList.get(i);
            BinaryUtils.copy(writeBuffer, app.getName());
            writeBuffer.put((byte)'[');
            BinaryUtils.copy(writeBuffer, app.getAppClass().getSimpleName());
            writeBuffer.put((byte)']');
            BinaryUtils.copy(writeBuffer, END_LINE);

            for (int j=0; j<app.getNCommands(); j++) {
                ExposedCommand command = app.getCommand(j);
                BinaryUtils.copy(writeBuffer, INDENT);
                BinaryUtils.copy(writeBuffer, command.getReturnTypeName());
                writeBuffer.put((byte) ' ');
                BinaryUtils.copy(writeBuffer, command.getMethodName());
                writeBuffer.put((byte) '(');

                for (int k=0; k<command.getParameterNames().length; k++) {
                    String parameterName = command.getParameterNames()[k];
                    Class<?> parameterType = command.getParameterTypes()[k];
                    if (k > 0) {
                        writeBuffer.put((byte)',');
                        writeBuffer.put((byte)' ');
                    }
                    BinaryUtils.copy(writeBuffer, parameterType.getSimpleName());
                    writeBuffer.put((byte)' ');
                    BinaryUtils.copy(writeBuffer, parameterName);
                }
                writeBuffer.put((byte) ')');
                BinaryUtils.copy(writeBuffer, END_LINE);
            }
        }

        writeBuffer.flip();
        clientSocket.write(writeBuffer);
    }

    private void sendReply(TCPClientSocket clientSocket, String str, boolean error) {
        writeBuffer.clear();
        BinaryUtils.copy(writeBuffer, str);
        BinaryUtils.copy(writeBuffer, END_LINE).flip();

        writeBuffer.mark();
        Logger logger = log.log().add("< ").add(writeBuffer);
        writeBuffer.reset();

        if (error) {
            log.error(logger);
        }
        else {
            log.info(logger);
        }
        clientSocket.write(writeBuffer);
    }
}
