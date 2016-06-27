package org.apache.cloudstack.engine.rest.service.api;

import org.apache.cloudstack.engine.datacenter.entity.api.ZoneEntity;
import org.apache.cloudstack.engine.service.api.ProvisioningService;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

@Produces({"application/json"})
public class ZoneRestService {
    //    @Inject
    ProvisioningService _provisioningService;

    @GET
    @Path("/zones")
    public List<ZoneEntity> listAll() {
        return _provisioningService.listZones();
    }

    @GET
    @Path("/zone/{zone-id}")
    public ZoneEntity get(@PathParam("zone-id") final String zoneId) {
        return _provisioningService.getZone(zoneId);
    }

    @POST
    @Path("/zone/{zone-id}/enable")
    public String enable(final String zoneId) {
        return null;
    }

    @POST
    @Path("/zone/{zone-id}/disable")
    public String disable(@PathParam("zone-id") final String zoneId) {
        final ZoneEntity zoneEntity = _provisioningService.getZone(zoneId);
        zoneEntity.disable();
        return null;
    }

    @POST
    @Path("/zone/{zone-id}/deactivate")
    public String deactivate(@PathParam("zone-id") final String zoneId) {
        return null;
    }

    @POST
    @Path("/zone/{zone-id}/activate")
    public String reactivate(@PathParam("zone-id") final String zoneId) {
        return null;
    }

    @PUT
    @Path("/zone/create")
    public ZoneEntity createZone(@QueryParam("xid") final String xid, @QueryParam("display-name") final String displayName) {
        return null;
    }

    @DELETE
    @Path("/zone/{zone-id}")
    public String deleteZone(@QueryParam("zone-id") final String xid) {
        return null;
    }
}
