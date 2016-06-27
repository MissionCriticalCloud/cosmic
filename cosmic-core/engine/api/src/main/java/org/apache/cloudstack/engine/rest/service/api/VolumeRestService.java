package org.apache.cloudstack.engine.rest.service.api;

import org.apache.cloudstack.engine.cloud.entity.api.VolumeEntity;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

@Produces("application/json")
public class VolumeRestService {

    @PUT
    @Path("/vol/create")
    public VolumeEntity create(@QueryParam("xid") final String xid, @QueryParam("display-name") final String displayName) {
        return null;
    }

    @POST
    @Path("/vol/{volid}/deploy")
    public String deploy(@PathParam("volid") final String volumeId) {
        return null;
    }

    @GET
    @Path("/vols")
    public List<VolumeEntity> listAll() {
        return null;
    }

    @POST
    @Path("/vol/{volid}/attach-to")
    public String attachTo(@PathParam("volid") final String volumeId, @QueryParam("vmid") final String vmId, @QueryParam("device-order") final short device) {
        return null;
    }

    @DELETE
    @Path("/vol/{volid}")
    public String delete(@PathParam("volid") final String volumeId) {
        return null;
    }

    @POST
    @Path("/vol/{volid}/detach")
    public String detach(@QueryParam("volid") final String volumeId) {
        return null;
    }
}
