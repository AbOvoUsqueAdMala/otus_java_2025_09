package ru.aoplogging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Ioc {
    private static final Logger logger = LoggerFactory.getLogger(Ioc.class);

    private Ioc() {}

    static TestLoggingInterface createClass(Class<?> clazz) {
        InvocationHandler handler = new DemoInvocationHandler(clazz);
        return (TestLoggingInterface) Proxy.newProxyInstance(
                Ioc.class.getClassLoader(), new Class<?>[] {TestLoggingInterface.class}, handler);
    }

    static class DemoInvocationHandler implements InvocationHandler {

        private final Map<Method, Method> methods = new HashMap<>();
        private final Object instance;

        DemoInvocationHandler(Class<?> clazz) {
            try {
                this.instance = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create instance of " + clazz, e);
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Method targetMethod = getClassMethodFromInterface(method);

            if (targetMethod.isAnnotationPresent(Log.class)) {
                logger.info("executed method: {}, param: {}", targetMethod.getName(), Arrays.toString(args));
            }
            return targetMethod.invoke(instance, args);
        }

        private Method getClassMethodFromInterface(Method method) throws NoSuchMethodException {

            var classMethod = methods.getOrDefault(method, null);
            if (classMethod != null) {
                return classMethod;
            }

            classMethod = instance.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            methods.put(method, classMethod);

            return classMethod;
        }

        @Override
        public String toString() {
            return "DemoInvocationHandler{" + "class for logging=" + instance + '}';
        }
    }
}
