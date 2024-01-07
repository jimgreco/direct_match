package com.core.app.heartbeats;

import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Created by jgreco on 5/8/15.
 */
public class HeartbeatVirtualMachineImpl implements HeartbeatVirtualMachine {
    private final byte[] VMName;
    private final String VMNameString;

    private byte nextAppID = 1;
    private final Map<String, HeartbeatApp> appMap = new UnifiedMap<>();
    private final List<HeartbeatApp> apps = new FastList<>();

    public HeartbeatVirtualMachineImpl(String vmName) {
        this.VMName = vmName.getBytes();
        this.VMNameString = vmName;
    }

    @Override
    public HeartbeatApp addApp(String appName, HeartbeatSource application) {
        HeartbeatApp app = appMap.get(appName);
        if (app == null) {
            app = new HeartbeatAppImpl(nextAppID++, appName, application);
            apps.add(app);
            appMap.put(appName, app);
        }
        return app;
    }

    @Override
    public void addApp(HeartbeatApp app) {
        if (appMap.containsKey(app.getName())) {
            apps.remove(app);
        }
        apps.add(app);
        appMap.put(app.getName(), app);
    }

    @Override
    public void writeHeader(ByteBuffer buffer, boolean hasData) {
        buffer.put((byte) (hasData ? 'D' : 'H'));
        buffer.putShort((short) VMName.length);
        buffer.put(VMName);
    }

    @Override
    public List<HeartbeatApp> getApps() {
        return apps;
    }

    @Override
    public HeartbeatApp getApp(byte appID) {
        if (appID <= 0 || appID > apps.size()) {
            return null;
        }

        return apps.get(appID - 1);
    }

    @Override
    public HeartbeatApp getApp(String appName) {
        return appMap.get(appName);
    }

    @Override
	public String getVMNameString(){
        return VMNameString;
    }

    @Override
	public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(VMNameString);
        builder.append('\n');

        for (int i=0; i<apps.size(); i++) {
            HeartbeatApp app = apps.get(i);
            builder.append('+');
            builder.append(app.getName());
            builder.append('\n');

            String lastCategory = "------";

            List<HeartbeatField> fields = app.getFields();
            for (int j=0; j<fields.size(); j++) {
                HeartbeatField field = fields.get(j);

                String category = field.getCategory();
                if (!category.equals(lastCategory)) {
                    builder.append(category).append('\n');
                    lastCategory = category;
                }

                builder.append("  ").append(field.getName()).append(": ");
                builder.append(field.getValue()).append('\n');
            }
        }

        return builder.toString();
    }
}
