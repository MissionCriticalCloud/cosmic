package com.cloud.network;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Network includes all of the enums used within networking.
 */
public class Networks {

    public enum RouterPrivateIpStrategy {
        None, DcGlobal, // global to data center
        HostLocal;

        public static final String DummyPrivateIp = "169.254.1.1";
    }

    public enum IsolationType {
        None(null, null), Ec2("ec2", String.class), Vlan("vlan", Integer.class) {
            @Override
            public <T> URI toUri(final T value) {
                try {
                    if (value.toString().contains(":")) {
                        return new URI(value.toString());
                    } else {
                        return new URI("vlan", value.toString(), null, null);
                    }
                } catch (final URISyntaxException e) {
                    throw new CloudRuntimeException("Unable to convert to isolation URI: " + value);
                }
            }
        },
        Vswitch("vs", String.class), Undecided(null, null), Vnet("vnet", Long.class);

        private final String scheme;
        private final Class<?> type;

        private IsolationType(final String scheme, final Class<?> type) {
            this.scheme = scheme;
            this.type = type;
        }

        public String scheme() {
            return scheme;
        }

        public Class<?> type() {
            return type;
        }

        public <T> URI toUri(final T value) {
            try {
                return new URI(scheme + "://" + value.toString());
            } catch (final URISyntaxException e) {
                throw new CloudRuntimeException("Unable to convert to isolation type URI: " + value);
            }
        }
    }

    public enum BroadcastScheme {
        Vlan("vlan"), VSwitch("vswitch");

        private final String scheme;

        private BroadcastScheme(final String scheme) {
            this.scheme = scheme;
        }

        @Override
        public String toString() {
            return scheme;
        }
    }
}
