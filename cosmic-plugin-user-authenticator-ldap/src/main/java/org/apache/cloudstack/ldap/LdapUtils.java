package org.apache.cloudstack.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public final class LdapUtils {
    private LdapUtils() {
    }

    public static String escapeLDAPSearchFilter(final String filter) {
        final StringBuilder sb = new StringBuilder();
        for (final char character : filter.toCharArray()) {
            switch (character) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000':
                    sb.append("\\00");
                    break;
                default:
                    sb.append(character);
            }
        }
        return sb.toString();
    }

    public static String getAttributeValue(final Attributes attributes, final String attributeName) throws NamingException {
        final Attribute attribute = attributes.get(attributeName);
        if (attribute != null) {
            final Object value = attribute.get();
            return String.valueOf(value);
        }
        return null;
    }
}
