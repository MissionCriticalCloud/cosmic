package org.apache.cloudstack.engine.rest.service.api;

import org.apache.cloudstack.engine.cloud.entity.api.NetworkEntity;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

@Produces("application/json")
public class NetworkRestService {
    @PUT
    @Path("/network/create")
    public NetworkEntity create(@QueryParam("xid") final String xid, @QueryParam("display-name") final String displayName) {
        return null;
    }

    @GET
    @Path("/network/{network-id}")
    public NetworkEntity get(@PathParam("network-id") final String networkId) {
        return null;
    }

    @GET
    @Path("/networks")
    public List<NetworkEntity> listAll() {
        return null;
    }

    @POST
    @Path("/network/{network-id}/")
    public String deploy(@PathParam("network-id") final String networkId) {
        return null;
    }
}
