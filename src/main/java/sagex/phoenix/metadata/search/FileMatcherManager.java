package sagex.phoenix.metadata.search;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.XmlUtil;

public class FileMatcherManager extends SystemConfigurationFileManager implements
        SystemConfigurationFileManager.ConfigurationFileVisitor {
    public static class FileMatcherXmlBuilder extends BaseBuilder {
        private File sourceFile;

        public FileMatcherXmlBuilder(File sourceFile) {
            super(sourceFile.getAbsolutePath());
            this.sourceFile = sourceFile;
        }

        private List<FileMatcher> matchers = new ArrayList<FileMatcher>();
        private FileMatcher curMatcher = null;

        public List<FileMatcher> getMatchers() {
            return matchers;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if ("match".equals(name)) {
                curMatcher = new FileMatcher();
                curMatcher.setSourceFile(sourceFile);
            } else if ("regex".equals(name)) {
            } else if ("file".equals(name)) {
            } else if ("title".equals(name)) {
            } else if ("year".equals(name)) {
            } else if ("metadata".equals(name)) {
                curMatcher.setMetadata(new ID(XmlUtil.attr(attributes, "name", null), null));
                curMatcher.setMediaType(MediaType.toMediaType(XmlUtil.attr(attributes, "type", "movie")));
            } else if ("fanart".equals(name)) {
                curMatcher.setFanart(new ID(XmlUtil.attr(attributes, "name", null), null));
            } else if ("titles".equals(name)) {
            } else {
                warning("Unhandled element: " + name + " in file matcher xml");
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if ("match".equals(name)) {
                matchers.add(curMatcher);
                curMatcher = null;
            } else if ("regex".equals(name)) {
                curMatcher.setFileRegex(getData());
            } else if ("file".equals(name)) {
                curMatcher.setFile(new File(getData()));
            } else if ("title".equals(name)) {
                curMatcher.setTitle(getData());
            } else if ("year".equals(name)) {
                curMatcher.setYear(getData());
            } else if ("metadata".equals(name)) {
                curMatcher.getMetadata().setValue(getData());
            } else if ("fanart".equals(name)) {
                curMatcher.getFanart().setValue(getData());
            }
        }
    }

    private List<FileMatcher> buildMatchers(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxFactory.newSAXParser();

        FileMatcherXmlBuilder handler = new FileMatcherXmlBuilder(xmlFile);
        parser.parse(xmlFile, handler);

        return handler.getMatchers();
    }

    /**
     * Returns a new builder that can be used to build {@link FileMatcher}
     * objects from Xml.
     *
     * @param sourceFile
     * @return
     */
    public FileMatcherXmlBuilder createBuilder(File sourceFile) {
        return new FileMatcherXmlBuilder(sourceFile);
    }

    private Logger log = Logger.getLogger(FileMatcherManager.class);

    private List<FileMatcher> matchers = new ArrayList<FileMatcher>();

    public FileMatcherManager(File systemDir, File userDir) {
        super(systemDir, userDir, new SuffixFileFilter("MediaTitles.xml", IOCase.INSENSITIVE));
    }

    public List<FileMatcher> getFileMatchers() {
        return matchers;
    }

    public void addMatcher(FileMatcher matcher) {
        if (matcher.getSourceFile() == null) {
            matcher.setSourceFile(getDefaultMediaTitlesFile());
        }

        if (matcher.getId() == null) {
            String id = null;
            if (matcher.getFileRegex() != null) {
                id = DigestUtils.md5Hex(matcher.getFileRegex().pattern());
            } else if (matcher.getFile() != null) {
                id = DigestUtils.md5Hex(matcher.getFile().getAbsolutePath());
            }
            if (id == null) {
                log.warn("Will not add the filematcher since it has no id");
                return;
            }
            matcher.setId(id);
        }

        FileMatcher fm = getMatcherForId(matcher.getId());
        if (fm != null) {
            log.info("Replacing file matcher: " + matcher);
            getFileMatchers().remove(matcher);
        }

        getFileMatchers().add(matcher);
    }

    public FileMatcher getMatcherForId(String id) {
        if (id == null)
            return null;
        for (FileMatcher f : getFileMatchers()) {
            if (id.equals(f.getId())) {
                return f;
            }
        }
        return null;
    }

    /**
     * For the given filePath, attempt to find a match that will identify the
     * metadata for the given item.
     *
     * @param filePath
     * @return
     */
    public FileMatcher getMatcher(String filePath) {
        if (filePath == null)
            return null;
        for (FileMatcher m : getFileMatchers()) {
            if (m.getFile() != null) {
                File f = new File(filePath);
                if (f.equals(m.getFile())) {
                    return m;
                }
            }

            if (m.getFileRegex() != null) {
                Matcher match = m.getFileRegex().matcher(filePath);
                if (match.find()) {
                    return m;
                }
            }
        }

        log.debug("No File Matchers for: " + filePath);
        return null;
    }

    @Override
    public void visitConfigurationFile(ConfigurationType type, File file) {
        log.info("Loading " + type + " media titles from " + file);
        try {
            List<FileMatcher> m = buildMatchers(file);
            if (m != null) {
                matchers.addAll(m);
            }
        } catch (Exception e) {
            log.warn("Failed to load matchers xml file: " + file, e);
        }
    }

    @Override
    public void loadConfigurations() {
        log.info("Begin loading Title Matchers");
        matchers.clear();
        accept(this);
        log.info("End loading Title Matchers");
    }

    public void addRegexMatcher(FileMatcher matcher) throws Exception {
        int pos = -1;
        for (int i = 0; i < getFileMatchers().size(); i++) {
            FileMatcher fm = getFileMatchers().get(i);
            if (fm.getFileRegex() != null && fm.getFileRegex().pattern().equals(matcher.getFileRegex().pattern())) {
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            getFileMatchers().remove(pos);
        }
        addMatcher(matcher);
    }

    public File getDefaultMediaTitlesFile() {
        return new File(getUserFiles().getDir(), "MediaTitles.xml");
    }

    public synchronized int saveMatchers() throws IOException {
        File outfile = null;
        if (getUserFiles().getFiles() != null) {
            for (File f : getUserFiles().getFiles()) {
                if (f.getName().equals("MediaTitles.xml")) {
                    outfile = f;
                }
            }
        }

        if (outfile == null) {
            outfile = getDefaultMediaTitlesFile();
        }

        if (!outfile.getParentFile().exists()) {
            FileUtils.mkdirsQuietly(outfile.getParentFile());
        }
        List<FileMatcher> matchers = getMatchersForSource(outfile);
        log.info("Saving " + matchers.size() + " file matchers to " + outfile);
        FileOutputStream fos = new FileOutputStream(outfile);
        XmlFileMatcherSerializer ser = new XmlFileMatcherSerializer();
        ser.serialize(matchers, fos);
        fos.flush();
        fos.close();
        return matchers.size();
    }

    private List<FileMatcher> getMatchersForSource(File source) {
        List<FileMatcher> matchers = new ArrayList<FileMatcher>();
        for (FileMatcher fm : getFileMatchers()) {
            if (source.equals(fm.getSourceFile())) {
                matchers.add(fm);
            }
        }
        return matchers;
    }
}
