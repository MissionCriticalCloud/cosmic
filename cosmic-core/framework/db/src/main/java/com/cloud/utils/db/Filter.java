package com.cloud.utils.db;

import com.cloud.utils.Pair;
import com.cloud.utils.ReflectUtil;

import javax.persistence.Column;
import java.lang.reflect.Field;

/**
 * Try to use static initialization to help you in finding incorrect
 * field names being passed in early.
 * <p>
 * Something like the following:
 * protected static final Filter s_NameFilter = new Filter(VMInstanceVO, name, true, null, null);
 * <p>
 * Filter nameFilter = new Filter(s_nameFilter);
 */
public class Filter {
    Long _offset;
    Long _limit;
    String _orderBy;

    /**
     * @param clazz  the VO object type
     * @param field  name of the field
     * @param offset
     * @param limit
     */
    public Filter(final Class<?> clazz, final String field, final boolean ascending, final Long offset, final Long limit) {
        _offset = offset;
        _limit = limit;

        addOrderBy(clazz, field, ascending);
    }

    public void addOrderBy(Class<?> clazz, final String field, final boolean ascending) {
        if (field == null) {
            return;
        }
        final Field f;
        final Pair<Class<?>, Field> pair = ReflectUtil.getAnyField(clazz, field);
        assert (pair != null) : "Can't find field " + field + " in " + clazz.getName();
        clazz = pair.first();
        f = pair.second();

        final Column column = f.getAnnotation(Column.class);
        final String name = column != null ? column.name() : field;

        final StringBuilder order = new StringBuilder();
        if (column == null || column.table() == null || column.table().length() == 0) {
            order.append(DbUtil.getTableName(clazz));
        } else {
            order.append(column.table());
        }
        order.append(".").append(name).append(ascending ? " ASC " : " DESC ");

        if (_orderBy == null) {
            _orderBy = order.insert(0, " ORDER BY ").toString();
        } else {
            _orderBy = order.insert(0, _orderBy + ", ").toString();
        }
    }

    public Filter(final long limit) {
        _orderBy = " ORDER BY RAND() LIMIT " + limit;
    }

    /**
     * Note that this copy constructor does not copy offset and limit.
     *
     * @param that filter
     */
    public Filter(final Filter that) {
        this._orderBy = that._orderBy;
        this._limit = null;
        that._limit = null;
    }

    public String getOrderBy() {
        return _orderBy;
    }

    public Long getOffset() {
        return _offset;
    }

    public void setOffset(final Long offset) {
        _offset = offset;
    }

    public Long getLimit() {
        return _limit;
    }

    public void setLimit(final Long limit) {
        _limit = limit;
    }
}
