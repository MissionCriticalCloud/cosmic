//

//

package com.cloud.utils.storage.encoding;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Decoder {
    private static final Logger s_logger = LoggerFactory.getLogger(Decoder.class);

    public static DecodedDataObject decode(final String url) throws URISyntaxException {
        final URI uri = new URI(url);
        final Map<String, String> params = getParameters(uri);
        final DecodedDataStore store =
                new DecodedDataStore(params.get(EncodingType.ROLE.toString()), params.get(EncodingType.STOREUUID.toString()),
                        params.get(EncodingType.PROVIDERNAME.toString()), uri.getScheme(), uri.getScheme() + uri.getHost() + uri.getPath(), uri.getHost(), uri.getPath());

        Long size = null;
        try {
            size = Long.parseLong(params.get(EncodingType.SIZE.toString()));
        } catch (final NumberFormatException e) {
            s_logger.info("[ignored] number not recognised", e);
        }
        final DecodedDataObject obj =
                new DecodedDataObject(params.get(EncodingType.OBJTYPE.toString()), size, params.get(EncodingType.NAME.toString()), params.get(EncodingType.PATH.toString()),
                        store);
        return obj;
    }

    private static Map<String, String> getParameters(final URI uri) {
        final String parameters = uri.getQuery();
        final Map<String, String> params = new HashMap<>();
        final List<String> paraLists = Arrays.asList(parameters.split("&"));
        for (final String para : paraLists) {
            final String[] pair = para.split("=");
            if (!pair[1].equalsIgnoreCase("null")) {
                params.put(pair[0], pair[1]);
            }
        }
        return params;
    }
}
