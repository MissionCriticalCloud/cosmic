//

//

package com.cloud.utils.mgmt;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class PropertyMapDynamicBean implements DynamicMBean {

    private Map<String, Object> _propMap = new HashMap<>();

    public PropertyMapDynamicBean() {
    }

    public PropertyMapDynamicBean(final Map<String, Object> propMap) {
        _propMap = propMap;
    }

    @Override
    public synchronized Object getAttribute(final String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (_propMap != null) {
            return _propMap.get(name);
        }

        throw new AttributeNotFoundException("No such property " + name);
    }

    @Override
    public synchronized void setAttribute(final Attribute attr) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        final String name = attr.getName();
        if (name != null) {
            _propMap.put(name, attr.getValue());
        }
    }

    @Override
    public synchronized AttributeList getAttributes(final String[] names) {
        final AttributeList list = new AttributeList();
        for (final String name : names) {
            final Object value = _propMap.get(name);
            if (value != null) {
                list.add(new Attribute(name, value));
            }
        }
        return list;
    }

    @Override
    public synchronized AttributeList setAttributes(final AttributeList list) {
        final Attribute[] attrs = list.toArray(new Attribute[0]);
        final AttributeList retList = new AttributeList();
        for (final Attribute attr : attrs) {
            final String name = attr.getName();
            final Object value = attr.getValue();
            _propMap.put(name, value);
            retList.add(new Attribute(name, value));
        }
        return retList;
    }

    @Override
    public synchronized Object invoke(final String name, final Object[] args, final String[] sig) throws MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {
        final SortedSet<String> names = new TreeSet<>();

        for (final String name : _propMap.keySet()) {
            names.add(name);
        }

        final MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[names.size()];
        final Iterator<String> it = names.iterator();
        for (int i = 0; i < attrs.length; i++) {
            final String name = it.next();
            attrs[i] = new MBeanAttributeInfo(name, "java.lang.String", name, true,   // isReadable
                    true,   // isWritable
                    false); // isIs
        }

        return new MBeanInfo(this.getClass().getName(), "Dynamic MBean", attrs, null, null, null);
    }

    public synchronized void addProp(final String name, final Object value) {
        _propMap.put(name, value);
    }

    public synchronized Object getProp(final String name) {
        return _propMap.get(name);
    }
}
