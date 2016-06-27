package com.cloud.network;

import com.cloud.utils.exception.CloudRuntimeException;

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

    /**
     * Different ways to assign ip address to this network.
     */
    public enum Mode {
        None, Static, Dhcp, ExternalDhcp
    }

    public enum AddressFormat {
        Ip4, Ip6, DualStack
    }

    /**
     * Different types of broadcast domains.
     */
    public enum BroadcastDomainType {
        Native(null, null) {
            @Override
            public <T> URI toUri(final T value) {
                try {
                    if (value.toString().contains("://")) {
                        return new URI(value.toString());
                    } else
                    // strange requirement but this is how the code expects it
                    {
                        return new URI("vlan://" + value.toString());
                    }
                } catch (final URISyntaxException e) {
                    throw new CloudRuntimeException("Unable to convert to broadcast URI: " + value);
                }
            }
        },
        Vlan("vlan", Integer.class) {
            @Override
            public <T> URI toUri(final T value) {
                try {
                    if (value.toString().contains("://")) {
                        return new URI(value.toString());
                    } else {
                        return new URI("vlan://" + value.toString());
                    }
                } catch (final URISyntaxException e) {
                    throw new CloudRuntimeException("Unable to convert to broadcast URI: " + value);
                }
            }
        },
        Vswitch("vs", String.class), LinkLocal(null, null), Vnet("vnet", Long.class), Storage("storage", Integer.class), Lswitch("lswitch", String.class) {
            @Override
            public <T> URI toUri(final T value) {
                try {
                    return new URI("lswitch", value.toString(), null, null);
                } catch (final URISyntaxException e) {
                    throw new CloudRuntimeException("Unable to convert to broadcast URI: " + value);
                }
            }

            /**
             * gets scheme specific part instead of host
             */
            @Override
            public String getValueFrom(final URI uri) {
                return uri.getSchemeSpecificPart();
            }
        },
        Mido("mido", String.class), Pvlan("pvlan", String.class),
        Vxlan("vxlan", Long.class) {
            @Override
            public <T> URI toUri(final T value) {
                try {
                    if (value.toString().contains("://")) {
                        return new URI(value.toString());
                    } else {
                        return new URI("vxlan://" + value.toString());
                    }
                } catch (final URISyntaxException e) {
                    throw new CloudRuntimeException("Unable to convert to broadcast URI: " + value);
                }
            }
        },
        Vcs("vcs", Integer.class) {
            @Override
            public <T> URI toUri(final T value) {
                try {
                    if (value.toString().contains("://")) {
                        return new URI(value.toString());
                    } else {
                        return new URI("vcs://" + value.toString());
                    }
                } catch (final URISyntaxException e) {
                    throw new CloudRuntimeException("Unable to convert to broadcast URI: " + value);
                }
            }
        },
        UnDecided(null, null),
        Vsp("vsp", String.class),;

        private final String scheme;
        private final Class<?> type;

        private BroadcastDomainType(final String scheme, final Class<?> type) {
            this.scheme = scheme;
            this.type = type;
        }

        /**
         * gets the type from a string encoded uri
         *
         * @param str the uri string
         * @return the scheme as BroadcastDomainType
         * @throws URISyntaxException when the string can not be converted to URI
         */
        public static BroadcastDomainType getTypeOf(final String str) throws URISyntaxException {
            if (com.cloud.dc.Vlan.UNTAGGED.equalsIgnoreCase(str)) {
                return Native;
            }
            return getSchemeValue(new URI(str));
        }

        /**
         * get the enum value from this uri
         *
         * @param uri to get the scheme value from
         * @return the scheme as BroadcastDomainType
         */
        public static BroadcastDomainType getSchemeValue(final URI uri) {
            return toEnumValue(uri.getScheme());
        }

        /**
         * converts a String to a BroadcastDomainType
         *
         * @param scheme convert a string representation to a BroacastDomainType
         * @return the value of this
         */
        public static BroadcastDomainType toEnumValue(final String scheme) {
            // scheme might be null and some of the enumvalue.scheme are as well, so
            if (scheme == null) {
                return UnDecided;
            }
            for (final BroadcastDomainType type : values()) {
                if (scheme.equalsIgnoreCase(type.scheme())) {
                    return type;
                }
            }
            return UnDecided;
        }

        /**
         * @return scheme to be used in broadcast uri. Null indicates that this
         * type does not have broadcast tags.
         */
        public String scheme() {
            return scheme;
        }

        /**
         * get the BroadcastDomain value from an arbitrary String
         * TODO what when the uriString is useless
         *
         * @param uriString the string encoded uri
         * @return depending on the scheme/BroadcastDomainType
         * @throws URISyntaxException the string is not even an uri
         */
        public static String getValue(final String uriString) throws URISyntaxException {
            return getValue(new URI(uriString));
        }

        /**
         * get the BroadcastDomain value from an arbitrary URI
         * TODO what when the uri is useless
         *
         * @param uri the uri
         * @return depending on the scheme/BroadcastDomainType
         */
        public static String getValue(final URI uri) {
            return getSchemeValue(uri).getValueFrom(uri);
        }

        /**
         * The default implementation of getValueFrom returns the host part of the uri
         *
         * @param uri to get the value from
         * @return the host part as String
         */
        public String getValueFrom(final URI uri) {
            return uri.getHost();
        }

        /**
         * encode a string into a BroadcastUri
         *
         * @param candidate the input string
         * @return an URI containing an appropriate (possibly given) scheme and the value
         */
        public static URI fromString(final String candidate) {
            try {
                Long.parseLong(candidate);
                return Vlan.toUri(candidate);
            } catch (final NumberFormatException nfe) {
                if (com.cloud.dc.Vlan.UNTAGGED.equalsIgnoreCase(candidate)) {
                    return Native.toUri(candidate);
                }
                try {
                    final URI uri = new URI(candidate);
                    final BroadcastDomainType tiep = getSchemeValue(uri);
                    if (tiep.scheme != null && tiep.scheme.equals(uri.getScheme())) {
                        return uri;
                    } else {
                        throw new CloudRuntimeException("string '" + candidate + "' has an unknown BroadcastDomainType.");
                    }
                } catch (final URISyntaxException e) {
                    throw new CloudRuntimeException("string is not a broadcast URI: " + candidate);
                }
            }
        }

        /**
         * The default implementation of toUri returns an uri with the scheme and value as host
         *
         * @param value will be put in the host field
         * @return the resulting URI
         */
        public <T> URI toUri(final T value) {
            try {
                return new URI(scheme + "://" + value.toString());
            } catch (final URISyntaxException e) {
                throw new CloudRuntimeException("Unable to convert to broadcast URI: " + value);
            }
        }

        /**
         * @return type of the value in the broadcast uri. Null indicates that
         * this type does not have broadcast tags.
         */
        public Class<?> type() {
            return type;
        }
    }

    /**
     * Different types of network traffic in the data center.
     */
    public enum TrafficType {
        None, Public, Guest, Storage, Management, Control, Vpn;

        public static boolean isSystemNetwork(final TrafficType trafficType) {
            if (Storage.equals(trafficType) || Management.equals(trafficType) || Control.equals(trafficType)) {
                return true;
            }
            return false;
        }

        public static TrafficType getTrafficType(final String type) {
            if ("Public".equals(type)) {
                return Public;
            } else if ("Guest".equals(type)) {
                return Guest;
            } else if ("Storage".equals(type)) {
                return Storage;
            } else if ("Management".equals(type)) {
                return Management;
            } else if ("Control".equals(type)) {
                return Control;
            } else if ("Vpn".equals(type)) {
                return Vpn;
            } else {
                return None;
            }
        }
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
