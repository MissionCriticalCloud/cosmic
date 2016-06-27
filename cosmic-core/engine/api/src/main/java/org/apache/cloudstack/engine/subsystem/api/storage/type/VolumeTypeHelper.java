package org.apache.cloudstack.engine.subsystem.api.storage.type;

import javax.inject.Inject;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class VolumeTypeHelper {

    private static final VolumeType defaultType = new Unknown();
    private final Map<String, VolumeType> mapTypes = new Hashtable<>();
    private List<VolumeType> types;

    @Inject
    public void setTypes(final List<VolumeType> types) {
        this.types = types;

        mapTypes.clear();
        for (final VolumeType ty : this.types) {
            mapTypes.put(ty.getClass().getSimpleName().toUpperCase(), ty);
        }
    }

    public VolumeType getType(final String type) {
        if (mapTypes.containsKey(type.toUpperCase())) {
            return mapTypes.get(type.toUpperCase());
        }
        return VolumeTypeHelper.defaultType;
    }
}
