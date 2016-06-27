package org.apache.cloudstack.engine.rest.service.api;

import org.apache.cloudstack.engine.datacenter.entity.api.ClusterEntity;
import org.apache.cloudstack.engine.service.api.ProvisioningService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

@Produces("application/json")
public class ClusterRestService {
    //    @Inject
    ProvisioningService _provisioningService;

    @GET
    @Path("/clusters")
    public List<ClusterEntity> listAll() {
        return null;
    }

    @GET
    @Path("/cluster/{clusterid}")
    public ClusterEntity get(@PathParam("clusterid") final String clusterId) {
        return null;
    }

    @POST
    @Path("/cluster/{clusterid}/enable")
    public String enable(@PathParam("clusterid") final String clusterId) {
        return null;
    }

    @POST
    @Path("/cluster/{clusterid}/disable")
    public String disable(@PathParam("clusterid") final String clusterId) {
        return null;
    }

    @POST
    @Path("/cluster/{clusterid}/deactivate")
    public String deactivate(@PathParam("clusterid") final String clusterId) {
        return null;
    }

    @POST
    @Path("/cluster/{clusterid}/reactivate")
    public String reactivate(@PathParam("clusterid") final String clusterId) {
        return null;
    }

    @PUT
    @Path("/cluster/create")
    public ClusterEntity create(@QueryParam("xid") final String xid, @QueryParam("display-name") final String displayName) {
        return null;
    }

    @PUT
    @Path("/cluster/{clusterid}/update")
    public ClusterEntity update(@QueryParam("display-name") final String displayName) {
        return null;
    }
}
