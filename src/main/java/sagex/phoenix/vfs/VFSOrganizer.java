package sagex.phoenix.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sagex.phoenix.util.Loggers;

/**
 * Loads one or more VFS files and organizes the filters, sorter, views, etc
 * into a single document, that is cached.
 * 
 * @author sls
 */
public class VFSOrganizer {
	public static final Logger log = Logger.getLogger(VFSOrganizer.class);

	protected Map<String, Element> tags = new LinkedHashMap<String, Element>();
	protected Map<String, Element> filters = new LinkedHashMap<String, Element>();
	protected Map<String, Element> filterGroups = new LinkedHashMap<String, Element>();
	protected Map<String, Element> sorts = new LinkedHashMap<String, Element>();
	protected Map<String, Element> groups = new LinkedHashMap<String, Element>();
	protected Map<String, Element> sources = new LinkedHashMap<String, Element>();
	protected Map<String, Element> views = new LinkedHashMap<String, Element>();

	private File dtdDir = null;
	private String name = null;
	
	private ArrayList<String> composedOfFiles = new ArrayList<String>();
	
	public VFSOrganizer(File dtdDir) {
		this.dtdDir = dtdDir;
	}

	public void organize(Reader in, String name) throws Exception {
		this.composedOfFiles.add(name);
		this.name = name;
		
		log.info("Organizing VFS " + name);

		SAXReader xmlReader = newSAXReader();
		
		Document doc = xmlReader.read(in);
		organizeNode(doc, "tags", "tag", "value", tags);
		organizeNode(doc, "filters", "item", "name", filters);
		organizeNode(doc, "filters", "item-group", "name", filterGroups);
		organizeNode(doc, "groups", "item", "name", groups);
		organizeNode(doc, "sorts", "item", "name", sorts);
		organizeNode(doc, "sources", "item", "name", sources);
		organizeNode(doc, "views", "view", "name", views);
	}

	private SAXReader newSAXReader() {
		SAXReader xmlReader = new SAXReader(true);

		final File dtd = new File(dtdDir, "vfs.dtd");
		EntityResolver er = null;
		if (!dtd.exists()) {
			Loggers.LOG.warn("VFS: Missing DTD File: " + dtd.getAbsolutePath() + " for " + name);
		} else {
			er = new EntityResolver() {
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					return new InputSource(new FileInputStream(dtd));
				}
			};
		}

		if (er != null) {
			try {
				xmlReader.setFeature("http://xml.org/sax/features/validation", true);
			} catch (SAXException e) {
				e.printStackTrace();
			}
			xmlReader.setEntityResolver(er);
		}
		
		return xmlReader;
	}

	private void organizeNode(Document doc, final String parentName, final String childElName, final String idAttr,
			final Map<String, Element> addTo) {
		Element e = doc.getRootElement().element(parentName);
		if (e != null) {
			e.accept(new VisitorSupport() {
				@Override
				public void visit(Element node) {
					if (childElName.equals(node.getName())) {
						String id = node.attributeValue(idAttr);
						if (id == null) {
							log.warn("No " + idAttr + " attribute for Node " + node + " in " + name);
							return;
						}

						if (addTo.put(id, node) != null) {
							log.info("Replaced VFS Entry: " + childElName + "[" + id + "] from " + name);
						}
					}
				}
			});
		}
	}

	public void writeTo(Writer out) throws IOException {
		DocumentFactory df = DocumentFactory.getInstance();
		Document d = df.createDocument();
		d.setDocType(df.createDocType("vfs", null, "vfs.dtd"));
		
		Element vfs = df.createElement("vfs");
		d.setRootElement(vfs);

		vfs.addComment("GENERATED FILE - DO NO EDIT");
		vfs.addComment("LAST UPDATED - " + new Date(System.currentTimeMillis()));

		if (composedOfFiles.size() >0) {
			vfs.addComment("THIS FILE CONSISTS OF THE FOLLOWING VFS FILES");
			for (String s : composedOfFiles) {
				vfs.addComment(s);
			}
		}
		
		addNodes(tags, "tags", vfs, df);
		addNodes(sources, "sources", vfs, df);
		addNodes(filters, "filters", vfs, df);
		addNodes(filterGroups, "filters", vfs, df);
		addNodes(sorts, "sorts", vfs, df);
		addNodes(groups, "groups", vfs, df);
		addNodes(views, "views", vfs, df);

		try {
			// we need to validate the document before we can write
			StringWriter sw = new StringWriter();
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setEncoding("UTF-8");
			XMLWriter xw = new XMLWriter(sw, outformat);
			xw.write(d);
			xw.flush();
			newSAXReader().read(new StringReader(sw.getBuffer().toString()));
		} catch (DocumentException e) {
			throw new IOException("New Document does not validate!", e);
		}

		// document is clean, now write it
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding("UTF-8");
		XMLWriter w = new XMLWriter(out, outformat);
		w.write(d);
		w.flush();
	}

	private void addNodes(Map<String, Element> tags, String nodeName, Element parent, DocumentFactory df) {
		if (tags.size() == 0)
			return;

		Element child = parent.element(nodeName);
		if (child == null) {
			child = df.createElement(nodeName);
			parent.add(child);
		}

		for (Element e : tags.values()) {
			e.setParent(null);
			child.add(e);
		}
	}

	public void organize(File file) throws Exception {
		FileReader r = new FileReader(file);
		try {
			organize(r, file.getAbsolutePath());
		} finally {
			IOUtils.closeQuietly(r);
		}
	}
}
