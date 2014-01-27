package sagex.phoenix.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import sagex.phoenix.util.var.SageExpressionVariable;
import sagex.phoenix.util.var.Variable;


public class XmlUtil {
    private static final Pattern exprPattern = Pattern.compile("\\$\\{([^}]+)}");

    public static Variable<String> strVar(String s) {
        if (s==null) return null;
        Matcher m = exprPattern.matcher(s);
        if (m.find()) {
            return new SageExpressionVariable<String>(m.group(1), null, String.class);
        } else {
            return new Variable<String>(s, String.class);
        }
    }

    public static Variable<Integer> intVar(Attributes attributes, String name, int defVal) {
        String s = attr(attributes, name, null);
        if (s==null) return null;
        Matcher m = exprPattern.matcher(s);
        if (m.find()) {
            return new SageExpressionVariable<Integer>(m.group(1), defVal, Integer.class);
        } else {
            return new Variable<Integer>(Integer.parseInt(s), Integer.class);
        }
    }

    
    public static Variable<Boolean> boolVar(Attributes attributes, String name, boolean defVal) {
        String s = attr(attributes, name, null);
        if (s==null) return null;
        Matcher m = exprPattern.matcher(s);
        if (m.find()) {
            return new SageExpressionVariable<Boolean>(m.group(1), defVal, Boolean.class);
        } else {
            return new Variable<Boolean>(BooleanUtils.toBoolean(s), Boolean.class);
        }
    }
    
    public static Variable<String> strVar(Attributes attributes, String name) {
        String s = attr(attributes, name, null);
        if (s==null) return null;
        Matcher m = exprPattern.matcher(s);
        if (m.find()) {
            return new SageExpressionVariable<String>(m.group(1),null, String.class);
        } else {
            return new Variable<String>(s, String.class);
        }
    }

    public static String attr(Attributes attributes, String name, String defValue) {
        String s = attributes.getValue(name);
        if (StringUtils.isEmpty(s)) return defValue;
        return s;
    }

    public static String attr(Attributes attributes, String name) {
        String s = attributes.getValue(name);
        return s;
    }
    
    public static boolean bool(Attributes attributes, String name, boolean defValue) {
        return BooleanUtils.toBoolean(attr(attributes, name, String.valueOf(defValue)));
    }
    
    public static void parseXml(String url, DefaultHandler handler) throws SAXException, MalformedURLException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        try {
            reader.setFeature("http://xml.org/sax/features/validation", false);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);
        InputStream is = new URL(url).openStream();
        try {
        	reader.parse(new InputSource(is));
        } catch (Exception e) {
        	IOUtils.closeQuietly(is);
        }
    }
}
