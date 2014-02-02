package sagex.phoenix.metadata.provider.dvdprofiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.dom4j.Element;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.DateUtils;

public class LocalMovieIndex implements IDVDProfilerMovieNodeVisitor {
	private static final Logger log = Logger.getLogger(LocalMovieIndex.class);
	private static LocalMovieIndex indexer = new LocalMovieIndex();

	private IndexReader reader = null;
	private IndexWriter writer = null;
	private File indexDir = null;

	private Searcher searcher = null;
	private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
	private QueryParser parser = new QueryParser(Version.LUCENE_30, "title", analyzer);

	public MetadataConfiguration cfg = GroupProxy.get(MetadataConfiguration.class);

	public static LocalMovieIndex getInstance() {
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

	public void addMovie(String title, String altTitle, String date, String id) throws Exception {
		log.debug("Indexing Movie: " + title + "; altTitle: " + altTitle + "; date: " + date + "; id: " + id);
		Document doc = createDocument(title, altTitle, date, id);
		writer.addDocument(doc);
	}

	private File getIndexDir() {
		return indexDir;
	}

	public static Document createDocument(String title, String altTitle, String date, String id) {
		// make a new, empty document
		Document doc = new Document();

		// ISSUE: 24 - remove html tags from the titles
		title = sagex.phoenix.util.StringUtils.removeHtml(title);

		// index titles
		doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));

		// store the alt title, if there is one.
		if (!StringUtils.isEmpty(altTitle)) {
			altTitle = sagex.phoenix.util.StringUtils.removeHtml(altTitle);
			doc.add(new Field("alttitle", altTitle, Field.Store.YES, Field.Index.ANALYZED));
		}

		// Store release date but not index
		doc.add(new Field("year", String.valueOf(DateUtils.parseYear(date)), Field.Store.YES, Field.Index.NO));
		doc.add(new Field("id", id, Field.Store.YES, Field.Index.NO));

		// return the document
		return doc;
	}

	public List<IMetadataSearchResult> searchTitle(String title, String providerId) throws Exception {
		if (searcher == null)
			openIndex();

		Query query = null;

		if (cfg.isScoreAlternateTitles()) {
			query = parser.parse(title + " OR alttitle:" + title);
		} else {
			query = parser.parse(title);
		}

		TopDocs hits = searcher.search(query, 10);

		int l = hits.totalHits;
		List<IMetadataSearchResult> results = new ArrayList<IMetadataSearchResult>(l);

		for (int i = 0; i < l; i++) {
			ScoreDoc sd = hits.scoreDocs[i];
			Document d = searcher.doc(sd.doc);
			String name = d.get("title");
			String altTitle = d.get("alttitle");
			int year = NumberUtils.toInt(d.get("year"));
			String id = d.get("id");

			float score1 = MetadataSearchUtil.calculateScore(title, name);
			float score2 = 0.0f;
			if (cfg.isScoreAlternateTitles() && !StringUtils.isEmpty(altTitle)) {
				score2 = MetadataSearchUtil.calculateScore(title, altTitle);
			}

			if (!StringUtils.isEmpty(altTitle)) {
				name = name + "(aka " + altTitle + ")";
			}
			MediaSearchResult res = new MediaSearchResult(MediaType.MOVIE, providerId, id, name, year, Math.max(score1, score2));
			// we only support movies
			res.setMediaType(MediaType.MOVIE);
			res.setUrl(id);
			results.add(res);
		}

		return results;
	}

	public void clean() {
		if (isNew())
			return;

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
		String id = DVDProfilerXmlFile.getElementValue(el, "ID");
		String title = DVDProfilerXmlFile.getElementValue(el, "Title");
		String altTitle = DVDProfilerXmlFile.getElementValue(el, "OriginalTitle");
		String year = DVDProfilerXmlFile.getElementValue(el, "Released");
		try {
			addMovie(title, altTitle, year, id);
		} catch (Exception e) {
			log.error("Can't index movie node: " + el.getText(), e);
		}
	}
}
