package sagex.phoenix.util;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import sagex.phoenix.util.url.CachedUrl;
import sagex.phoenix.util.url.IUrl;

public class DOMUtils {
    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    public static String getElementValue(Element el, String tag) {
        if (el == null || tag == null)
            return null;
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return StringUtils.trim(n.getTextContent());
        }
        return null;
    }

    public static int getElementIntValue(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return NumberUtils.toInt(StringUtils.trim(n.getTextContent()));
        }
        return 0;
    }

    public static int getElementIntValue(Element el, String tag, int defValue) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return NumberUtils.toInt(StringUtils.trim(n.getTextContent()), defValue);
        }
        return defValue;
    }

    public static String getMaxElementValue(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        String retVal = null;
        for (int i = 0; i < nl.getLength(); i++) {
            String s = nl.item(i).getTextContent();
            if (retVal == null) {
                retVal = s;
            } else {
                if (s != null && s.length() > retVal.length()) {
                    retVal = s;
                }
            }
        }
        return retVal;
    }

    public static Element getElementByTagName(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            return (Element) nl.item(0);
        }
        return null;
    }

    /**
     * Returns a parsed document for the url. if the IUrl instance is a
     * CachedUrl, then and there is a parsing error, then the the cached url
     * will be removed, and it will be tried again.
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static Document parseDocument(IUrl url) throws Exception {
        DocumentBuilder parser = factory.newDocumentBuilder();
        try {
            return parser.parse(url.getInputStream(null, true));
        } catch (Exception e) {
            try {
                // if it's a socket timeout... don't try to re-read...
                if (! (e instanceof SocketTimeoutException)) {
                    // let try a gzip input stream
                    Loggers.LOG.warn("Failed to parse url " + url + " because of " + e.getMessage()
                            + "; will try to use a gzip decoder on it", e);
                    return parser.parse(new GZIPInputStream(url.getInputStream(null, true)));
                }
            } catch (Exception ex) {
                Loggers.LOG.warn("GZIP parse also failed", ex);
            }

            if (url instanceof CachedUrl) {
                CachedUrl.remove((CachedUrl) url);
                Loggers.LOG.warn("Failed to parse cached url " + url + ", removing cached file.", e);
            }

            throw e;
        }
    }

    public static void parseXml(IUrl url, DefaultHandler handler) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        try {
            reader.setFeature("http://xml.org/sax/features/validation", false);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);
        reader.parse(new InputSource(url.getInputStream(null, true)));
    }
}
