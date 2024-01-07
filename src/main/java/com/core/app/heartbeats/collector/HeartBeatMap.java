package com.core.app.heartbeats.collector;

import com.core.app.heartbeats.HeartBeatFieldIDEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hli on 9/27/15.
 */
public class HeartBeatMap {

    private static final String APP_IDENTIFIER = HeartBeatFieldIDEnum.App.toString();
    private static final String CORE_IDENTIFIER = HeartBeatFieldIDEnum.CORE.toString();
    private static final String COMMA = ",";
    private static final String COLON = ":";

    private final Map<String,Map<String,Map<String,String>>> vmMap=new HashMap<>();
    private final Map<String,Login> loginStats=new HashMap<>();


    public void addHeartBeatData(String data){
        for(String appDetails: data.split("&")) {
            Map<String,String> result=stringToMap(appDetails);
            if(result.containsKey(HeartBeatFieldIDEnum.LoggedIn.name())){
                String appName=result.get(HeartBeatFieldIDEnum.App.name());
                if(loginStats.containsKey(appName)){
                    loginStats.get(appName).onUpdate(result);
                }else{
                    Login login=new Login(result);
                    loginStats.put(login.getApp(),login);
                }

            }
            if(!result.isEmpty()){
                String currAppName=result.get(APP_IDENTIFIER);
                String currCoreName=result.get(CORE_IDENTIFIER);
                if(vmMap.containsKey(currCoreName)){
                    vmMap.get(currCoreName).put(currAppName,result);
                }else{
                    Map<String,Map<String,String>> coreAppMap=new HashMap<>();
                    coreAppMap.put(currAppName, result);
                    vmMap.put(currCoreName,coreAppMap);
                }
            }
        }

    }

    public static Map<String, String> stringToMap(String query) {
        Map<String, String> result = new HashMap<>();

        for (String param : query.split(COMMA)) {
            String pair[] = param.split(COLON);
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public Map<String, String> getHeartbeatStatusFor(String vmName, String app) {
        if(vmMap.containsKey(vmName) && vmMap.get(vmName).containsKey(app)){
            return vmMap.get(vmName).get(app);
        }
        return null;
    }

    public ArrayList<Login>  getLogins() {
        ArrayList<Login> logins=new ArrayList<>();
        Iterator loginIter=loginStats.values().iterator();
        while(loginIter.hasNext()){
            logins.add((Login)loginIter.next());
        }
        return logins;
    }


    public ArrayList<Map<String,String>> getHeartbeatStatusArrayForAllVMs() {
        ArrayList<Map<String,String>> result=new ArrayList<>();
        Iterator coreIterator= vmMap.values().iterator();
        while(coreIterator.hasNext()){
            Map<String, Map<String, String>> mapEntry =(Map<String, Map<String, String>> ) coreIterator.next();
            Iterator  appIter=mapEntry.values().iterator();
            while (appIter.hasNext()){
                Map<String,String> keyval= (Map<String,String> )appIter.next();
                result.add(keyval);
            }

        }
        return result;
    }


}
