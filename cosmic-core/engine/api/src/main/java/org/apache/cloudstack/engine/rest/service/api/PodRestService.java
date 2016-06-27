package org.apache.cloudstack.engine.rest.service.api;

import org.apache.cloudstack.engine.datacenter.entity.api.PodEntity;
import org.apache.cloudstack.engine.service.api.ProvisioningService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Produces({"application/json"})
public class PodRestService {
    //  @Inject
    ProvisioningService _provisioningService;

    @GET
    @Path("/pod/{pod-id}")
    public PodEntity getPod(@PathParam("pod-id") final String podId) {
        return null;
    }

    @POST
    @Path("/pod/{pod-id}/enable")
    public String enable(@PathParam("pod-id") final String podId) {
        return null;
    }

    @POST
    @Path("/pod/{pod-id}/disable")
    public String disable(@PathParam("pod-id") final String podId) {
        return null;
    }

    @POST
    @Path("/pod/{pod-id}/deactivate")
    public String deactivate(@PathParam("pod-id") final String podId) {
        return null;
    }

    @POST
    @Path("/pod/{pod-id}/reactivate")
    public String reactivate(@PathParam("pod-id") final String podId) {
        return null;
    }

    @PUT
    @Path("/pod/create")
    public PodEntity create(@QueryParam("xid") final String xid, @QueryParam("display-name") final String displayName) {
        return null;
    }

    @PUT
    @Path("/pod/{pod-id}")
    public PodEntity update(@PathParam("pod-id") final String podId, @QueryParam("display-name") final String displayName) {
        return null;
    }
}
