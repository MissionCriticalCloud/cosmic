package com.cloud.region.ha;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

/**
 * GlobalLoadBalancerRule defines a global (multi zone) load balancing configuration.
 */
public interface GlobalLoadBalancerRule extends Identity, InternalIdentity, ControlledEntity {

    public String getName();

    public String getDescription();

    public String getGslbDomain();

    public String getAlgorithm();

    public String getPersistence();

    public int getRegion();

    @Override
    public long getAccountId();

    public State getState();

    public String getServiceType();

    enum Algorithm {

        RoundRobin, LeastConn, Proximity;

        public static boolean isValidAlgorithm(final String algorithm) {
            if (RoundRobin.name().equalsIgnoreCase(algorithm) || LeastConn.name().equalsIgnoreCase(algorithm) || Proximity.name().equalsIgnoreCase(algorithm)) {
                return true;
            }
            return false;
        }
    }

    enum Persistence {

        sourceip;

        public static boolean isValidPersistence(final String persistence) {
            if (sourceip.name().equalsIgnoreCase(persistence)) {
                return true;
            }
            return false;
        }
    }

    enum ServiceType {
        tcp, udp, http;

        public static boolean isValidServiceType(final String serviceType) {
            if (tcp.name().equalsIgnoreCase(serviceType) ||
                    udp.name().equalsIgnoreCase(serviceType) ||
                    http.name().equalsIgnoreCase(serviceType)) {
                return true;
            }
            return false;
        }
    }

    enum State {
        Staged, Add, Active, Revoke
    }
}
