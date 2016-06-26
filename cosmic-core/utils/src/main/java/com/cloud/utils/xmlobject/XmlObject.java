//

//

package com.cloud.utils.xmlobject;

import com.cloud.utils.exception.CloudRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlObject {
    private final Logger logger = LoggerFactory.getLogger(XmlObject.class.getName());
    private final Map<String, Object> elements = new HashMap<>();
    private String text;
    private String tag;

    XmlObject() {
    }

    public XmlObject(final String tag) {
        this.tag = tag;
    }

    public void removeAllChildren() {
        elements.clear();
    }

    public XmlObject putElement(final String key, final Object e) {
        if (e == null) {
            throw new IllegalArgumentException(String.format("element[%s] can not be null", key));
        }
        final Object old = elements.get(key);
        if (old == null) {
            //System.out.println(String.format("no %s, add new", key));
            elements.put(key, e);
        } else {
            if (old instanceof List) {
                //System.out.println(String.format("already list %s, add", key));
                ((List) old).add(e);
            } else {
                //System.out.println(String.format("not list list %s, add list", key));
                final List lst = new ArrayList();
                lst.add(old);
                lst.add(e);
                elements.put(key, lst);
            }
        }

        return this;
    }

    public void removeElement(final String key) {
        elements.remove(key);
    }

    private Object recurGet(final XmlObject obj, final Iterator<String> it) {
        final String key = it.next();
        final Object e = obj.elements.get(key);
        if (e == null) {
            return null;
        }

        if (!it.hasNext()) {
            return e;
        } else {
            if (!(e instanceof XmlObject)) {
                throw new CloudRuntimeException(String.format("%s doesn't reference to a XmlObject", it.next()));
            }
            return recurGet((XmlObject) e, it);
        }
    }

    public <T> T get(final String elementStr) {
        final String[] strs = elementStr.split("\\.");
        final List<String> lst = new ArrayList<>(strs.length);
        Collections.addAll(lst, strs);
        return (T) recurGet(this, lst.iterator());
    }

    public <T> List<T> getAsList(final String elementStr) {
        final Object e = get(elementStr);
        if (e instanceof List) {
            return (List<T>) e;
        }

        final List lst = new ArrayList(1);
        if (e != null) {
            lst.add(e);
        }

        return lst;
    }

    public String getText() {
        return text;
    }

    public XmlObject setText(final String text) {
        this.text = text;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public XmlObject setTag(final String tag) {
        this.tag = tag;
        return this;
    }

    public String dump() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag);
        final List<XmlObject> children = new ArrayList<>();
        for (final Map.Entry<String, Object> e : elements.entrySet()) {
            final String key = e.getKey();
            final Object val = e.getValue();
            if (val instanceof String) {
                sb.append(String.format(" %s=\"%s\"", key, val.toString()));
            } else if (val instanceof XmlObject) {
                children.add((XmlObject) val);
            } else if (val instanceof List) {
                children.addAll((Collection<? extends XmlObject>) val);
            } else {
                throw new CloudRuntimeException(String.format("unsupported element type[tag:%s, class: %s], only allowed type of [String, List<XmlObject>, Object]", key,
                        val.getClass().getName()));
            }
        }

        if (!children.isEmpty() && text != null) {
            logger.info(String.format("element %s cannot have both text[%s] and child elements, set text to null", tag, text));
            text = null;
        }

        if (!children.isEmpty()) {
            sb.append(">");
            for (final XmlObject x : children) {
                sb.append(x.dump());
            }
            sb.append(String.format("</%s>", tag));
        } else {
            if (text != null) {
                sb.append(">");
                sb.append(text);
                sb.append(String.format("</%s>", tag));
            } else {
                sb.append(" />");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("<" + tag);
        for (final Map.Entry<String, Object> e : elements.entrySet()) {
            final String key = e.getKey();
            final Object value = e.getValue();
            if (!(value instanceof String)) {
                continue;
            }
            sb.append(String.format(" %s=\"%s\"", key, value.toString()));
        }

        if (text == null || "".equals(text.trim())) {
            sb.append(" />");
        } else {
            sb.append(">").append(text).append(String.format("</ %s>", tag));
        }
        return sb.toString();
    }

    public <T> T evaluateObject(final T obj) {
        Class<?> clazz = obj.getClass();
        try {
            do {
                final Field[] fs = clazz.getDeclaredFields();
                for (final Field f : fs) {
                    f.setAccessible(true);
                    final Object value = get(f.getName());
                    f.set(obj, value);
                }
                clazz = clazz.getSuperclass();
            } while (clazz != null && clazz != Object.class);
            return obj;
        } catch (final Exception e) {
            throw new CloudRuntimeException(e);
        }
    }
}
