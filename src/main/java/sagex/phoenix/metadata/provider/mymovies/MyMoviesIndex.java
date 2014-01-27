package sagex.phoenix.metadata.provider.mymovies;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Element;

import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.StringUtils;

public class MyMoviesIndex implements MyMoviesNodeVisitor {
    private Logger log      = Logger.getLogger(this.getClass());
    private static MyMoviesIndex indexer  = new MyMoviesIndex();

    private IndexReader            reader   = null;
    private IndexWriter            writer   = null;
    private File                   indexDir = null;

    private Searcher               searcher = null;
    private Analyzer               analyzer = new StandardAnalyzer(Version.LUCENE_30);
    private QueryParser            parser   = new QueryParser(Version.LUCENE_30, "title", analyzer);

    public static MyMoviesIndex getInstance() {
        return indexer;
    }

    public boolean isNew() {
        File ch[] = getIndexDir().listFiles();
        return ch == null || ch.length == 0;
    }

    public void beginIndexing() throws Exception {
        writer = new IndexWriter(new SimpleFSDirectory(getIndexDir()), analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
    }

    public void endIndexing() throws Exception {
        writer.optimize();
        writer.close();

        // open the index, for searching
        openIndex();
    }

    public void openIndex() throws Exception {
        indexDir = getIndexDir();
        if (!indexDir.exists()) {
            log.debug("Creating Lucene Index Dir: " + indexDir.getAbsolutePath());
        }

        log.debug("Opening Lucene Index: " + indexDir.getAbsolutePath());

        reader = IndexReader.open(new SimpleFSDirectory(indexDir));
        searcher = new IndexSearcher(reader);
    }

    public void addMovie(String name, String date, String id, String imdb) throws Exception {
        log.debug("Indexing Movie: " + name + "; date: " + date + "; id: " + id);
        Document doc = createDocument(name, date, id, imdb);
        writer.addDocument(doc);
    }

    private File getIndexDir() {
        return indexDir;
    }

    public static Document createDocument(String name, String date, String id, String imdb) {
        // make a new, empty document
        Document doc = new Document();

        // ISSUE: 24 - remove html tags from the titles
        name=StringUtils.removeHtml(name);
        
        // index titles
        doc.add(new Field("title", name, Field.Store.YES, Field.Index.ANALYZED));

        // Store release date but not index
        doc.add(new Field("year", String.valueOf(DateUtils.parseYear(date, "MM/dd/yyyy")), Field.Store.YES, Field.Index.NO));
        doc.add(new Field("id", id, Field.Store.YES, Field.Index.NO));
        doc.add(new Field("imdb", imdb, Field.Store.YES, Field.Index.NO));

        // return the document
        return doc;
    }

    public List<IMetadataSearchResult> searchTitle(String title) throws Exception {
        if (searcher == null) openIndex();

        Query query = parser.parse(title);
        TopDocs hits = searcher.search(query, 10);

        int l = hits.totalHits;
        List<IMetadataSearchResult> results = new ArrayList<IMetadataSearchResult>(l);

        for (int i = 0; i < l; i++) {
            ScoreDoc sd = hits.scoreDocs[i];
            Document d = searcher.doc(sd.doc);
            String name = d.get("title");
            int year = NumberUtils.toInt(d.get("year"));
            String id = d.get("id");
            String imdb = d.get("imdb");
            
            
            float score = MetadataSearchUtil.calculateScore(title, name);
            MediaSearchResult res = new MediaSearchResult(MediaType.MOVIE, MyMoviesMetadataProvider.PROVIDER_ID, id, name, year, score);
            res.setIMDBId(imdb);
            // only support movies
            res.setMediaType(MediaType.MOVIE);
            res.setUrl(id);
            results.add(res);
        }

        return results;
    }

    public void clean() {
        if (isNew()) return;

        log.debug("Deleting All Currently indexed documents.");
        try {
            openIndex();
            int s = reader.numDocs();
            for (int i = 0; i < s; i++) {
                reader.deleteDocument(i);
            }
        } catch (Exception e) {
            log.error("Failed to delete index documents: Consider manually removing the directory: " + indexDir.getAbsolutePath());
        }
        log.debug("Finished Deleting documents.");
    }

    public void setIndexDir(String indexDir2) {
        this.indexDir = new File(indexDir2);
        if (!indexDir.exists()) {
            log.debug("Creating Lucene Index Dir: " + indexDir.getAbsolutePath());
        }
    }

    public void visitMovie(Element el) {
        String id = MyMoviesXmlFile.getElementValue(el, "ID");
        String title = MyMoviesXmlFile.getElementValue(el, "LocalTitle");
        String year = MyMoviesXmlFile.getElementValue(el, "ReleaseDate");
        String imdb = MyMoviesXmlFile.getElementValue(el, "IMDB");
        
        try {
            addMovie(title, year, id, imdb);
        } catch (Exception e) {
            log.warn("Can't index MyMovie node: " + el.getTextContent(), e);
        }
    }
}
