package org.apache.cloudstack.framework.config.impl;

import com.cloud.utils.crypt.DBEncryptionUtil;
import org.apache.cloudstack.config.Configuration;
import org.apache.cloudstack.framework.config.ConfigKey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "configuration")
public class ConfigurationVO implements Configuration {
    @Column(name = "instance")
    private String instance;

    @Column(name = "component")
    private String component;

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "value", length = 8191)
    private String value;

    @Column(name = "default_value", length = 8191)
    private String defaultValue;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "is_dynamic")
    private boolean dynamic;

    @Column(name = "scope")
    private String scope;

    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date updated;

    protected ConfigurationVO() {
    }

    public ConfigurationVO(final String component, final ConfigKey<?> key) {
        this(key.category(), "DEFAULT", component, key.key(), key.defaultValue(), key.description());
        defaultValue = key.defaultValue();
        dynamic = key.isDynamic();
        scope = key.scope() != null ? key.scope().toString() : null;
    }

    public ConfigurationVO(final String category, final String instance, final String component, final String name, final String value, final String description) {
        this.category = category;
        this.instance = instance;
        this.component = component;
        this.name = name;
        this.description = description;
        setValue(value);
    }

    private boolean isEncryptedConfig() {
        return "Hidden".equals(getCategory()) || "Secure".equals(getCategory());
    }

    @Override
    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    @Override
    public String getInstance() {
        return instance;
    }

    public void setInstance(final String instance) {
        this.instance = instance;
    }

    @Override
    public String getComponent() {
        return component;
    }

    public void setComponent(final String component) {
        this.component = component;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        if (isEncryptedConfig()) {
            return DBEncryptionUtil.decrypt(value);
        } else {
            return value;
        }
    }

    public void setValue(final String value) {
        if (isEncryptedConfig()) {
            this.value = DBEncryptionUtil.encrypt(value);
        } else {
            this.value = value;
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(final boolean dynamic) {
        this.dynamic = dynamic;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(final Date updated) {
        this.updated = updated;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
