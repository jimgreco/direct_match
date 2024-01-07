package com.core.app.heartbeats.collector;

import com.core.app.heartbeats.HeartbeatApp;
import com.core.app.heartbeats.HeartbeatField;
import com.core.app.heartbeats.HeartbeatVirtualMachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jgreco on 6/3/15.
 */
public class HeartbeatTable {
    private final String name;
    private final List<Map<String, Object>> data = new ArrayList<>();

    public HeartbeatTable(Map<String, HeartbeatVirtualMachine> vms) {
        this("CORE");

        for (Map.Entry<String, HeartbeatVirtualMachine> vm : vms.entrySet()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("VM", vm.getKey());

            String appNames = "";
            List<HeartbeatApp> apps = vm.getValue().getApps();
            boolean first = true;
            for (HeartbeatApp app : apps) {
                if (!first) {
                    appNames += ",";
                }
                first = false;
                appNames += app.getName();

                if (app.getName().contains("CON")) {
                    List<HeartbeatField> fields = app.getFields();
                    for (HeartbeatField field : fields) {
                        map.put(field.getName(), field.getValue());
                    }
                }
            }

            map.put("Apps", appNames);

            data.add(map);
        }
    }

    public HeartbeatTable(HeartbeatApp app) {
        this(app.getName());

        for (HeartbeatField field : app.getFields()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("App", app.getName());
            map.put("Category", field.getCategory());
            map.put("Name", field.getName());
            map.put("Value", field.getValue());
            data.add(map);
        }
    }

    private HeartbeatTable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }
}
