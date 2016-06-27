package org.apache.cloudstack.framework.config;

import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.framework.config.impl.ConfigDepotImpl;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;

import java.sql.Date;

/**
 * ConfigKey supplants the original Config.java.  It is just a class
 * declaration where others can declare their config variables.
 */
public class ConfigKey<T> {

    static ConfigDepotImpl s_depot = null;
    private final String _category;
    private final Class<T> _type;
    private final String _name;
    private final String _defaultValue;
    private final String _description;
    private final Scope _scope; // Parameter can be at different levels (Zone/cluster/pool/account), by default every parameter is at global
    private final boolean _isDynamic;
    private final T _multiplier;
    T _value = null;

    public ConfigKey(final String category, final Class<T> type, final String name, final String defaultValue, final String description, final boolean isDynamic, final Scope
            scope) {
        this(type, name, category, defaultValue, description, isDynamic, scope, null);
    }

    public ConfigKey(final Class<T> type, final String name, final String category, final String defaultValue, final String description, final boolean isDynamic, final Scope
            scope, final T multiplier) {
        _category = category;
        _type = type;
        _name = name;
        _defaultValue = defaultValue;
        _description = description;
        _scope = scope;
        _isDynamic = isDynamic;
        _multiplier = multiplier;
    }

    public ConfigKey(final String category, final Class<T> type, final String name, final String defaultValue, final String description, final boolean isDynamic) {
        this(type, name, category, defaultValue, description, isDynamic, Scope.Global, null);
    }

    @Deprecated
    public ConfigKey(final Class<T> type, final String name, final String category, final String defaultValue, final String description, final boolean isDynamic) {
        this(type, name, category, defaultValue, description, isDynamic, Scope.Global, null);
    }

    static public void init(final ConfigDepotImpl depot) {
        s_depot = depot;
    }

    public String category() {
        return _category;
    }

    public Class<T> type() {
        return _type;
    }

    public final String key() {
        return _name;
    }

    public String defaultValue() {
        return _defaultValue;
    }

    public String description() {
        return _description;
    }

    public Scope scope() {
        return _scope;
    }

    public boolean isDynamic() {
        return _isDynamic;
    }

    public T multiplier() {
        return _multiplier;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ConfigKey) {
            final ConfigKey<?> that = (ConfigKey<?>) obj;
            return this._name.equals(that._name);
        }
        return false;
    }

    @Override
    public String toString() {
        return _name;
    }

    public boolean isSameKeyAs(final Object obj) {
        if (this.equals(obj)) {
            return true;
        } else if (obj instanceof String) {
            final String key = (String) obj;
            return key.equals(_name);
        }

        throw new CloudRuntimeException("Comparing ConfigKey to " + obj.toString());
    }

    public T value() {
        if (_value == null || isDynamic()) {
            final ConfigurationVO vo = s_depot != null ? s_depot.global().findById(key()) : null;
            _value = valueOf((vo != null && vo.getValue() != null) ? vo.getValue() : defaultValue());
        }

        return _value;
    }

    public T valueIn(final Long id) {
        if (id == null) {
            return value();
        }

        final String value = s_depot != null ? s_depot.scoped(this).getConfigValue(id, this) : null;
        if (value == null) {
            return value();
        } else {
            return valueOf(value);
        }
    }

    protected T valueOf(final String value) {
        Number multiplier = 1;
        if (multiplier() != null) {
            multiplier = (Number) multiplier();
        }
        final Class<T> type = type();
        if (type.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        } else if (type.isAssignableFrom(Integer.class)) {
            return (T) new Integer(Integer.parseInt(value) * multiplier.intValue());
        } else if (type.isAssignableFrom(Long.class)) {
            return (T) new Long(Long.parseLong(value) * multiplier.longValue());
        } else if (type.isAssignableFrom(Short.class)) {
            return (T) new Short(Short.parseShort(value));
        } else if (type.isAssignableFrom(String.class)) {
            return (T) value;
        } else if (type.isAssignableFrom(Float.class)) {
            return (T) new Float(Float.parseFloat(value) * multiplier.floatValue());
        } else if (type.isAssignableFrom(Double.class)) {
            return (T) new Double(Double.parseDouble(value) * multiplier.doubleValue());
        } else if (type.isAssignableFrom(String.class)) {
            return (T) value;
        } else if (type.isAssignableFrom(Date.class)) {
            return (T) Date.valueOf(value);
        } else if (type.isAssignableFrom(Character.class)) {
            return (T) new Character(value.charAt(0));
        } else {
            throw new CloudRuntimeException("Unsupported data type for config values: " + type);
        }
    }

    public static enum Scope {
        Global, Zone, Cluster, StoragePool, Account, ManagementServer
    }
}
