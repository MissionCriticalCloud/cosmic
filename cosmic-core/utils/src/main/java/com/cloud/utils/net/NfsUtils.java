//

//

package com.cloud.utils.net;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class NfsUtils {

    public static String url2Mount(final String urlStr) throws URISyntaxException {
        final URI url;
        url = new URI(urlStr);
        return url.getHost() + ":" + url.getPath();
    }

    public static String uri2Mount(final URI uri) {
        return uri.getHost() + ":" + uri.getPath();
    }

    public static String url2PathSafeString(final String urlStr) {
        String safe = urlStr.replace(File.separatorChar, '-');
        safe = safe.replace("?", "");
        safe = safe.replace("*", "");
        safe = safe.replace("\\", "");
        safe = safe.replace("/", "");
        return safe;
    }

    public static String getHostPart(final String nfsPath) {
        final String[] toks = nfsPath.split(":");
        if (toks != null && toks.length == 2) {
            return toks[0];
        }
        return null;
    }
}
