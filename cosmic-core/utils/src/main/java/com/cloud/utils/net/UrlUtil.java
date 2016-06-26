//

//

package com.cloud.utils.net;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UrlUtil {
    public final static Map<String, String> parseQueryParameters(final URL url) {
        return parseQueryParameters(url.getQuery());
    }

    public final static Map<String, String> parseQueryParameters(final String query) {
        final HashMap<String, String> values = new HashMap<>();
        parseQueryParameters(query, false, values);

        return values;
    }

    public final static void parseQueryParameters(String query, final boolean lowercaseKeys, final Map<String, String> params) {
        if (query == null) {
            return;
        }

        if (query.startsWith("?")) {
            query = query.substring(1);
        }

        final String[] parts = query.split("&");
        for (final String part : parts) {
            final String[] tokens = part.split("=");

            if (lowercaseKeys) {
                tokens[0] = tokens[0].toLowerCase();
            }

            params.put(tokens[0], tokens[1]);
        }
    }

    public final static Map<String, String> parseQueryParameters(final URI url) {
        return parseQueryParameters(url.getQuery());
    }
}
