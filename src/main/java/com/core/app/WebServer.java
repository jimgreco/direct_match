package com.core.app;

import com.core.util.BinaryUtils;
import com.core.util.log.LogManager;
import com.core.util.tcp.OneShotTCPServerSocket;
import com.core.util.tcp.OneShotTCPServerTCPServerSocketListener;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPSocketFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class WebServer implements OneShotTCPServerTCPServerSocketListener {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private final OneShotTCPServerSocket serverSocket;
    private final AppList appList;

    public WebServer(TCPSocketFactory tcpSocketFactory, int port, AppList list, LogManager logs) throws IOException {
        serverSocket = new OneShotTCPServerSocket(tcpSocketFactory, port, this, logs.get("WEBSERVER"));

        appList = list;
    }

    @Override
    public void onRead(TCPClientSocket clientSocket, ByteBuffer readBuffer) {
        try {
            String request = BinaryUtils.toString(readBuffer);

            writeBuffer.clear();
            BinaryUtils.copy(writeBuffer, "HTTP/1.1 200 OK\r\n");
            BinaryUtils.copy(writeBuffer, "Content-Type: application/javascript\r\n\r\n");

            boolean hasCallback = request.contains("callback");

            String[] items = request.split(" ");
            if (items.length < 3) {
                clientSocket.write(writeError("error", "Invalid HTTP request", ""));

                clientSocket.close();
                return;
            }

            String uri = items[1];
            String[] uriComponents = uri.split("/");

            if (uriComponents.length < 3) {
                clientSocket.write(writeError("error", "Invalid URI", uri));
                clientSocket.close();
                return;
            }

            String appName = uriComponents[1];
            String methodName = uriComponents[2];
            String[] methodAndParams = methodName.split("\\?");
            methodName = methodAndParams[0];




            App app = appList.get(appName);
            if (app == null) {
                clientSocket.write(writeError("error", "Unknown application", appName));
                clientSocket.close();
                return;
            }

            ExposedCommand command = app.getCommand(methodName);
            if (command == null) {
                clientSocket.write(writeError("error", "Unknown command", methodName));
                clientSocket.close();
                return;
            }




            String[] paramValues;
            if(methodAndParams.length == 2) {
                paramValues = methodAndParams[1].split("&");
            }
            else {
                paramValues = new String[0];
            }

            Object[] values = new Object[Math.max(0, paramValues.length - (hasCallback ? 1 : 0))];
            String callbackFn = "";

            int paramIndex = 0;
            for (int i=0; i<paramValues.length; i++) {
                String[] paramValue = paramValues[i].split("=");
                if (paramValue.length != 2) {
                    clientSocket.write(writeError(callbackFn, "Invalid parameter parsing", paramValues[i]));
                    clientSocket.close();
                    return;
                }

                String param = paramValue[0];
                String value = paramValue[1];

                if (param.equalsIgnoreCase("callback")) {
                    callbackFn = value;
                    continue;
                }

                values[paramIndex] = command.parseParam(param, value);

                if (values[paramIndex] == null) {
                    clientSocket.write(writeError(callbackFn, "Invalid parameter parsing", methodAndParams[1]));
                    clientSocket.close();
                    return;
                }

                paramIndex++;
            }



            Object result;
            try {
                result = command.getMethod().invoke(app.getObject(), values);
                if (result == null) {
                    result = "OK";
                }

                String resultJson = objectMapper.writeValueAsString(result);
                clientSocket.write(writeOK(callbackFn, resultJson));
            }
            catch(InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                clientSocket.write(writeError(callbackFn, "Target exception", targetException.getMessage()));
            }
            catch (JsonProcessingException e) {
                clientSocket.write(writeError(callbackFn, "Could not convert to JSON", e.getMessage()));
            }
            catch(Exception e) {
                clientSocket.write(writeError(callbackFn, "Other exception", e.getMessage()));
            }

            clientSocket.closeWhenFinishedWriting();
        }
        catch(Exception e) {
            e.printStackTrace();
            clientSocket.close();
        }
    }

    private ByteBuffer writeOK(String callbackFn, String payload) {
        boolean hasCallback = callbackFn.length() > 0;
        if (hasCallback) {
            BinaryUtils.copy(writeBuffer, callbackFn);
            writeBuffer.put((byte) '(');
        }

        BinaryUtils.copy(writeBuffer, "{\"status\":\"ok\",\"payload\":");
        BinaryUtils.copy(writeBuffer, payload);
        BinaryUtils.copy(writeBuffer, "}");

        if (hasCallback) {
            writeBuffer.put((byte) ')');
            writeBuffer.put((byte) ';');
        }

        writeBuffer.flip();
        return writeBuffer;
    }

    private ByteBuffer writeError(String callbackFn, String msg, String request) {
        boolean hasCallback = callbackFn.length() > 0;
        if (hasCallback) {
            BinaryUtils.copy(writeBuffer, callbackFn);
            writeBuffer.put((byte) '(');
        }

        BinaryUtils.copy(writeBuffer, "{\"status\":\"error\",\"payload\":\"");
        BinaryUtils.copy(writeBuffer, msg);
        BinaryUtils.copy(writeBuffer, ". ");
        BinaryUtils.copy(writeBuffer, request != null ? request : "null");
        BinaryUtils.copy(writeBuffer, "\"}");

        if (hasCallback) {
            writeBuffer.put((byte) ')');
            writeBuffer.put((byte) ';');
        }

        writeBuffer.flip();
        return writeBuffer;
    }

    public void open() {
        serverSocket.open();
    }

    public void close() {
        serverSocket.close();
    }
}
