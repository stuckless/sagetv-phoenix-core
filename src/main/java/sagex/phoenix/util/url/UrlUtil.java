package sagex.phoenix.util.url;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.GroupProxy;

public class UrlUtil {
    private static final Logger log = Logger.getLogger(UrlUtil.class);

    /**
     * Returns the the entire Url Path except the filename, like doing a basedir
     * on a filename.
     *
     * @param url
     * @return
     */
    public static String getBaseUrl(String url) {
        String path = getPathName(url);
        if (path != null && path.contains("/")) {
            path = path.substring(0, path.lastIndexOf("/"));
        }
        return getDomainUrl(url) + path;
    }

    public static String getDomainUrl(String url) {
        URL u;
        try {
            u = new URL(url);
            return String.format("%s://%s/", u.getProtocol(), u.getHost());
        } catch (MalformedURLException e) {
            log.error("Failed to get domain url for: " + url);
        }
        return null;
    }

    public static String joinUrlPath(String baseUrl, String path) {
        StringBuffer sb = new StringBuffer(baseUrl);
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            path = path.substring(1);
        }
        sb.append(path);

        return sb.toString();
    }

    public static String getPathName(String url) {
        URL u;
        try {
            u = new URL(url);
            return u.getPath();
        } catch (MalformedURLException e) {
            log.error("getPathName() Failed! " + url, e);
        }
        return null;
    }

    public static String encode(String data) {
        if (data == null)
            return "";
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to url encode data: " + data + " as UTF-8; will try again using default encoding", e);
            return URLEncoder.encode(data);
        }
    }

    /**
     * Opens a URL Connection, and sets the default URL Timeout, HTTP
     * User-Agent, etc.
     *
     * @param url
     * @return {@link URLConnection}
     * @throws IOException
     * @throws SocketTimeoutException
     */
    public static URLConnection openUrlConnection(URL url, boolean followRedirects) throws IOException, SocketTimeoutException {
        return openUrlConnection(url, null, null, 0, followRedirects);
    }

    /**
     * Opens a URL Connection, and sets the URL Timeout, HTTP User-Agent and
     * Referrer. If the userAgent is null then the system user agent is used. If
     * the timeout is 0, then the system timeout is used.
     *
     * @param url
     * @return {@link URLConnection}
     * @throws IOException
     * @throws SocketTimeoutException
     */
    public static URLConnection openUrlConnection(URL url, String userAgent, String referrer, int timeout, boolean followRedirects)
            throws IOException, SocketTimeoutException {
        UrlConfiguration cfg = GroupProxy.get(UrlConfiguration.class);
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            if (userAgent == null) {
                userAgent = cfg.getHttpUserAgent();
            }
            if (userAgent != null) {
                conn.setRequestProperty("User-Agent", userAgent);
            }

            if (referrer != null) {
                conn.setRequestProperty("REFERER", referrer);
            }
            ((HttpURLConnection) conn).setInstanceFollowRedirects(followRedirects);
        }

        if (timeout <= 0) {
            timeout = cfg.getReadTimeoutMS();
        }

        if (timeout > 0) {
            conn.setReadTimeout(timeout);
            conn.setConnectTimeout(timeout);
        }

        return conn;
    }

    public static String buildURL(String path, Map<String, String> args) {
        if (args == null || args.size() == 0)
            return path;

        StringBuilder sb = new StringBuilder(path);
        for (Map.Entry<String, String> e : args.entrySet()) {
            if (sb.indexOf("?") == -1) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(StringUtils.defaultIfEmpty(e.getKey(), "unamed"));
            sb.append("=");
            sb.append(encode(StringUtils.defaultIfEmpty(e.getValue(), "")));
        }
        return sb.toString();
    }

    public static String decode(String data) {
        if (data == null)
            return "";
        try {
            return URLDecoder.decode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to url encode data: " + data + " as UTF-8; will try again using default encoding", e);
            return URLDecoder.decode(data);
        }
    }

    public static String getContentAsString(IUrl url) throws IOException {
        InputStream is = null;
        try {
            is = url.getInputStream(null, true);
            return IOUtils.toString(is, "UTF-8");
        } finally {
            IOUtils.closeQuietly(is);
        }
    }


}
