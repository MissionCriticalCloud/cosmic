package com.cloud.utils.db;

import com.cloud.utils.Ternary;
import com.cloud.utils.exception.CloudRuntimeException;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class UpdateBuilder implements MethodInterceptor {
    protected Map<String, Ternary<Attribute, Boolean, Object>> _changes;
    protected HashMap<Attribute, Object> _collectionChanges;
    protected GenericDaoBase<?, ?> _dao;

    protected UpdateBuilder(final GenericDaoBase<?, ?> dao) {
        _dao = dao;
        _changes = new HashMap<>();
    }

    @Override
    public Object intercept(final Object object, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
        final String name = method.getName();
        if (name.startsWith("set")) {
            final String field = methodToField(name, 3);
            makeChange(field, args[0]);
        } else if (name.startsWith("incr")) {
            makeIncrChange(name, args);
        } else if (name.startsWith("decr")) {
            makeDecrChange(name, args);
        }
        return methodProxy.invokeSuper(object, args);
    }

    private String methodToField(final String method, final int start) {
        final char[] chs = method.toCharArray();
        chs[start] = Character.toLowerCase(chs[start]);
        return new String(chs, start, chs.length - start);
    }

    protected Attribute makeChange(final String field, final Object value) {
        final Attribute attr = _dao._allAttributes.get(field);

        assert (attr == null || attr.isUpdatable()) : "Updating an attribute that's not updatable: " + field;
        if (attr != null) {
            if (attr.attache == null) {
                _changes.put(field, new Ternary<>(attr, null, value));
            } else {
                if (_collectionChanges == null) {
                    _collectionChanges = new HashMap<>();
                }
                _collectionChanges.put(attr, value);
            }
        }
        return attr;
    }

    protected void makeIncrChange(final String method, final Object[] args) {
        final String field = methodToField(method, 4);
        final Attribute attr = _dao._allAttributes.get(field);
        assert (attr != null && attr.isUpdatable()) : "Updating an attribute that's not updatable: " + field;
        incr(attr, args == null || args.length == 0 ? 1 : args[0]);
    }

    protected void makeDecrChange(final String method, final Object[] args) {
        final String field = methodToField(method, 4);
        final Attribute attr = _dao._allAttributes.get(field);
        assert (attr != null && attr.isUpdatable()) : "Updating an attribute that's not updatable: " + field;
        decr(attr, args == null || args.length == 0 ? 1 : args[0]);
    }

    public void incr(final Attribute attr, final Object value) {
        _changes.put(attr.field.getName(), new Ternary<>(attr, true, value));
    }

    public void decr(final Attribute attr, final Object value) {
        _changes.put(attr.field.getName(), new Ternary<>(attr, false, value));
    }

    public void set(final Object entity, final String name, final Object value) {
        final Attribute attr = makeChange(name, value);

        set(entity, attr, value);
    }

    public void set(final Object entity, final Attribute attr, final Object value) {
        _changes.put(attr.field.getName(), new Ternary<>(attr, null, value));
        try {
            attr.field.set(entity, value);
        } catch (final IllegalArgumentException e) {
            throw new CloudRuntimeException("Unable to update " + attr.field.getName() + " with " + value, e);
        } catch (final IllegalAccessException e) {
            throw new CloudRuntimeException("Unable to update " + attr.field.getName() + " with " + value, e);
        }
    }

    public boolean hasChanges() {
        return (_changes.size() + (_collectionChanges != null ? _collectionChanges.size() : 0)) != 0;
    }

    public boolean has(final String name) {
        return _changes.containsKey(name);
    }

    public Map<Attribute, Object> getCollectionChanges() {
        return _collectionChanges;
    }

    public void clear() {
        _changes.clear();
        if (_collectionChanges != null) {
            _collectionChanges.clear();
            _collectionChanges = null;
        }
    }

    public StringBuilder toSql(final String tables) {
        if (_changes.isEmpty()) {
            return null;
        }

        return SqlGenerator.buildMysqlUpdateSql(tables, _changes.values());
    }

    public Collection<Ternary<Attribute, Boolean, Object>> getChanges() {
        return _changes.values();
    }
}
