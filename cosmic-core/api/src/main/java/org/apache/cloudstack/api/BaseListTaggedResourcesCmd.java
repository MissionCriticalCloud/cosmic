package org.apache.cloudstack.api;

import com.cloud.exception.InvalidParameterValueException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseListTaggedResourcesCmd extends BaseListProjectAndAccountResourcesCmd implements IBaseListTaggedResourcesCmd {
    @Parameter(name = ApiConstants.TAGS, type = CommandType.MAP, description = "List resources by tags (key/value pairs)")
    private Map tags;

    @Override
    public Map<String, String> getTags() {
        Map<String, String> tagsMap = null;
        if (tags != null && !tags.isEmpty()) {
            tagsMap = new HashMap<>();
            final Collection<?> servicesCollection = tags.values();
            final Iterator<?> iter = servicesCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> services = (HashMap<String, String>) iter.next();
                final String key = services.get("key");
                final String value = services.get("value");
                if (value == null) {
                    throw new InvalidParameterValueException("No value is passed in for key " + key);
                }
                tagsMap.put(key, value);
            }
        }
        return tagsMap;
    }
}
