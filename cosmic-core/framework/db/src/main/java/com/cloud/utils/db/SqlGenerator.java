// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.utils.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.TableGenerator;

import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.db.Attribute.Flag;

public class SqlGenerator {
    Class<?> _clazz;
    ArrayList<Attribute> _attributes;
    ArrayList<Field> _embeddeds;
    ArrayList<Class<?>> _tables;
    LinkedHashMap<String, List<Attribute>> _ids;
    HashMap<String, TableGenerator> _generators;
    ArrayList<Attribute> _ecAttrs;

    public SqlGenerator(final Class<?> clazz) {
        _clazz = clazz;
        _tables = new ArrayList<>();
        _attributes = new ArrayList<>();
        _ecAttrs = new ArrayList<>();
        _embeddeds = new ArrayList<>();
        _ids = new LinkedHashMap<>();
        _generators = new HashMap<>();

        buildAttributes(clazz, DbUtil.getTableName(clazz), DbUtil.getAttributeOverrides(clazz), false, false);
        assert (_tables.size() > 0) : "Did you forget to put @Entity on " + clazz.getName();
        handleDaoAttributes(clazz);
        findEcAttributes();
    }

    protected boolean checkMethods(final Class<?> clazz, final Map<String, Attribute> attrs) {
        final Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            final String name = method.getName();
            if (name.startsWith("get")) {
                final String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                assert !attrs.containsKey(fieldName) : "Mismatch in " + clazz.getSimpleName() + " for " + name;
            } else if (name.startsWith("is")) {
                final String fieldName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                assert !attrs.containsKey(fieldName) : "Mismatch in " + clazz.getSimpleName() + " for " + name;
            } else if (name.startsWith("set")) {
                final String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                assert !attrs.containsKey(fieldName) : "Mismatch in " + clazz.getSimpleName() + " for " + name;
            }
        }
        return true;

    }

    protected void buildAttributes(final Class<?> clazz, final String tableName, final AttributeOverride[] overrides, final boolean embedded, final boolean isId) {
        if (!embedded && clazz.getAnnotation(Entity.class) == null) {
            return;
        }

        final Class<?> parent = clazz.getSuperclass();
        if (parent != null) {
            buildAttributes(parent, DbUtil.getTableName(parent), DbUtil.getAttributeOverrides(parent), false, false);
        }

        if (!embedded) {
            _tables.add(clazz);
            _ids.put(tableName, new ArrayList<Attribute>());
        }

        final Field[] fields = clazz.getDeclaredFields();
        for (final Field field : fields) {
            field.setAccessible(true);

            final TableGenerator tg = field.getAnnotation(TableGenerator.class);
            if (tg != null) {
                _generators.put(field.getName(), tg);
            }

            if (!DbUtil.isPersistable(field)) {
                continue;
            }

            if (field.getAnnotation(Embedded.class) != null) {
                _embeddeds.add(field);
                final Class<?> embeddedClass = field.getType();
                assert (embeddedClass.getAnnotation(Embeddable.class) != null) : "Class is not annotated with Embeddable: " + embeddedClass.getName();
                buildAttributes(embeddedClass, tableName, DbUtil.getAttributeOverrides(field), true, false);
                continue;
            }

            if (field.getAnnotation(EmbeddedId.class) != null) {
                _embeddeds.add(field);
                final Class<?> embeddedClass = field.getType();
                assert (embeddedClass.getAnnotation(Embeddable.class) != null) : "Class is not annotated with Embeddable: " + embeddedClass.getName();
                buildAttributes(embeddedClass, tableName, DbUtil.getAttributeOverrides(field), true, true);
                continue;
            }

            final Attribute attr = new Attribute(clazz, overrides, field, tableName, embedded, isId);

            if (attr.getColumnName().equals(GenericDao.REMOVED_COLUMN)) {
                attr.setTrue(Attribute.Flag.DaoGenerated);
                attr.setFalse(Attribute.Flag.Insertable);
                attr.setFalse(Attribute.Flag.Updatable);
                attr.setTrue(Attribute.Flag.TimeStamp);
                attr.setFalse(Attribute.Flag.Time);
                attr.setFalse(Attribute.Flag.Date);
                attr.setTrue(Attribute.Flag.Nullable);
                attr.setTrue(Attribute.Flag.Removed);
            }

            if (attr.isId()) {
                final List<Attribute> attrs = _ids.get(tableName);
                attrs.add(attr);
            }

            _attributes.add(attr);
        }
    }

    protected void findEcAttributes() {
        for (final Attribute attr : _attributes) {
            if (attr.field == null) {
                continue;
            }
            final ElementCollection ec = attr.field.getAnnotation(ElementCollection.class);
            if (ec != null) {
                final Attribute idAttr = _ids.get(attr.table).get(0);
                assert supportsElementCollection(attr.field) : "Doesn't support ElementCollection for " + attr.field.getName();
                attr.attache = new EcInfo(attr, idAttr);
                _ecAttrs.add(attr);
            }
        }
    }

    protected boolean supportsElementCollection(final Field field) {
        final ElementCollection otm = field.getAnnotation(ElementCollection.class);
        if (otm.fetch() == FetchType.LAZY) {
            assert (false) : "Doesn't support laz fetch: " + field.getName();
            return false;
        }

        final CollectionTable ct = field.getAnnotation(CollectionTable.class);
        if (ct == null) {
            assert (false) : "No collection table sepcified for " + field.getName();
            return false;
        }

        return true;
    }

    public Map<String, TableGenerator> getTableGenerators() {
        return _generators;
    }

    protected void handleDaoAttributes(final Class<?> clazz) {
        Attribute attr;
        Class<?> current = clazz;
        while (current != null && current.getAnnotation(Entity.class) != null) {
            final DiscriminatorColumn column = current.getAnnotation(DiscriminatorColumn.class);
            if (column != null) {
                final String columnName = column.name();
                attr = findAttribute(columnName);
                if (attr != null) {
                    attr.setTrue(Attribute.Flag.DaoGenerated);
                    attr.setTrue(Attribute.Flag.Insertable);
                    attr.setTrue(Attribute.Flag.Updatable);
                    attr.setFalse(Attribute.Flag.Nullable);
                    attr.setTrue(Attribute.Flag.DC);
                } else {
                    attr = new Attribute(DbUtil.getTableName(current), column.name());
                    attr.setFalse(Flag.Selectable);
                    attr.setTrue(Flag.Insertable);
                    attr.setTrue(Flag.DaoGenerated);
                    attr.setTrue(Flag.DC);
                    _attributes.add(attr);
                }
                if (column.discriminatorType() == DiscriminatorType.CHAR) {
                    attr.setTrue(Attribute.Flag.CharDT);
                } else if (column.discriminatorType() == DiscriminatorType.STRING) {
                    attr.setTrue(Attribute.Flag.StringDT);
                } else if (column.discriminatorType() == DiscriminatorType.INTEGER) {
                    attr.setTrue(Attribute.Flag.IntegerDT);
                }
            }

            final PrimaryKeyJoinColumn[] pkjcs = DbUtil.getPrimaryKeyJoinColumns(current);
            if (pkjcs != null) {
                for (final PrimaryKeyJoinColumn pkjc : pkjcs) {
                    final String tableName = DbUtil.getTableName(current);
                    attr = findAttribute(pkjc.name());
                    if (attr == null || !tableName.equals(attr.table)) {
                        final Attribute id = new Attribute(DbUtil.getTableName(current), pkjc.name());
                        if (pkjc.referencedColumnName().length() > 0) {
                            attr = findAttribute(pkjc.referencedColumnName());
                            assert (attr != null) : "Couldn't find referenced column name " + pkjc.referencedColumnName();
                        }
                        id.field = attr.field;
                        id.setTrue(Flag.Id);
                        id.setTrue(Flag.Insertable);
                        id.setFalse(Flag.Updatable);
                        id.setFalse(Flag.Nullable);
                        id.setFalse(Flag.Selectable);
                        _attributes.add(id);
                        final List<Attribute> attrs = _ids.get(id.table);
                        attrs.add(id);
                    }
                }
            }
            current = current.getSuperclass();
        }

        attr = findAttribute(GenericDao.CREATED_COLUMN);
        if (attr != null && attr.field.getType() == Date.class) {
            attr.setTrue(Attribute.Flag.DaoGenerated);
            attr.setTrue(Attribute.Flag.Insertable);
            attr.setFalse(Attribute.Flag.Updatable);
            attr.setFalse(Attribute.Flag.Date);
            attr.setFalse(Attribute.Flag.Time);
            attr.setTrue(Attribute.Flag.TimeStamp);
            attr.setFalse(Attribute.Flag.Nullable);
            attr.setTrue(Attribute.Flag.Created);
        }

        attr = findAttribute(GenericDao.XID_COLUMN);
        if (attr != null && attr.field.getType() == String.class) {
            attr.setTrue(Attribute.Flag.DaoGenerated);
            attr.setTrue(Attribute.Flag.Insertable);
            attr.setFalse(Attribute.Flag.Updatable);
            attr.setFalse(Attribute.Flag.TimeStamp);
            attr.setFalse(Attribute.Flag.Time);
            attr.setFalse(Attribute.Flag.Date);
            attr.setFalse(Attribute.Flag.Nullable);
            attr.setFalse(Attribute.Flag.Removed);
        }
    }

    public List<Attribute> getElementCollectionAttributes() {
        return _ecAttrs;
    }

    public Attribute findAttribute(final String name) {
        for (final Attribute attr : _attributes) {

            if (attr.columnName.equalsIgnoreCase(name)) {
                if (attr.columnName.equalsIgnoreCase(GenericDao.REMOVED_COLUMN) && attr.isUpdatable()) {
                    return null;
                }
                return attr;
            }
        }

        return null;
    }

    public static StringBuilder buildUpdateSql(final String tableName, final List<Attribute> attrs) {
        final StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName).append(" SET ");
        for (final Attribute attr : attrs) {
            sql.append(attr.columnName).append(" = ?, ");
        }
        sql.delete(sql.length() - 2, sql.length());
        sql.append(" WHERE ");

        return sql;
    }

    public List<Pair<StringBuilder, Attribute[]>> buildUpdateSqls() {
        final ArrayList<Pair<StringBuilder, Attribute[]>> sqls = new ArrayList<>(_tables.size());
        for (final Class<?> table : _tables) {
            final ArrayList<Attribute> attrs = new ArrayList<>();
            final String tableName = DbUtil.getTableName(table);
            for (final Attribute attr : _attributes) {
                if (attr.isUpdatable() && tableName.equals(attr.table)) {
                    attrs.add(attr);
                }
            }
            if (attrs.size() != 0) {
                final Pair<StringBuilder, Attribute[]> pair =
                        new Pair<>(buildUpdateSql(tableName, attrs), attrs.toArray(new Attribute[attrs.size()]));
                sqls.add(pair);
            }
        }
        return sqls;
    }

    public static StringBuilder buildMysqlUpdateSql(final String joins, final Collection<Ternary<Attribute, Boolean, Object>> setters) {
        if (setters.size() == 0) {
            return null;
        }

        final StringBuilder sql = new StringBuilder("UPDATE ");

        sql.append(joins);

        sql.append(" SET ");

        for (final Ternary<Attribute, Boolean, Object> setter : setters) {
            final Attribute attr = setter.first();
            sql.append(attr.table).append(".").append(attr.columnName).append("=");
            if (setter.second() != null) {
                sql.append(attr.table).append(".").append(attr.columnName).append(setter.second() ? "+" : "-");
            }
            sql.append("?, ");
        }

        sql.delete(sql.length() - 2, sql.length());

        sql.append(" WHERE ");

        return sql;
    }

    public List<Pair<String, Attribute[]>> buildInsertSqls() {
        final LinkedHashMap<String, ArrayList<Attribute>> tableAttributeMap = new LinkedHashMap<>();
        for (final Class<?> table : _tables) {
            tableAttributeMap.put(DbUtil.getTableName(table), new ArrayList<Attribute>());
        }

        for (final Attribute attr : _attributes) {
            if (attr.isInsertable()) {
                final ArrayList<Attribute> attrs = tableAttributeMap.get(attr.table);
                assert (attrs != null) : "Null set of attributes for " + attr.table;
                attrs.add(attr);
            }
        }

        final List<Pair<String, Attribute[]>> sqls = new ArrayList<>(tableAttributeMap.size());
        for (final Map.Entry<String, ArrayList<Attribute>> entry : tableAttributeMap.entrySet()) {
            final ArrayList<Attribute> attrs = entry.getValue();
            final StringBuilder sql = buildInsertSql(entry.getKey(), attrs);
            final Pair<String, Attribute[]> pair = new Pair<>(sql.toString(), attrs.toArray(new Attribute[attrs.size()]));
            sqls.add(pair);
        }

        return sqls;
    }

    protected StringBuilder buildInsertSql(final String table, final ArrayList<Attribute> attrs) {
        final StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(table).append(" (");
        for (final Attribute attr : attrs) {
            sql.append(table).append(".").append(attr.columnName).append(", ");
        }
        if (attrs.size() > 0) {
            sql.delete(sql.length() - 2, sql.length());
        }

        sql.append(") VALUES (");
        for (int i = 0; i < attrs.size(); i++) {
            sql.append("?, ");
        }

        if (attrs.size() > 0) {
            sql.delete(sql.length() - 2, sql.length());
        }

        sql.append(")");

        return sql;
    }

    protected List<Pair<String, Attribute[]>> buildDeleteSqls() {
        final LinkedHashMap<String, ArrayList<Attribute>> map = new LinkedHashMap<>();
        for (final Class<?> table : _tables) {
            map.put(DbUtil.getTableName(table), new ArrayList<Attribute>());
        }

        for (final Attribute attr : _attributes) {
            if (attr.isId()) {
                final ArrayList<Attribute> attrs = map.get(attr.table);
                assert (attrs != null) : "Null set of attributes for " + attr.table;
                attrs.add(attr);
            }
        }

        final List<Pair<String, Attribute[]>> sqls = new ArrayList<>(map.size());
        for (final Map.Entry<String, ArrayList<Attribute>> entry : map.entrySet()) {
            final ArrayList<Attribute> attrs = entry.getValue();
            final String sql = buildDeleteSql(entry.getKey(), attrs);
            final Pair<String, Attribute[]> pair = new Pair<>(sql, attrs.toArray(new Attribute[attrs.size()]));
            sqls.add(pair);
        }

        Collections.reverse(sqls);
        return sqls;
    }

    protected String buildDeleteSql(final String table, final ArrayList<Attribute> attrs) {
        final StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(table).append(" WHERE ");
        for (final Attribute attr : attrs) {
            sql.append(table).append(".").append(attr.columnName).append("= ? AND ");
        }
        sql.delete(sql.length() - 5, sql.length());
        return sql.toString();
    }

    public Pair<String, Attribute[]> buildRemoveSql() {
        final Attribute attribute = findAttribute(GenericDao.REMOVED_COLUMN);
        if (attribute == null) {
            return null;
        }

        final StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(attribute.table).append(" SET ");
        sql.append(attribute.columnName).append(" = ? WHERE ");

        final List<Attribute> ids = _ids.get(attribute.table);

        // if ids == null, that means the removed column was added as a JOIN
        // value to another table.  We ignore it here.
        if (ids == null) {
            return null;
        }
        if (ids.size() == 0) {
            return null;
        }

        for (final Attribute id : ids) {
            sql.append(id.table).append(".").append(id.columnName).append(" = ? AND ");
        }

        sql.delete(sql.length() - 5, sql.length());

        final Attribute[] attrs = ids.toArray(new Attribute[ids.size() + 1]);
        attrs[attrs.length - 1] = attribute;

        return new Pair<>(sql.toString(), attrs);
    }

    public Map<String, Attribute[]> getIdAttributes() {
        final LinkedHashMap<String, Attribute[]> ids = new LinkedHashMap<>(_ids.size());

        for (final Map.Entry<String, List<Attribute>> entry : _ids.entrySet()) {
            ids.put(entry.getKey(), entry.getValue().toArray(new Attribute[entry.getValue().size()]));
        }

        return ids;
    }

    /**
     * @return a map of tables and maps of field names to attributes.
     */
    public Map<String, Attribute> getAllAttributes() {
        final Map<String, Attribute> attrs = new LinkedHashMap<>(_attributes.size());
        for (final Attribute attr : _attributes) {
            if (attr.field != null) {
                attrs.put(attr.field.getName(), attr);
            }
        }

        return attrs;
    }

    public Map<Pair<String, String>, Attribute> getAllColumns() {
        final Map<Pair<String, String>, Attribute> attrs = new LinkedHashMap<>(_attributes.size());
        for (final Attribute attr : _attributes) {
            if (attr.columnName != null) {
                attrs.put(new Pair<>(attr.table, attr.columnName), attr);
            }
        }

        return attrs;
    }

    protected static void addPrimaryKeyJoinColumns(final StringBuilder sql, final String fromTable, final String toTable, final String joinType, final PrimaryKeyJoinColumn[] pkjcs) {
        if ("right".equalsIgnoreCase(joinType)) {
            sql.append(" RIGHT JOIN ").append(toTable).append(" ON ");
        } else if ("left".equalsIgnoreCase(joinType)) {
            sql.append(" LEFT JOIN ").append(toTable).append(" ON ");
        } else {
            sql.append(" INNER JOIN ").append(toTable).append(" ON ");
        }
        for (final PrimaryKeyJoinColumn pkjc : pkjcs) {
            sql.append(fromTable).append(".").append(pkjc.name());
            final String refColumn = DbUtil.getReferenceColumn(pkjc);
            sql.append("=").append(toTable).append(".").append(refColumn).append(" ");
        }
    }

    public Pair<String, Attribute> getRemovedAttribute() {
        final Attribute removed = findAttribute(GenericDao.REMOVED_COLUMN);
        if (removed == null) {
            return null;
        }

        if (removed.field.getType() != Date.class) {
            return null;
        }

        final StringBuilder sql = new StringBuilder();
        sql.append(removed.table).append(".").append(removed.columnName).append(" IS NULL ");

        return new Pair<>(sql.toString(), removed);
    }

    protected static void buildJoins(final StringBuilder innerJoin, final Class<?> clazz) {
        final String tableName = DbUtil.getTableName(clazz);

        final SecondaryTable[] sts = DbUtil.getSecondaryTables(clazz);
        final ArrayList<String> secondaryTables = new ArrayList<>();
        for (final SecondaryTable st : sts) {
            final JoinType jt = clazz.getAnnotation(JoinType.class);
            String join = null;
            if (jt != null) {
                join = jt.type();
            }
            addPrimaryKeyJoinColumns(innerJoin, tableName, st.name(), join, st.pkJoinColumns());
            secondaryTables.add(st.name());
        }

        final Class<?> parent = clazz.getSuperclass();
        if (parent.getAnnotation(Entity.class) != null) {
            final String table = DbUtil.getTableName(parent);
            final PrimaryKeyJoinColumn[] pkjcs = DbUtil.getPrimaryKeyJoinColumns(clazz);
            assert (pkjcs != null) : "No Join columns specified for the super class";
            addPrimaryKeyJoinColumns(innerJoin, tableName, table, null, pkjcs);
        }
    }

    public String buildTableReferences() {
        final StringBuilder sql = new StringBuilder();
        sql.append(DbUtil.getTableName(_tables.get(_tables.size() - 1)));

        for (final Class<?> table : _tables) {
            buildJoins(sql, table);
        }

        return sql.toString();
    }

    public Pair<StringBuilder, Attribute[]> buildSelectSql(final boolean enableQueryCache) {
        final StringBuilder sql = new StringBuilder("SELECT ");

        sql.append(enableQueryCache ? "SQL_CACHE " : "");

        final ArrayList<Attribute> attrs = new ArrayList<>();

        for (final Attribute attr : _attributes) {
            if (attr.isSelectable()) {
                attrs.add(attr);
                sql.append(attr.table).append(".").append(attr.columnName).append(", ");
            }
        }

        if (attrs.size() > 0) {
            sql.delete(sql.length() - 2, sql.length());
        }

        sql.append(" FROM ").append(buildTableReferences());

        sql.append(" WHERE ");

        sql.append(buildDiscriminatorClause().first());

        return new Pair<>(sql, attrs.toArray(new Attribute[attrs.size()]));
    }

    public Pair<StringBuilder, Attribute[]> buildSelectSql(final Attribute[] attrs) {
        final StringBuilder sql = new StringBuilder("SELECT ");

        for (final Attribute attr : attrs) {
            sql.append(attr.table).append(".").append(attr.columnName).append(", ");
        }

        if (attrs.length > 0) {
            sql.delete(sql.length() - 2, sql.length());
        }

        sql.append(" FROM ").append(buildTableReferences());

        sql.append(" WHERE ");

        sql.append(buildDiscriminatorClause().first());

        return new Pair<>(sql, attrs);
    }

    /**
     * buildDiscriminatorClause builds the join clause when there are multiple tables.
     *
     * @return
     */
    public Pair<StringBuilder, Map<String, Object>> buildDiscriminatorClause() {
        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> values = new HashMap<>();

        for (final Class<?> table : _tables) {
            final DiscriminatorValue dv = table.getAnnotation(DiscriminatorValue.class);
            if (dv != null) {
                final Class<?> parent = table.getSuperclass();
                final String tableName = DbUtil.getTableName(parent);
                final DiscriminatorColumn dc = parent.getAnnotation(DiscriminatorColumn.class);
                assert (dc != null) : "Parent does not have discrminator column: " + parent.getName();
                sql.append(tableName);
                sql.append(".");
                sql.append(dc.name()).append("=");
                Object value = null;
                if (dc.discriminatorType() == DiscriminatorType.INTEGER) {
                    sql.append(dv.value());
                    value = Integer.parseInt(dv.value());
                } else if (dc.discriminatorType() == DiscriminatorType.CHAR) {
                    sql.append(dv.value());
                    value = dv.value().charAt(0);
                } else if (dc.discriminatorType() == DiscriminatorType.STRING) {
                    String v = dv.value();
                    v = v.substring(0, v.length() < dc.length() ? v.length() : dc.length());
                    sql.append("'").append(v).append("'");
                    value = v;
                }
                values.put(dc.name(), value);
                sql.append(" AND ");
            }
        }

        return new Pair<>(sql, values);
    }

    public Field[] getEmbeddedFields() {
        return _embeddeds.toArray(new Field[_embeddeds.size()]);
    }

    public String buildCountSql() {
        final StringBuilder sql = new StringBuilder();

        return sql.append("SELECT COUNT(*) FROM ").append(buildTableReferences()).append(" WHERE ").append(buildDiscriminatorClause().first()).toString();
    }

    public String buildDistinctIdSql() {
        final StringBuilder sql = new StringBuilder();

        return sql.append("SELECT DISTINCT id FROM ").append(buildTableReferences()).append(" WHERE ").append(buildDiscriminatorClause().first()).toString();
    }
}
