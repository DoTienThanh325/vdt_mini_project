package com.vdt.documenttransfer.common.logging;

import java.lang.reflect.Method;
import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AppLogger {

    private static final Logger SERVICE_FILE_LOGGER = LoggerFactory.getLogger("SERVICE_FILE");

    @Around("execution(public * com.vdt.documenttransfer.modules..service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> serviceClass = joinPoint.getTarget().getClass();
        String serviceName = serviceClass.getSimpleName();
        String methodName = signature.getName();
        ServiceLogContext context = buildServiceLogContext(signature, joinPoint.getArgs(), serviceName);

        try {
            Object result = joinPoint.proceed();
            context = context.withObjectValue(resolveObjectValue(result, context.objectValue()));
            infoService(serviceClass, serviceName, methodName, elapsedMilliseconds(startTime), context);
            return result;
        } catch (Throwable throwable) {
            errorService(serviceClass, serviceName, methodName, elapsedMilliseconds(startTime), context, throwable);
            throw throwable;
        }
    }

    private void infoService(Class<?> serviceClass, String serviceName, String methodName, long durationMs,
            ServiceLogContext context) {
        try {
            putIfNotNull("service_logger", serviceClass.getName());
            putIfNotNull("action", "SERVICE_EXECUTION");
            putIfNotNull("service", serviceName);
            putIfNotNull("method", methodName);
            putIfNotNull("duration_ms", durationMs);
            putServiceContext(context);
            MDC.put("status", "SUCCESS");
            SERVICE_FILE_LOGGER.info("Service method completed successfully");
        } finally {
            MDC.clear();
        }
    }

    private void errorService(Class<?> serviceClass, String serviceName, String methodName, long durationMs,
            ServiceLogContext context, Throwable throwable) {
        try {
            putIfNotNull("service_logger", serviceClass.getName());
            putIfNotNull("action", "SERVICE_EXECUTION");
            putIfNotNull("service", serviceName);
            putIfNotNull("method", methodName);
            putIfNotNull("duration_ms", durationMs);
            putServiceContext(context);
            MDC.put("status", "FAILED");
            SERVICE_FILE_LOGGER.error("Service method failed: {}", throwable.getMessage(), throwable);
        } finally {
            MDC.clear();
        }
    }

    private long elapsedMilliseconds(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private ServiceLogContext buildServiceLogContext(MethodSignature signature, Object[] args, String serviceName) {
        String[] parameterNames = signature.getParameterNames();
        Object userId = null;
        Object objectValue = null;
        Object userIdFallback = null;

        for (int index = 0; index < args.length; index++) {
            Object argument = args[index];
            String parameterName = parameterNames != null ? parameterNames[index] : "";

            if (userId == null && isUserParameter(parameterName, argument)) {
                userId = readProperty(argument, "getId");
            }

            if (userId == null && "userId".equalsIgnoreCase(parameterName)) {
                userId = argument;
                userIdFallback = argument;
            }

            if (objectValue == null && isObjectIdentifier(parameterName)) {
                objectValue = argument;
            }

            if (objectValue == null) {
                objectValue = resolveObjectValue(argument, null);
            }
        }

        return new ServiceLogContext(userId, toObjectType(serviceName),
                objectValue != null ? objectValue : userIdFallback);
    }

    private boolean isUserParameter(String parameterName, Object argument) {
        if (argument == null) {
            return false;
        }

        return parameterName.equalsIgnoreCase("user")
                || parameterName.equalsIgnoreCase("clerk")
                || parameterName.equalsIgnoreCase("leader")
                || parameterName.equalsIgnoreCase("manager")
                || argument.getClass().getSimpleName().equals("User");
    }

    private boolean isObjectIdentifier(String parameterName) {
        String name = parameterName.toLowerCase();
        return !name.equals("userid")
                && (name.equals("id") || name.endsWith("id") || name.endsWith("code")
                        || name.equals("username") || name.equals("notificationid"));
    }

    private Object resolveObjectValue(Object result, Object fallback) {
        if (result == null) {
            return fallback;
        }

        for (String getter : new String[] {
                "getDocumentCode", "getSystemCode", "getOrgCode", "getNotificationId", "getId", "getUsername"
        }) {
            Object value = readProperty(result, getter);
            if (value != null) {
                return value;
            }
        }

        return fallback;
    }

    private Object readProperty(Object target, String getterName) {
        if (target == null) {
            return null;
        }

        try {
            Method getter = target.getClass().getMethod(getterName);
            return getter.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private String toObjectType(String serviceName) {
        String name = serviceName.replaceFirst("ServiceImpl$", "");
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase();
    }

    private void putServiceContext(ServiceLogContext context) {
        MDC.put("user_id", Objects.toString(context.userId(), "UNKNOWN"));
        MDC.put("object_type", Objects.toString(context.objectType(), "UNKNOWN"));
        MDC.put("object_value", Objects.toString(context.objectValue(), "UNKNOWN"));
    }

    private void putIfNotNull(String key, Object value) {
        if (value != null) {
            MDC.put(key, Objects.toString(value));
        }
    }

    private record ServiceLogContext(Object userId, String objectType, Object objectValue) {
        private ServiceLogContext withObjectValue(Object value) {
            return new ServiceLogContext(userId, objectType, value);
        }
    }
}
