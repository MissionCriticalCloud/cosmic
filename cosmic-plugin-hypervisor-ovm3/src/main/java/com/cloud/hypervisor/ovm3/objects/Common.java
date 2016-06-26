package com.cloud.hypervisor.ovm3.objects;

public class Common extends OvmObject {

    public Common(final Connection connection) {
        setClient(connection);
    }

    public Integer getApiVersion() throws Ovm3ResourceException {
        final Object[] x = (Object[]) callWrapper("get_api_version");
        return (Integer) x[0];
    }

    public Boolean sleep(final int seconds) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("sleep", seconds);
    }

    public <T> String dispatch(final String url, final String function, final T... args) throws Ovm3ResourceException {
        return callString("dispatch", url, function, args);
    }

    public String echo(final String msg) throws Ovm3ResourceException {
        return callString("echo", msg);
    }
}
