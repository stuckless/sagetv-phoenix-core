package sagex.phoenix.metadata.search;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class XmlFileMatcherSerializer {

    public void serialize(List<FileMatcher> matchers, OutputStream os) throws IOException {
        Document doc = DocumentHelper.createDocument();
        doc.setDocType(DocumentFactory.getInstance().createDocType("titles", null, null));
        Element titles = doc.addElement("titles");

        for (FileMatcher fm : matchers) {
            serializeMatcher(fm, titles);
        }

        XMLWriter writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
        writer.write(doc);
        os.flush();
    }

    public void serializeMatcher(FileMatcher fm, Element parent) {
        /*
		 * <match> <regex>[\\/]Babylon\s*5[\\/]</regex> <title>Babylon 5</title>
		 * <year>1993</year> <metadata type="tv" name="tvdb">7072</metadata>
		 * </match>
		 */
        Element el = parent.addElement("match");
        if (fm.getFile() != null) {
            el.addElement("file").setText(fm.getFile().getAbsolutePath());
        }
        if (fm.getFileRegex() != null) {
            el.addElement("regex").setText(fm.getFileRegex().pattern());
        }
        if (!StringUtils.isEmpty(fm.getTitle())) {
            el.addElement("title").setText(fm.getTitle());
        }
        if (!StringUtils.isEmpty(fm.getYear())) {
            el.addElement("year").setText(fm.getYear());
        }

        if (fm.getMetadata() != null && fm.getMediaType() != null) {
            el.addElement("metadata").addAttribute("type", fm.getMediaType().sageValue())
                    .addAttribute("name", fm.getMetadata().getName()).setText(fm.getMetadata().getValue());
        }

        if (fm.getFanart() != null && fm.getMediaType() != null) {
            el.addElement("fanart").addAttribute("type", fm.getMediaType().sageValue())
                    .addAttribute("name", fm.getFanart().getName()).setText(fm.getFanart().getValue());
        }
    }
}
