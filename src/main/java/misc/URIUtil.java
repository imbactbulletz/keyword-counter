package misc;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URIUtil {

    public static String convertPageURLtoDomain(String pageURL) {
        URL aURL = null;
        try {
            aURL = new URL(pageURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String host = aURL.getHost();

        if(host.startsWith("www.")) {
            host = host.substring("www.".length());
        }

        return host;
    }
}
