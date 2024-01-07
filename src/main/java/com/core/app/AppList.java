package com.core.app;

import com.core.connector.CommandSender;
import com.core.match.MatchCommandSender;
import com.core.match.fix.stp.FixServerTcpConnectorFactory;
import com.core.util.log.Log;
import com.core.util.log.LogManager;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * User: jgreco
 */
public class AppList {
    private final LogManager logManager;
    private final List<App> apps = new FastList<>();
    private final Map<String, App> appNameMap = new UnifiedMap<>();

    public AppList(LogManager logManager) {
        this.logManager = logManager;
    }

    public Application add(String instanceName, String contribName, String path, AppContext context, Map<String, String> params) throws Exception {
        Class<?> appClass = Class.forName(path);
        Constructor<?>[] constructors = appClass.getConstructors();
        Constructor<?> mainConstructor;
        Object[] parameters;
        MatchCommandSender sender = null;
        String errorMessage = null;

        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(AppConstructor.class) == null) {
                continue;
            }

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

            parameters = new Object[parameterTypes.length];
            Log log = logManager.get(instanceName);

            for (int i=0; i<parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Annotation[] annotations = parameterAnnotations[i];

                parameters[i] = context.getParameter(parameterType);
                if (parameters[i] == null) {
                    if (parameterType.equals(MatchCommandSender.class) || parameterType.equals(CommandSender.class)) {
                        if (sender != null) {
                            parameters[i] = sender;
                        }
                        else {
                            parameters[i] = sender = context.buildSender(contribName, log);
                        }
                    }
                    else if (parameterType.equals(Log.class)) {
                        parameters[i] = log;
                    }
                    else if (parameterType.equals(FixServerTcpConnectorFactory.class)) {
                        parameters[i] = new FixServerTcpConnectorFactory();
                    }
                    else {
                        if (annotations.length == 0 || !annotations[0].annotationType().equals(Param.class)) {
                            errorMessage = instanceName + " invalid app parameter: " + parameterType.getSimpleName();
                            break;
                        }

                        String paramName = ((Param) annotations[0]).name();

                        if (paramName.equalsIgnoreCase("name")) {
                            parameters[i] = contribName;
                        }
                        else if (paramName.equalsIgnoreCase("instance")) {
                            parameters[i] = instanceName;
                        }
                        else {
                            String paramString = params.get(paramName);
                            if (paramString == null) {
                                paramString = context.getParameter(paramName);
                            }
                            if (paramString == null) {
                                errorMessage = instanceName + " unknown parameter: " + paramName;
                                break;
                            }

                            // TODO: This could be rewritten to be more flexible
                            if (parameterType.equals(String.class)) {
                                parameters[i] = paramString.equals("null") ? null : paramString;
                            } else if (parameterType.equals(int.class)) {
                                parameters[i] = Integer.valueOf(paramString);
                            } else if (parameterType.equals(double.class)) {
                                parameters[i] = Double.valueOf(paramString);
                            } else if (parameterType.equals(short.class)) {
                                parameters[i] = Short.valueOf(paramString);
                            } else if (parameterType.equals(byte.class)) {
                                parameters[i] = Byte.valueOf(paramString);
                            } else if (parameterType.equals(char.class)) {
                                parameters[i] = Character.valueOf(paramString.charAt(0));
                            } else if (parameterType.equals(boolean.class)) {
                                parameters[i] = Boolean.valueOf(paramString);
                            }
                            else {
                                errorMessage = instanceName + " invalid param type (" + parameterType.getSimpleName() + ") for parameter: " + paramName;
                                break;
                            }
                        }
                    }
                }
            }

            if (errorMessage == null) {
            	// constructor must be non null here
                mainConstructor = constructor;
                try {
                    Application app = (Application)mainConstructor.newInstance(parameters);
                    addCommands(instanceName, app);
                    return app;
                }
                catch (InvocationTargetException e) {
                    log.error(log.log().add(e.getCause()));
                    throw new CommandException(e);
                }
                catch (Exception e) {
                    log.error(log.log().add(e));
                    throw new CommandException(e);
                }
            }
           	throw new CommandException("ERROR constructing application: " + errorMessage);
        }

        throw new CommandException("ERROR No constructor with AppConstructor attribute: " + instanceName + " (" + path + ")");
    }

    public void addCommands(String name, Object o) throws Exception {
        App app = new App(name, o);
        apps.add(app);
        appNameMap.put(name.toLowerCase(), app);

        Class<?> aClass = o.getClass();
        Method[] methods = aClass.getMethods();

        for (Method method : methods) {
            Exposed annotation1 = method.getAnnotation(Exposed.class);

            // TODO: Write something more flexible for the special commands
            if (annotation1 != null) {
                app.addCommand(annotation1.name(), new ExposedCommand(annotation1.name(), method));
            }
            else if (method.getName().equalsIgnoreCase("setActive")) {
                app.addCommand("setActive", new ExposedCommand("setActive", method));
            }
            else if (method.getName().equalsIgnoreCase("setPassive")) {
                app.addCommand("setPassive", new ExposedCommand("setPassive", method));
            }
            else if (method.getName().equalsIgnoreCase("status")) {
                app.addCommand("status", new ExposedCommand("status", method));
            }
            else if (method.getName().equalsIgnoreCase("setDebug")) {
                app.addCommand("setDebug", new ExposedCommand("setDebug", method));
            }
        }
    }

    public App get(String appName) {
        return appNameMap.get(appName.toLowerCase());
    }

    public App get(int i) {
        return apps.get(i);
    }

    public int size() {
        return apps.size();
    }
}
