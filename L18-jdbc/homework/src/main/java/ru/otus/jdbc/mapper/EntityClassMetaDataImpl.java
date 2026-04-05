package ru.otus.jdbc.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import ru.otus.crm.model.Id;

@SuppressWarnings("java:S3011")
public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {

    private final Class<T> entityClass;
    private final Constructor<T> constructor;
    private final List<Field> allFields;
    private final Field idField;
    private final List<Field> fieldsWithoutId;

    @SuppressWarnings("unchecked")
    public EntityClassMetaDataImpl(Class<T> entityClass) {

        this.entityClass = entityClass;

        this.allFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .toList();
        this.idField = allFields.stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Не нашел поле с аннотацией Id для класса: " + entityClass.getName()));
        this.fieldsWithoutId =
                allFields.stream().filter(field -> !field.equals(idField)).toList();
        this.constructor = Arrays.stream(entityClass.getDeclaredConstructors())
                .filter(ctor -> ctor.getParameterCount() == allFields.size())
                .map(ctor -> (Constructor<T>) ctor)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Не нашел конструктор для класса: " + entityClass.getName()));
        this.constructor.setAccessible(true);
    }

    @Override
    public String getName() {
        return entityClass.getSimpleName().toLowerCase();
    }

    @Override
    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    public Field getIdField() {
        return idField;
    }

    @Override
    public List<Field> getAllFields() {
        return allFields;
    }

    @Override
    public List<Field> getFieldsWithoutId() {
        return fieldsWithoutId;
    }
}
