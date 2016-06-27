package org.apache.cloudstack.region;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "region")
public class RegionVO implements Region {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "end_point")
    private String endPoint;

    @Column(name = "gslb_service_enabled")
    private boolean gslbEnabled;

    @Column(name = "portableip_service_enabled")
    private boolean portableipEnabled;

    public RegionVO() {
    }

    public RegionVO(final int id, final String name, final String endPoint) {
        this.id = id;
        this.name = name;
        this.endPoint = endPoint;
        this.gslbEnabled = true;
    }

    public boolean getGslbEnabled() {
        return gslbEnabled;
    }

    public void setGslbEnabled(final boolean gslbEnabled) {
        this.gslbEnabled = gslbEnabled;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(final String endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public boolean checkIfServiceEnabled(final Service service) {
        if (Service.Gslb.equals(service)) {
            return gslbEnabled;
        } else if (Service.PortableIp.equals(service)) {
            return portableipEnabled;
        } else {
            assert false : "Unknown Region level Service";
            return false;
        }
    }

    @Override
    public void enableService(final org.apache.cloudstack.region.Region.Service service) {
        if (Service.Gslb.equals(service)) {
            this.gslbEnabled = true;
        } else if (Service.PortableIp.equals(service)) {
            this.portableipEnabled = true;
        } else {
            assert false : "Unknown Region level Service";
            return;
        }
    }

    public boolean getPortableipEnabled() {
        return portableipEnabled;
    }

    public void setPortableipEnabled(final boolean portableipEnabled) {
        this.portableipEnabled = portableipEnabled;
    }
}
