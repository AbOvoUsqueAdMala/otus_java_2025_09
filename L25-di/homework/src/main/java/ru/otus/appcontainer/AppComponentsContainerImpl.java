package ru.otus.appcontainer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;

@SuppressWarnings("squid:S1068")
public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    private void processConfig(Class<?> configClass) {
        checkConfigClass(configClass);

        var configInstance = instantiateConfig(configClass);
        var componentMethods = getComponentMethods(configClass);

        for (var componentMethod : componentMethods) {
            createAppComponent(configInstance, componentMethod);
        }
    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    private Object instantiateConfig(Class<?> configClass) {
        try {
            var constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot instantiate config: " + configClass.getName(), e);
        }
    }

    private List<Method> getComponentMethods(Class<?> configClass) {
        var componentMethods = Arrays.stream(configClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(AppComponent.class))
                .sorted(Comparator.comparingInt((Method method) ->
                                method.getAnnotation(AppComponent.class).order())
                        .thenComparing(Method::getName))
                .toList();

        validateComponentNames(componentMethods);
        return componentMethods;
    }

    private void validateComponentNames(List<Method> componentMethods) {
        Set<String> componentNames = new HashSet<>(appComponentsByName.keySet());

        for (var componentMethod : componentMethods) {
            var componentName =
                    componentMethod.getAnnotation(AppComponent.class).name();
            if (!componentNames.add(componentName)) {
                throw new IllegalStateException("Duplicate component name: " + componentName);
            }
        }
    }

    private void createAppComponent(Object configInstance, Method componentMethod) {
        try {
            componentMethod.setAccessible(true);
            var args = Arrays.stream(componentMethod.getParameterTypes())
                    .map(this::getAppComponent)
                    .toArray();

            var component = componentMethod.invoke(configInstance, args);
            var componentName =
                    componentMethod.getAnnotation(AppComponent.class).name();

            if (component == null) {
                throw new IllegalStateException("Component factory returned null: " + componentName);
            }
            if (appComponentsByName.containsKey(componentName)) {
                throw new IllegalStateException("Duplicate component name: " + componentName);
            }

            appComponents.add(component);
            appComponentsByName.put(componentName, component);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create component: " + componentMethod.getName(), e);
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        var components = appComponents.stream()
                .filter(componentClass::isInstance)
                .map(componentClass::cast)
                .toList();

        if (components.size() != 1) {
            throw new IllegalStateException("Expected single component of type " + componentClass.getName()
                    + ", but found " + components.size());
        }

        return components.getFirst();
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        var component = appComponentsByName.get(componentName);
        if (component == null) {
            throw new IllegalStateException("Component not found: " + componentName);
        }
        return (C) component;
    }
}
