package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {

    private final EntityClassMetaData<?> entityClassMetaData;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityClassMetaData) {
        this.entityClassMetaData = entityClassMetaData;
    }

    @Override
    public String getSelectAllSql() {
        return "select " + getColumns() + " from " + entityClassMetaData.getName();
    }

    @Override
    public String getSelectByIdSql() {
        return getSelectAllSql() + " where " + entityClassMetaData.getIdField().getName() + " = ?";
    }

    @Override
    public String getInsertSql() {
        var columns = entityClassMetaData.getFieldsWithoutId().stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "));
        var values = entityClassMetaData.getFieldsWithoutId().stream()
                .map(field -> "?")
                .collect(Collectors.joining(", "));
        return "insert into " + entityClassMetaData.getName() + "(" + columns + ") values (" + values + ")";
    }

    @Override
    public String getUpdateSql() {
        var assignments = entityClassMetaData.getFieldsWithoutId().stream()
                .map(field -> field.getName() + " = ?")
                .collect(Collectors.joining(", "));
        var idFieldName = entityClassMetaData.getIdField().getName();
        return "update " + entityClassMetaData.getName() + " set " + assignments + " where " + idFieldName
                + " = ? returning " + idFieldName;
    }

    private String getColumns() {
        return entityClassMetaData.getAllFields()
                .stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "));
    }
}
