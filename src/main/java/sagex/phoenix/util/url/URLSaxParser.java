package sagex.phoenix.util.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sagex.phoenix.util.AbstractSaxHandler;

/**
 * Single use parser for a given url. Provides the parsing framework.
 * Subclasses, need to provide the Sax Handling events.
 *
 * @author seans
 */
public class URLSaxParser extends AbstractSaxHandler {
    private static final Logger log = Logger.getLogger(URLSaxParser.class);

    protected String url = null;
    protected boolean followRedirects = false;
    protected boolean isRedirecting = false;
    protected String redirectUrl = null;

    public URLSaxParser(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    /**
     * Given the Url, parse it using the Html Parser, allowing the sub-class to
     * handle all sax events.
     * <p/>
     * if the url is an http url, it will check to see that the connection
     * status is OK before parsing, otherwise, it just passes the url into the
     * parser and allows it parse.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void parse() throws IOException, SAXException {
        parse(null);
    }

    public void parse(CookieHandler cookieHandler) throws IOException, SAXException {
        // org.xml.sax.XMLReader reader =
        // org.xml.sax.helpers.XMLReaderFactory.createXMLReader
        // ("org.htmlparser.sax.XMLReader");
        org.xml.sax.XMLReader reader = new org.htmlparser.sax.XMLReader();
        org.xml.sax.ContentHandler content = this;
        reader.setContentHandler(content);
        org.xml.sax.ErrorHandler errors = this;
        reader.setErrorHandler(errors);

        log.info("Parsing Url: " + url);
        IUrl urlFacade = createUrl(url);
        try {
            InputStream is = urlFacade.getInputStream(cookieHandler, false);
            if (urlFacade.hasMoved()) {
                isRedirecting = true;
                redirectUrl = urlFacade.getMovedUrl().toExternalForm();
            } else {
                reader.parse(new InputSource(is));
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to open connection: " + url, e);
        }
    }

    protected IUrl createUrl(String url) {
        return UrlFactory.newUrl(url);
    }

    public boolean isTag(String tag1, String tag2) {
        return tag1 != null && tag1.equalsIgnoreCase(tag2);
    }

    public String attr(Attributes atts, String aName) {
        String val = atts.getValue(aName);
        return (val == null) ? "" : val;
    }

    public boolean attrContains(Attributes atts, String aName, String aContains) {
        return attr(atts, aName).contains(aContains);
    }

    public boolean isRedirecting() {
        return isRedirecting;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public boolean getFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public String getCharacters(char ch[], int start, int length) {
        String charbuf = null;
        try {
            charbuf = new String(ch, start, length);
            try {
                charbuf = URLDecoder.decode(charbuf, "UTF-8");
            } catch (Exception ex) {
            }
            charbuf = charbuf.trim();

            // just remove html entities
            // charbuf = charbuf.replaceAll("&nbsp;", " ");

            charbuf = StringEscapeUtils.unescapeXml(charbuf);

            // charbuf = charbuf.replaceAll("&nbsp;", " ");

            // charbuf = charbuf.replaceAll("&[#0-9a-zA-Z]+;", "");

        } catch (Throwable e) {
            log.error("There was a problem getting a string from the char array!; buffer as known: " + charbuf, e);
        }

        // really excessive logging
        if (log.isDebugEnabled()) {
            if (charbuf != null && charbuf.length() > 0) {
                log.debug("CharBuffer:[" + charbuf + "]");
            }
        }
        return charbuf;
    }

}
