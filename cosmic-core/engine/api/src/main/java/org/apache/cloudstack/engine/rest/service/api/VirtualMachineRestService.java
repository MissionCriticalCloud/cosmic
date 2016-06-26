package org.apache.cloudstack.engine.rest.service.api;

import org.apache.cloudstack.engine.cloud.entity.api.VirtualMachineEntity;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

@Produces("application/xml")
public class VirtualMachineRestService {

    @GET
    @Path("/vm/{vmid}")
    public VirtualMachineEntity get(@PathParam("vmid") final String vmId) {
        return null;
    }

    @PUT
    @Path("/vm/create")
    public VirtualMachineEntity create(@QueryParam("xid") final String xid, @QueryParam("hostname") final String hostname, @QueryParam("display-name") final String displayName) {
        return null;
    }

    @GET
    @Path("/vms")
    public List<VirtualMachineEntity> listAll() {
        return null;
    }
}
