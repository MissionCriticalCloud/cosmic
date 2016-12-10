package com.cloud.engine.datacenter.entity.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Describes a zone and operations that can be done in a zone.
 */
@Path("/zone/{zoneid}")
@Produces({"application/json"})
@XmlRootElement(name = "zone")
public interface ZoneEntity extends DataCenterResourceEntity {
    @GET
    @Path("/pods")
    List<PodEntity> listPods();
}
