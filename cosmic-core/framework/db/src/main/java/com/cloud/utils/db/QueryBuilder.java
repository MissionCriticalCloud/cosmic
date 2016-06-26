package com.cloud.utils.db;

public class QueryBuilder<T> extends GenericQueryBuilder<T, T> {

    protected QueryBuilder(final Class<T> entityType) {
        super(entityType, entityType);
    }

    public static <T> QueryBuilder<T> create(final Class<T> entityType) {
        return new QueryBuilder<>(entityType);
    }
}
