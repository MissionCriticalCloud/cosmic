package com.cloud.utils.db;

import com.cloud.utils.Pair;
import com.cloud.utils.db.SearchBase.Condition;
import com.cloud.utils.db.SearchBase.Select;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * big joins or high performance searches, it is much better to
 */
public class SearchCriteria<K> {
    private final Map<String, Attribute> _attrs;
    private final ArrayList<Condition> _conditions;
    private final ArrayList<Select> _selects;
    private final GroupBy<? extends SearchBase<?, ?, K>, ?, K> _groupBy;
    private final List<Object> _groupByValues;
    private final Class<K> _resultType;
    private final SelectType _selectType;
    private ArrayList<Condition> _additionals = null;
    private HashMap<String, Object[]> _params = new HashMap<>();
    private int _counter;
    private HashMap<String, JoinBuilder<SearchCriteria<?>>> _joins;

    protected SearchCriteria(final SearchBase<?, ?, K> sb) {
        this._attrs = sb._attrs;
        this._conditions = sb._conditions;
        this._additionals = new ArrayList<>();
        this._counter = 0;
        this._joins = null;
        if (sb._joins != null) {
            _joins = new HashMap<>(sb._joins.size());
            for (final Map.Entry<String, JoinBuilder<SearchBase<?, ?, ?>>> entry : sb._joins.entrySet()) {
                final JoinBuilder<SearchBase<?, ?, ?>> value = entry.getValue();
                _joins.put(entry.getKey(),
                        new JoinBuilder<>(value.getT().create(), value.getFirstAttribute(), value.getSecondAttribute(), value.getType()));
            }
        }
        _selects = sb._selects;
        _groupBy = sb._groupBy;
        if (_groupBy != null) {
            _groupByValues = new ArrayList<>();
        } else {
            _groupByValues = null;
        }
        _resultType = sb._resultType;
        _selectType = sb._selectType;
    }

    protected void setParameters(final HashMap<String, Object[]> parameters) {
        _params = parameters;
    }

    public SelectType getSelectType() {
        return _selectType;
    }

    public void getSelect(final StringBuilder str, int insertAt) {
        if (_selects == null || _selects.size() == 0) {
            return;
        }

        for (final Select select : _selects) {
            String func = select.func.toString() + ",";
            if (select.attr == null) {
                func = func.replace("@", "*");
            } else {
                func = func.replace("@", select.attr.table + "." + select.attr.columnName);
            }
            str.insert(insertAt, func);
            insertAt += func.length();
            if (select.field == null) {
                break;
            }
        }

        str.delete(insertAt - 1, insertAt);
    }

    public List<Field> getSelectFields() {
        final List<Field> fields = new ArrayList<>(_selects.size());
        for (final Select select : _selects) {
            fields.add(select.field);
        }

        return fields;
    }

    public boolean isSelectAll() {
        return _selects == null || _selects.size() == 0;
    }

    protected JoinBuilder<SearchCriteria<?>> findJoin(final Map<String, JoinBuilder<SearchCriteria<?>>> jbmap, final String joinName) {
        JoinBuilder<SearchCriteria<?>> jb = jbmap.get(joinName);
        if (jb != null) {
            return jb;
        }

        for (final JoinBuilder<SearchCriteria<?>> j2 : jbmap.values()) {
            final SearchCriteria<?> sc = j2.getT();
            if (sc._joins != null) {
                jb = findJoin(sc._joins, joinName);
            }
            if (jb != null) {
                return jb;
            }
        }

        assert (false) : "Unable to find a join by the name " + joinName;
        return null;
    }

    public void setJoinParameters(final String joinName, final String conditionName, final Object... params) {
        final JoinBuilder<SearchCriteria<?>> join = findJoin(_joins, joinName);
        assert (join != null) : "Incorrect join name specified: " + joinName;
        join.getT().setParameters(conditionName, params);
    }

    public SearchCriteria<?> getJoin(final String joinName) {
        return _joins.get(joinName).getT();
    }

    public Pair<GroupBy<?, ?, ?>, List<Object>> getGroupBy() {
        return _groupBy == null ? null : new Pair<>(_groupBy, _groupByValues);
    }

    public void setGroupByValues(final Object... values) {
        for (final Object value : values) {
            _groupByValues.add(value);
        }
    }

    public Class<K> getResultType() {
        return _resultType;
    }

    @Deprecated
    public void addAnd(final String field, final Op op, final Object... values) {
        final String name = Integer.toString(_counter++);
        addCondition(name, " AND ", field, op);
        setParameters(name, values);
    }

    protected void addCondition(final String conditionName, final String cond, final String fieldName, final Op op) {
        final Attribute attr = _attrs.get(fieldName);
        assert attr != null : "Unable to find field: " + fieldName;
        addCondition(conditionName, cond, attr, op);
    }

    public void setParameters(final String conditionName, final Object... params) {
        assert _conditions.contains(new Condition(conditionName)) || _additionals.contains(new Condition(conditionName)) : "Couldn't find " + conditionName;
        _params.put(conditionName, params);
    }

    protected void addCondition(final String conditionName, final String cond, final Attribute attr, final Op op) {
        final Condition condition = new Condition(conditionName, /*(_conditions.size() + _additionals.size()) == 0 ? "" : */cond, attr, op);
        _additionals.add(condition);
    }

    @Deprecated
    public void addAnd(final Attribute attr, final Op op, final Object... values) {
        final String name = Integer.toString(_counter++);
        addCondition(name, " AND ", attr, op);
        setParameters(name, values);
    }

    @Deprecated
    public void addOr(final String field, final Op op, final Object... values) {
        final String name = Integer.toString(_counter++);
        addCondition(name, " OR ", field, op);
        setParameters(name, values);
    }

    public String getWhereClause() {
        final StringBuilder sql = new StringBuilder();
        int i = 0;
        for (final Condition condition : _conditions) {
            if (condition.isPreset()) {
                _params.put(condition.name, condition.presets);
            }
            final Object[] params = _params.get(condition.name);
            if ((condition.op == null || condition.op.params == 0) || (params != null)) {
                condition.toSql(sql, params, i++);
            }
        }

        for (final Condition condition : _additionals) {
            if (condition.isPreset()) {
                _params.put(condition.name, condition.presets);
            }
            final Object[] params = _params.get(condition.name);
            if ((condition.op.params == 0) || (params != null)) {
                condition.toSql(sql, params, i++);
            }
        }

        return sql.toString();
    }

    public List<Pair<Attribute, Object>> getValues() {
        final ArrayList<Pair<Attribute, Object>> params = new ArrayList<>(_params.size());
        for (final Condition condition : _conditions) {
            final Object[] objs = _params.get(condition.name);
            if (condition.op != null && condition.op.params != 0 && objs != null) {
                getParams(params, condition, objs);
            }
        }

        for (final Condition condition : _additionals) {
            final Object[] objs = _params.get(condition.name);
            if ((condition.op.params == 0) || (objs != null)) {
                getParams(params, condition, objs);
            }
        }

        return params;
    }

    public Collection<JoinBuilder<SearchCriteria<?>>> getJoins() {
        return _joins != null ? _joins.values() : null;
    }

    private void getParams(final ArrayList<Pair<Attribute, Object>> params, final Condition condition, final Object[] objs) {
        if (condition.op == Op.SC) {
            assert (objs != null && objs.length > 0) : " Where's your search criteria object? " + condition.name;
            params.addAll(((SearchCriteria<?>) objs[0]).getValues());
            return;
        }

        if (objs != null && objs.length > 0) {
            for (final Object obj : objs) {
                if ((condition.op != Op.EQ && condition.op != Op.NEQ) || (obj != null)) {
                    params.add(new Pair<>(condition.attr, obj));
                }
            }
        }
    }

    public Pair<String, ArrayList<Object>> toSql() {
        final StringBuilder sql = new StringBuilder();

        return new Pair<>(sql.toString(), null);
    }

    public enum Op {
        GT(" > ? ", 1), GTEQ(" >= ? ", 1), LT(" < ? ", 1), LTEQ(" <= ? ", 1), EQ(" = ? ", 1), NEQ(" != ? ", 1), BETWEEN(" BETWEEN ? AND ? ", 2), NBETWEEN(
                " NOT BETWEEN ? AND ? ",
                2), IN(" IN () ", -1), NOTIN(" NOT IN () ", -1), LIKE(" LIKE ? ", 1), NLIKE(" NOT LIKE ? ", 1), NIN(" NOT IN () ", -1), NULL(" IS NULL ", 0), NNULL(
                " IS NOT NULL ",
                0), SC(" () ", 1), TEXT("  () ", 1), RP("", 0), AND(" AND ", 0), OR(" OR ", 0), NOT(" NOT ", 0);

        private final String op;
        int params;

        Op(final String op, final int params) {
            this.op = op;
            this.params = params;
        }

        @Override
        public String toString() {
            return op;
        }

        public int getParams() {
            return params;
        }
    }

    public enum Func {
        NATIVE("@", 1), MAX("MAX(@)", 1), MIN("MIN(@)", 1), FIRST("FIRST(@)", 1), LAST("LAST(@)", 1), SUM("SUM(@)", 1), COUNT("COUNT(@)", 1), DISTINCT("DISTINCT(@)", 1);

        private final String func;
        private final int count;

        Func(final String func, final int params) {
            this.func = func;
            this.count = params;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return func;
        }

    }

    public enum SelectType {
        Fields, Entity, Single, Result
    }
}
