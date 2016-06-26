package org.apache.cloudstack.region;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public interface Region {

    public int getId();

    public String getName();

    public void setName(String name);

    public String getEndPoint();

    public boolean checkIfServiceEnabled(Service service);

    public void enableService(Service service);

    /**
     * A region level service, is a service that constitute services across one or more zones in the region or a service
     * made available to all the zones in the region.
     */
    class Service {
        private static final List<Service> regionServices = new ArrayList<>();

        public static final Service Gslb = new Service("Gslb");
        public static final Service PortableIp = new Service("PortableIp");
        private final String name;

        public Service(final String name) {
            this.name = name;
            regionServices.add(this);
        }

        public String getName() {
            return name;
        }
    }

    /**
     * A provider provides the region level service in a zone.
     */
    class Provider {

        private static final List<Provider> supportedProviders = new ArrayList<>();
        private final String name;
        private final Service service;

        public Provider(final String name, final Service service) {
            this.name = name;
            this.service = service;
            supportedProviders.add(this);
        }

        public String getName() {
            return name;
        }
    }
}
