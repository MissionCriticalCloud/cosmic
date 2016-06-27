package org.apache.cloudstack.spring.lifecycle.registry;

import com.cloud.utils.component.Registry;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

public class ExtensionRegistry implements Registry<Object>, Configurable, BeanNameAware {

    private static final Logger log = LoggerFactory.getLogger(ExtensionRegistry.class);

    String name;
    String beanName;

    String orderConfigKey;
    String orderConfigDefault;
    ConfigKey<String> orderConfigKeyObj;

    String excludeKey;
    String excludeDefault;
    ConfigKey<String> excludeKeyObj;

    String configComponentName;
    List<Object> preRegistered;
    List<Object> registered = new CopyOnWriteArrayList<>();
    List<Object> readOnly = Collections.unmodifiableList(registered);

    @Override
    public String getConfigComponentName() {
        return configComponentName == null ? this.getClass().getSimpleName() : configComponentName;
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        final List<ConfigKey<String>> result = new ArrayList<>();

        if (orderConfigKey != null && orderConfigKeyObj == null) {
            orderConfigKeyObj = new ConfigKey<>("Advanced", String.class, orderConfigKey, orderConfigDefault, "The order of precedence for the extensions", false);
        }

        if (orderConfigKeyObj != null) {
            result.add(orderConfigKeyObj);
        }

        if (excludeKey != null && excludeKeyObj == null) {
            excludeKeyObj = new ConfigKey<>("Advanced", String.class, excludeKey, excludeDefault, "Extensions to exclude from being registered", false);
        }

        if (excludeKeyObj != null) {
            result.add(excludeKeyObj);
        }

        return result.toArray(new ConfigKey[result.size()]);
    }

    public void setConfigComponentName(final String configComponentName) {
        this.configComponentName = configComponentName;
    }

    @PostConstruct
    public void init() {
        if (name == null) {
            for (String part : beanName.replaceAll("([A-Z])", " $1").split("\\s+")) {
                part = StringUtils.capitalize(part.toLowerCase());

                name = name == null ? part : name + " " + part;
            }
        }

        if (preRegistered != null) {
            for (final Object o : preRegistered) {
                register(o);
            }
        }
    }

    @Override
    public boolean register(final Object item) {
        if (registered.contains(item)) {
            return false;
        }

        String[] order = new String[]{};
        final Set<String> exclude = new HashSet<>();

        if (orderConfigKeyObj != null) {
            final Object value = orderConfigKeyObj.value();
            if (value != null && value.toString().trim().length() > 0) {
                order = value.toString().trim().split("\\s*,\\s*");
            }
        }

        if (excludeKeyObj != null) {
            final Object value = excludeKeyObj.value();
            if (value != null && value.toString().trim().length() > 0) {
                for (final String e : value.toString().trim().split("\\s*,\\s*")) {
                    exclude.add(e);
                }
            }
        }

        final String name = RegistryUtils.getName(item);

        if (name != null && exclude.size() > 0 && exclude.contains(name)) {
            return false;
        }

        if (name == null && order.length > 0) {
            throw new RuntimeException("getName() is null for [" + item + "]");
        }

        int i = 0;
        for (final String orderTest : order) {
            if (orderTest.equals(name)) {
                registered.add(i, item);
                i = -1;
                break;
            }

            if (registered.size() <= i) {
                break;
            }

            if (RegistryUtils.getName(registered.get(i)).equals(orderTest)) {
                i++;
            }
        }

        if (i != -1) {
            registered.add(item);
        }

        log.debug("Registering extension [{}] in [{}]", name, this.name);

        return true;
    }

    @Override
    public void unregister(final Object type) {
        registered.remove(type);
    }

    @Override
    public List<Object> getRegistered() {
        return readOnly;
    }

    public String getOrderConfigKey() {
        return orderConfigKey;
    }

    public void setOrderConfigKey(final String orderConfigKey) {
        this.orderConfigKey = orderConfigKey;
    }

    public String getOrderConfigDefault() {
        return orderConfigDefault;
    }

    public void setOrderConfigDefault(final String orderConfigDefault) {
        this.orderConfigDefault = orderConfigDefault;
    }

    public String getExcludeKey() {
        return excludeKey;
    }

    public void setExcludeKey(final String excludeKey) {
        this.excludeKey = excludeKey;
    }

    public String getExcludeDefault() {
        return excludeDefault;
    }

    public void setExcludeDefault(final String excludeDefault) {
        this.excludeDefault = excludeDefault;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void setBeanName(final String name) {
        beanName = name;
    }

    public List<Object> getPreRegistered() {
        return preRegistered;
    }

    public void setPreRegistered(final List<Object> preRegistered) {
        this.preRegistered = preRegistered;
    }
}
