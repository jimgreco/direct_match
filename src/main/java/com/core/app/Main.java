package com.core.app;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.core.nio.SelectorService;
import com.core.util.log.Log;
import com.core.util.log.LogManager;
import com.core.util.log.LogManagerFactory;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;
/**
 * User: jgreco
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("5 args required for Main [Common XML File] [XML File] [Telnet Port] [Web Port] [Script]");
            return;
        }

        String commonXmlFileName = args[0];
        String xmlFileName = args[1];
        String telnetPort = args[2];
        String webPort = args[3];

        if (args.length == 5) {
            final String script = args[4];
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (System.getProperty("os.name").contains("Windows")) {
                        	new ProcessBuilder("cmd", "/c", script).start();
                        }
                        else if (System.getProperty("os.name").contains("Mac OS")){
                            new ProcessBuilder("open", script).start();
                        }
                        else {
                            Runtime.getRuntime().exec(script);
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 5000);
        }

        String[] fileNamePieces = xmlFileName.split("/");
        String coreName = fileNamePieces[fileNamePieces.length - 1].split("\\.")[0];

        SystemTimeSource timeSource = new SystemTimeSource();

        Log localLog = new SystemOutLog(coreName, "SELECT", timeSource);
        localLog.info(localLog.log()
                .add("Main CommonXmlFile=").add(commonXmlFileName)
                .add(", XmlFile=").add(xmlFileName)
                .add(", TelnetPort=").add(telnetPort)
                .add(", WebPort=").add(webPort)
                .add("\n"));

        // TODO: We should make the time source, log type, and event service type configurable in the XML
        SelectorService selectorService = new SelectorService(localLog, timeSource);
        LogManager logManager = LogManagerFactory.get(timeSource, selectorService, "system", coreName, null);

        // Build up a list of apps
        AppBuilder appBuilder = new AppBuilder(
                coreName,
                commonXmlFileName,
                xmlFileName,
                selectorService,
                selectorService,
                selectorService,
                selectorService,
                timeSource,
                logManager);

        try {
            AppList appList = appBuilder.parseApps();

            TelnetServer telnetServer = new TelnetServer(selectorService, Integer.parseInt(telnetPort), appList, logManager);

            // TODO: We should make it optional to instantiate a web server.  For the sequencer we want to remove that ability
            WebServer webServer = new WebServer(selectorService, Integer.parseInt(webPort), appList,logManager);

            telnetServer.open();
            webServer.open();
        }
        catch(Exception e) {
            localLog.error(localLog.log().add("COULD NOT CONSTRUCT APPS").add(e));
            return;
        }

        selectorService.run();
    }
}
