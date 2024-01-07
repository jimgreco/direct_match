package com.core.app;

import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.List;
import java.util.Map;

/**
 * User: jgreco
 */
public class App {
    private final String name;
    private final Object obj;
    private final Class<?> cls;

    private final List<ExposedCommand> commands = new FastList<>();
    private final Map<String, ExposedCommand> commandNameMap = new UnifiedMap<>();

    public App(String name, Object obj) {
        this.name = name;
        this.cls = obj.getClass();
        this.obj = obj;
    }

    public void addCommand(String commandName, ExposedCommand command) {
        commands.add(command);
        commandNameMap.put(commandName.toLowerCase(), command);
    }

    public Object getObject() {
        return obj;
    }

    public Class<?> getAppClass() {
        return cls;
    }

    public String getName() {
        return name;
    }

    public int getNCommands() {
        return commands.size();
    }

    public ExposedCommand getCommand(int i) {
        return commands.get(i);
    }

    public ExposedCommand getCommand(String commandName) {
        return commandNameMap.get(commandName.toLowerCase());
    }
}
