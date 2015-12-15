package sagex.phoenix.util.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.log4j.Logger;

public class Url implements IUrl {
    private Logger log = Logger.getLogger(Url.class);

    private String url = null;
    private String movedUrl = null;

    public Url(String url) {
        this.url = url;
    }

    public URL getMovedUrl() throws IOException {
        return new URL(movedUrl);
    }

    public URL getUrl() throws IOException {
        return new URL(url);
    }

    public boolean hasMoved() {
        return movedUrl != null;
    }

    protected void sendCookies(URL url, URLConnection conn, ICookieHandler handler) {
        if (handler != null) {
            Map<String, String> cookies = handler.getCookiesToSend(url.toExternalForm());
            if (cookies != null) {
                for (Map.Entry<String, String> e : cookies.entrySet()) {
                    conn.setRequestProperty("Cookie", String.format("%s=%s", e.getKey(), e.getValue()));
                }
            }
        }
    }

    public InputStream getInputStream(ICookieHandler handler, boolean followRedirects) throws IOException {
        URL u = getUrl();

        URLConnection conn = UrlUtil.openUrlConnection(u, followRedirects);
        sendCookies(u, conn, handler);

        // get the stream
        InputStream is = conn.getInputStream();
        if (conn instanceof HttpURLConnection) {
            int rc = ((HttpURLConnection) conn).getResponseCode();
            if (rc == HttpURLConnection.HTTP_MOVED_PERM || rc == HttpURLConnection.HTTP_MOVED_TEMP) {
                movedUrl = conn.getHeaderField("Location");
                if (movedUrl != null) {
                    int p = movedUrl.indexOf('?');
                    if (p != -1) {
                        movedUrl = movedUrl.substring(0, p);
                    }
                    log.debug("Found a Moved Url: " + u.toExternalForm() + "; Moved: " + movedUrl);
                }
            }
        }

        handleCookies(u, conn, handler);
        return is;
    }

    protected void handleCookies(URL u, URLConnection conn, ICookieHandler handler) {
        if (handler != null) {
            // process the response cookies
            String headerName = null;
            for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
                if (headerName.equals("Set-Cookie")) {
                    String cookie = conn.getHeaderField(i);
                    handler.handleSetCookie(u.toExternalForm(), cookie);
                }
            }
        }
    }

}
