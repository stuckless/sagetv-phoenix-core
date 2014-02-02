package sagex.phoenix.metadata.provider.dvdprofiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import sagex.phoenix.metadata.MetadataException;

public class DVDProfilerXmlFile {
	private Logger log = Logger.getLogger(DVDProfilerXmlFile.class);

	private File file = null;

	public DVDProfilerXmlFile(File xmlFile) {
		this.file = xmlFile;
	}

	public void visitMovies(final IDVDProfilerMovieNodeVisitor visitor) {
		SAXReader reader = new SAXReader();
		reader.addHandler("/Collection/DVD", new ElementHandler() {
			@Override
			public void onStart(ElementPath path) {
			}

			@Override
			public void onEnd(ElementPath path) {
				Element e = path.getCurrent();
				visitor.visitMovie(e);
				e.detach();
			}
		});
		try {
			reader.read(file);
		} catch (DocumentException e) {
			log.error("Failed to parse dvdprofiler file: " + file, e);
			throw new RuntimeException(e);
		}
	}

	public static String getElementValue(Element el, String tag) {
		String s = el.elementText(tag);
		if (s != null)
			s = s.trim();
		return s;
	}

	public Element findMovieById(final String id) throws MetadataException {
		if (id == null)
			throw new MetadataException("Movie id was null!");
		final List<Element> ids = new ArrayList<Element>();

		SAXReader reader = new SAXReader();
		reader.addHandler("/Collection/DVD", new ElementHandler() {
			@Override
			public void onStart(ElementPath path) {
			}

			@Override
			public void onEnd(ElementPath path) {
				Element e = path.getCurrent();
				if (id.equals(getElementValue(e, "ID"))) {
					ids.add(e);
				}
				e.detach();
			}
		});
		try {
			reader.read(file);
		} catch (DocumentException e) {
			throw new MetadataException("Failed to parse dvd profiler xml: " + file, e);
		}

		if (ids.size() == 0) {
			throw new MetadataException("DVD Profiler: Failed to find movie for id: " + id);
		}
		return ids.get(0);
	}
}
