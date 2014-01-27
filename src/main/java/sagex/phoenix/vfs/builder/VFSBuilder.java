package sagex.phoenix.vfs.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.VFSManager;

public class VFSBuilder extends VFSManagerBuilder {
    private DefaultHandler              defaultHandler = null;

    private Map<String, DefaultHandler> handlers       = new HashMap<String, DefaultHandler>();

    public VFSBuilder(VFSManager mgr) {
        super(mgr);
        handlers.put("tags", new TagsBuilder(mgr));
        handlers.put("sources", new SourcesBuilder(mgr));
        handlers.put("filters", new FiltersBuilder(mgr));
        handlers.put("sorts", new SortersBuilder(mgr));
        handlers.put("groups", new GroupsBuilder(mgr));
        handlers.put("views", new ViewsBuilder(mgr));
    }

    /**
     * Register vfs elements with the vfs manager. It will validate that the xml
     * is valid
     * 
     * @param vfsXML
     * @param vfsDir
     *            - primary vfs dir that contains the vfs.dtd
     * @param manager
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void registerVFSSources(File vfsXML, File vfsDir, VFSManager manager) throws ParserConfigurationException, SAXException, IOException {
        registerVFSSources(new FileInputStream(vfsXML), vfsDir, manager);
    }

    public static void registerVFSSources(String vfsXML, File vfsDir, VFSManager manager) throws ParserConfigurationException, SAXException, IOException {
    	registerVFSSources(new ByteArrayInputStream(vfsXML.getBytes()), vfsDir, manager);
    }

    public static void registerVFSSources(InputStream xmlStream, File vfsDir, VFSManager manager) throws ParserConfigurationException, SAXException, IOException {
        final File dtd = new File(vfsDir, "vfs.dtd");
        EntityResolver er = null;
        if (!dtd.exists()) {
        	Loggers.LOG.warn("VFS: Missing DTD File: " + dtd.getAbsolutePath());
        } else {
	        er = new EntityResolver() {
	            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
	                return new InputSource(new FileInputStream(dtd));
	            }
	        };
        }

        VFSBuilder builder = new VFSBuilder(manager);
        XMLReader reader = XMLReaderFactory.createXMLReader();
        if (er!=null) {
	        try {
	            reader.setFeature("http://xml.org/sax/features/validation", true);
	        } catch (SAXException e) {
	            e.printStackTrace();
	        }
	        reader.setEntityResolver(er);
        }
        reader.setContentHandler(builder);
        reader.setErrorHandler(builder);
        reader.parse(new InputSource(xmlStream));
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (defaultHandler != null) {
            defaultHandler.characters(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (handlers.get(name) != null) {
            defaultHandler = null;
        }

        if (defaultHandler != null) {
            defaultHandler.endElement(uri, localName, name);
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (handlers.get(name) != null) {
            defaultHandler = handlers.get(name);
        }

        if (defaultHandler != null) {
            defaultHandler.startElement(uri, localName, name, attributes);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sagex.phoenix.vfs.factory.BaseBuilder#setDocumentLocator(org.xml.sax.
     * Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);

        for (DefaultHandler h : handlers.values()) {
            h.setDocumentLocator(locator);
        }
    }

}
