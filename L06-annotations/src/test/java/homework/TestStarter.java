package homework;

import homework.annotation.After;
import homework.annotation.Before;
import homework.annotation.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStarter {

    private static final Logger log = LoggerFactory.getLogger(TestStarter.class);

    public static void main(String[] args) {
        runTests("homework.MainTest");
    }

    public static void runTests(String className) {
        try {

            Class<?> testClass = Class.forName(className);
            List<Method> beforeMethods = new ArrayList<>();
            List<Method> afterMethods = new ArrayList<>();
            List<Method> testMethods = new ArrayList<>();

            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Before.class)) {
                    beforeMethods.add(method);
                }
                if (method.isAnnotationPresent(After.class)) {
                    afterMethods.add(method);
                }
                if (method.isAnnotationPresent(Test.class)) {
                    testMethods.add(method);
                }
            }

            int total = testMethods.size();
            int passed = 0;

            for (Method test : testMethods) {
                Object instance = testClass.getDeclaredConstructor().newInstance();

                try {
                    var methodsPassed = invokeAll(instance, beforeMethods);
                    if (!methodsPassed) {
                        log.error("Error in before methods");
                        continue;
                    }
                    test.invoke(instance);
                    log.info("✅ {} passed", test.getName());
                    passed++;
                } catch (InvocationTargetException e) {
                    log.info(
                            "❌ {} failed: {}",
                            test.getName(),
                            e.getTargetException().getMessage());
                } catch (Exception e) {
                    log.info("⚠️ Unexpected error in {}: {}", test.getName(), e.getMessage());
                } finally {
                    invokeAll(instance, afterMethods);
                    log.info("--------");
                }
            }

            log.info(
                    """

                            ===== TEST SUMMARY =====
                            Total tests: {}
                            Passed: {}
                            Failed: {}
                            """,
                    total,
                    passed,
                    total - passed);

        } catch (Exception e) {
            log.error("Error running tests: {}", e.getMessage());
        }
    }

    private static boolean invokeAll(Object instance, List<Method> methods) {
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (Exception e) {
                log.info("⚠️ Error in {}: {}", method.getName(), e.getMessage());
                return false;
            }
        }
        return true;
    }
}
