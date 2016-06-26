//

//

package com.cloud.utils.exception;

import com.cloud.utils.Pair;
import com.cloud.utils.SerialVersionUID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * wrap exceptions that you know there's no point in dealing with.
 */
public class CloudRuntimeException extends RuntimeException implements ErrorContext {

    private static final long serialVersionUID = SerialVersionUID.CloudRuntimeException;

    // This holds a list of uuids and their descriptive names.
    transient protected ArrayList<ExceptionProxyObject> idList = new ArrayList<>();

    transient protected ArrayList<Pair<Class<?>, String>> uuidList = new ArrayList<>();

    protected int csErrorCode;

    public CloudRuntimeException(final String message) {
        super(message);
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    public CloudRuntimeException(final String message, final Throwable th) {
        super(message, th);
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    protected CloudRuntimeException() {
        super();

        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    public CloudRuntimeException(final Throwable t) {
        super(t.getMessage(), t);
    }

    public void addProxyObject(final ExceptionProxyObject obj) {
        idList.add(obj);
    }

    public void addProxyObject(final String uuid) {
        idList.add(new ExceptionProxyObject(uuid, null));
    }

    public void addProxyObject(final String voObjUuid, final String description) {
        final ExceptionProxyObject proxy = new ExceptionProxyObject(voObjUuid, description);
        idList.add(proxy);
    }

    @Override
    public CloudRuntimeException add(final Class<?> entity, final String uuid) {
        uuidList.add(new Pair<>(entity, uuid));
        return this;
    }

    @Override
    public List<Pair<Class<?>, String>> getEntitiesInError() {
        return uuidList;
    }

    public ArrayList<ExceptionProxyObject> getIdProxyList() {
        return idList;
    }

    public int getCSErrorCode() {
        return csErrorCode;
    }

    public void setCSErrorCode(final int cserrcode) {
        csErrorCode = cserrcode;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        final int idListSize = idList.size();
        out.writeInt(idListSize);
        for (final ExceptionProxyObject proxy : idList) {
            out.writeObject(proxy);
        }

        final int uuidListSize = uuidList.size();
        out.writeInt(uuidListSize);
        for (final Pair<Class<?>, String> entry : uuidList) {
            out.writeObject(entry.first().getCanonicalName());
            out.writeObject(entry.second());
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        final int idListSize = in.readInt();
        if (idList == null) {
            idList = new ArrayList<>();
        }
        if (uuidList == null) {
            uuidList = new ArrayList<>();
        }

        for (int i = 0; i < idListSize; i++) {
            final ExceptionProxyObject proxy = (ExceptionProxyObject) in.readObject();

            idList.add(proxy);
        }

        final int uuidListSize = in.readInt();
        for (int i = 0; i < uuidListSize; i++) {
            final String clzName = (String) in.readObject();
            final String val = (String) in.readObject();

            uuidList.add(new Pair<>(Class.forName(clzName), val));
        }
    }
}
