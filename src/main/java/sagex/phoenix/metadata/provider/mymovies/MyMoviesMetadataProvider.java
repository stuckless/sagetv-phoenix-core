package sagex.phoenix.metadata.provider.mymovies;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProviderInfo;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataProvider;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;

public class MyMoviesMetadataProvider extends MetadataProvider {
	private Logger log = Logger.getLogger(this.getClass());

	public static final String PROVIDER_ID = "mymovies";

	private File xmlFile = null;
	private MyMoviesXmlFile xmlFileTool;

	private boolean initialized = false;

	private MyMoviesConfiguration cfg = new MyMoviesConfiguration();

	public MyMoviesMetadataProvider(IMetadataProviderInfo info) {
		super(info);
	}

	public synchronized List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
		if (!initialized)
			initialize();

		if (shouldRebuildIndexes()) {
			try {
				rebuildIndexes();
			} catch (Exception e) {
				log.error("Failed to rebuild the indexes for MyMovies!", e);
			}
		}

		// search by ID, if the ID is present
		if (!StringUtils.isEmpty(query.get(SearchQuery.Field.ID))) {
			List<IMetadataSearchResult> res = MetadataSearchUtil.searchById(this, query, query.get(SearchQuery.Field.ID));
			if (res != null) {
				return res;
			}
		}

		// carry on normal search
		String arg = query.get(SearchQuery.Field.QUERY);
		try {
			return MyMoviesIndex.getInstance().searchTitle(arg);
		} catch (Exception e) {
			throw new MetadataException("MyMovies seaarch failed for: " + query, e);
		}
	}

	private void initialize() throws MetadataException {
		String indexDir = new File(Phoenix.getInstance().getUserCacheDir(), "mymovies").getAbsolutePath();
		MyMoviesIndex.getInstance().setIndexDir(indexDir);

		String xml = cfg.getXmlFile();
		if (xml == null) {
			throw new MetadataException("Missing xml.  Please Set MyMovies Xml Location.");
		}

		xmlFile = new File(xml);
		if (!xmlFile.exists()) {
			throw new MetadataException("Missing Xml File: " + xmlFile);
		}

		log.info("MyMovies initialized using xml file: " + xmlFile);

		xmlFileTool = new MyMoviesXmlFile(xmlFile);
		initialized = true;
	}

	private boolean isXmlModified() {
		return xmlFile.lastModified() > cfg.getXmlFileLastModified();
	}

	private void rebuildIndexes() throws Exception {
		log.debug("Rebuilding MyMovies Indexes....");

		MyMoviesIndex.getInstance().clean();
		MyMoviesIndex.getInstance().beginIndexing();
		xmlFileTool.visitMovies(MyMoviesIndex.getInstance());
		MyMoviesIndex.getInstance().endIndexing();

		cfg.setXmlFileLastModified(xmlFile.lastModified());
		Phoenix.getInstance().getConfigurationManager().save();
	}

	private boolean shouldRebuildIndexes() {
		return MyMoviesIndex.getInstance().isNew() || isXmlModified();
	}

	public synchronized IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
		if (!initialized)
			initialize();

		if (MetadataSearchUtil.hasMetadata(result))
			return MetadataSearchUtil.getMetadata(result);

		return new MyMoviesParser(result.getId(), this).getMetaData();
	}

	public MyMoviesXmlFile getMyMoviesXmlFile() {
		return xmlFileTool;
	}
}
