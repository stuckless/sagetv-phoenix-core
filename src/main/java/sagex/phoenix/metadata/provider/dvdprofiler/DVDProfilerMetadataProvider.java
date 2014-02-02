package sagex.phoenix.metadata.provider.dvdprofiler;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProviderInfo;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataProvider;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;

public class DVDProfilerMetadataProvider extends MetadataProvider {
	private File xmlFile = null;
	private File imageDir = null;
	private DVDProfilerXmlFile xmlFileTool;

	private boolean initialized = false;

	private DVDProfilerConfiguration cfg = new DVDProfilerConfiguration();

	public DVDProfilerMetadataProvider(IMetadataProviderInfo info) {
		super(info);
	}

	/**
	 * searching is synchronized because of the potential to need to rebuild the
	 * index.
	 */
	public synchronized List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
		if (!initialized)
			initialize();

		if (shouldRebuildIndexes()) {
			try {
				rebuildIndexes();
			} catch (Exception e) {
				throw new MetadataException("Failed to build/rebuild the DVDProfiler index", e);
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
			return LocalMovieIndex.getInstance().searchTitle(arg, getInfo().getId());
		} catch (Exception e) {
			throw new MetadataException("DVD Profiler search failed for " + query, e);
		}
	}

	private void initialize() throws MetadataException {
		log.info("Initializing DVDProfiler index");
		String indexDir = new File(Phoenix.getInstance().getUserCacheDir(), "dvdprofiler").getAbsolutePath();
		LocalMovieIndex.getInstance().setIndexDir(indexDir);

		String xml = cfg.getXmlFile();
		if (xml == null) {
			throw new MetadataException("Missing xml.  Please Set DVDProfiler Xml Location.");
		}

		xmlFile = new File(xml);
		log.info("Loading DVDProfiler collection " + xmlFile);
		if (!xmlFile.exists()) {
			throw new MetadataException("Missing Xml File: " + xmlFile);
		}

		String strImageDir = cfg.getImageDir();
		if (strImageDir == null) {
			log.warn("DVD Profiler Image dir is not set, will use a relative Images path.");
			this.imageDir = new File(xmlFile.getParentFile(), "Images");
		} else {
			this.imageDir = new File(strImageDir);
		}

		if (!this.imageDir.exists()) {
			log.warn("Imagedir does not exist: " + strImageDir + "; Will not use DVD Profiler Covers");
		}

		xmlFileTool = new DVDProfilerXmlFile(xmlFile);

		initialized = true;
		log.info("DVDProfiler initialized");
	}

	private boolean isXmlModified() {
		return xmlFile.lastModified() > cfg.getXmlFileLastModified();
	}

	private void rebuildIndexes() throws Exception {
		log.debug("Rebuilding DVD Profiler Indexes....");

		LocalMovieIndex.getInstance().clean();
		LocalMovieIndex.getInstance().beginIndexing();
		xmlFileTool.visitMovies(LocalMovieIndex.getInstance());
		LocalMovieIndex.getInstance().endIndexing();

		cfg.setXmlFileLastModified(xmlFile.lastModified());
		Phoenix.getInstance().getConfigurationManager().save();
	}

	private boolean shouldRebuildIndexes() {
		return LocalMovieIndex.getInstance().isNew() || isXmlModified();
	}

	public DVDProfilerXmlFile getDvdProfilerXmlFile() {
		return xmlFileTool;
	}

	public File getImagesDir() {
		return imageDir;
	}

	/**
	 * getting metadata is synchronized because of the potention to rebuild the
	 * indexes
	 */
	public synchronized IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
		if (!initialized)
			initialize();

		if (MetadataSearchUtil.hasMetadata(result))
			return MetadataSearchUtil.getMetadata(result);

		return new DVDProfilerParser(result.getId(), this).getMetaData();
	}
}
