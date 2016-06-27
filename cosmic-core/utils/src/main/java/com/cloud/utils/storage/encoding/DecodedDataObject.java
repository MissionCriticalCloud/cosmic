//

//

package com.cloud.utils.storage.encoding;

public class DecodedDataObject {
    private final String objType;
    private final Long size;
    private final String name;
    private final String path;
    private final DecodedDataStore store;

    public DecodedDataObject(final String objType, final Long size, final String name, final String path, final DecodedDataStore store) {
        this.objType = objType;
        this.size = size;
        this.name = name;
        this.path = path;
        this.store = store;
    }

    public String getObjType() {
        return objType;
    }

    public Long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public DecodedDataStore getStore() {
        return store;
    }
}
