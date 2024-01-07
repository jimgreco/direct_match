package com.core.app;

import com.core.util.ByteStringBuffer;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * User: jgreco
 */
public class ExposedCommand {
    private final Method method;
    private final String methodName;
    private final String returnTypeName;
    private final Class<?>[] parameterTypes;
    private final String[] parameterNames;
    private final ObjectIntHashMap<String> paramIndexMap = new ObjectIntHashMap<>();
    private final ByteStringBuffer buffer = new ByteStringBuffer();

    public ExposedCommand(String name, Method method) throws Exception {
        this.method = method;
        this.methodName = name;
        this.returnTypeName = method.getReturnType().getSimpleName();
        this.parameterTypes = method.getParameterTypes();
        this.parameterNames = new String[parameterTypes.length];

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] types = method.getParameterTypes();

        for (int j=0; j<types.length; j++) {
            Class<?> parameterType = types[j];
            Annotation[] parameterAnnotation = parameterAnnotations[j];
            String paramName = null;

            for (int k = 0; k < parameterAnnotation.length; k++) {
                Annotation annotation = parameterAnnotation[k];
                if (annotation.annotationType().equals(Param.class)) {
                    paramName = ((Param) annotation).name();
                }
            }

            if (paramName == null) {
                throw new Exception("Missing Param annotation for " + method.getName());
            }

            if (!isValidType(parameterType)) {
                throw new Exception("Invalid param type for " + method.getName() + ": " + parameterType);
            }

            paramIndexMap.put(paramName, j);

            parameterNames[j] = paramName;
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public String[] getParameterNames() {
        return parameterNames;
    }

    private static boolean isValidType(Class<?> type) {
        return type.equals(String.class) ||
                type.equals(int.class) ||
                type.equals(double.class) ||
                type.equals(void.class) ||
                type.equals(boolean.class) ||
                type.equals(ByteStringBuffer.class);
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    public Object parseParam(String paramName, String paramValue) {
        int index = paramIndexMap.getIfAbsent(paramName, -1);
        if (index == -1) {
            return null;
        }

        Class<?> type = parameterTypes[index];
        if (type.equals(String.class)) {
            return paramValue;
        }
        else if (type.equals(int.class)) {
            return Integer.valueOf(paramValue);
        }
        else if (type.equals(double.class)) {
            return Double.valueOf(paramValue);
        }
        else if (type.equals(boolean.class)) {
            return Boolean.valueOf(paramValue);
        }
        else if (type.equals(ByteStringBuffer.class)) {
            return buffer.clear().add(paramValue);
        }

        return null;
    }
}
