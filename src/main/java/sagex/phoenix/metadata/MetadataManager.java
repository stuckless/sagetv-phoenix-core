package sagex.phoenix.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import sagex.api.Configuration;
import sagex.api.ShowAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.fanart.FanartStorage;
import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.fanart.LocalFanartStorage;
import sagex.phoenix.metadata.factory.MetadataProviderBuilder;
import sagex.phoenix.metadata.persistence.Sage7Persistence;
import sagex.phoenix.metadata.provider.tmdb.TMDBMetadataProvider;
import sagex.phoenix.metadata.search.HasFindByIMDBID;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.metadata.search.SearchQueryFactory;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.filters.FilePathFilter;
import sagex.phoenix.vfs.filters.MediaResourceTypeFilter;
import sagex.phoenix.vfs.filters.MissingMetadataFilter;
import sagex.phoenix.vfs.sage.SageMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * Manages all Fanart/Metadata operations, from adding/removing providers,
 * searching, updating, etc.
 *
 * @author seans
 */
public class MetadataManager extends SystemConfigurationFileManager implements
        SystemConfigurationFileManager.ConfigurationFileVisitor {

    private static final String AUTO_IMPORT_AS_RECORDINGS_KEY = "enable_converting_imported_videos_to_tv_recordings";

    private MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
    private Map<String, IMetadataProvider> metadataProviders = new TreeMap<String, IMetadataProvider>();

    List<SystemConfigurationFileManager> configurations = new ArrayList<SystemConfigurationFileManager>();
    private File dtd;
    private EntityResolver dtdResolver;

    public MetadataManager(File systemDir, File userDir) {
        super(systemDir, userDir, new SuffixFileFilter(".xml", IOCase.INSENSITIVE));

        this.dtd = new File(systemDir, "metadata.dtd");
        dtdResolver = new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new FileInputStream(dtd));
            }
        };
    }

    /**
     * Get all providers registered to handle the given MediaType. It may return
     * an empty list.
     *
     * @param type {@link MediaType}
     * @return
     */
    public List<IMetadataProvider> getProviders(MediaType type) {
        List<IMetadataProvider> prov = new ArrayList<IMetadataProvider>();

        if (type == MediaType.TV) {
            prov.add(metadataProviders.get("tmdb"));
            prov.add(metadataProviders.get("tvdb"));
        } else if (type == MediaType.MOVIE) {
            prov.add(metadataProviders.get("tmdb"));
        }

        if (prov.size() == 0) {
            for (IMetadataProvider p : metadataProviders.values()) {
                for (MediaType t : p.getInfo().getSupportedSearchTypes()) {
                    if (t.equals(type)) {
                        prov.add(p);
                        break;
                    }
                }
            }
        }

        return prov;
    }

    /**
     * Get all providers
     *
     * @return
     */
    public List<IMetadataProvider> getProviders() {
        return new ArrayList<IMetadataProvider>(metadataProviders.values());
    }

    /**
     * Add a new Metadata Provider
     *
     * @param provider
     */
    public void addMetaDataProvider(IMetadataProvider provider) {
        log.debug("Adding Provider: " + provider.getInfo().getId() + "; " + provider);
        IMetadataProvider prov = metadataProviders.put(provider.getInfo().getId(), provider);
        if (prov != null) {
            log.warn("Provider: " + prov.getInfo() + " has been replaced with " + provider.getInfo());
        }
    }

    /**
     * Get a list of providers that match the given id. If the provider contains
     * a comma separated list, then the list will contain more than one.
     *
     * @param providerId
     * @return
     */
    public List<IMetadataProvider> getProviders(String providerId) {
        List<IMetadataProvider> provs = new ArrayList<IMetadataProvider>();

        if (!StringUtils.isEmpty(providerId) && providerId.contains(",")) {
            Pattern p = Pattern.compile("([^,]+)");
            Matcher m = p.matcher(providerId);
            while (m.find()) {
                String id = m.group(1).trim();
                IMetadataProvider provider = getProvider(id);
                if (provider != null) {
                    provs.add(provider);
                }
            }
        } else {
            log.debug("Finding Provider for " + providerId);
            IMetadataProvider provider = getProvider(providerId);
            if (provider != null) {
                log.debug("Added Provider for " + providerId + " with Implementation " + provider);
                provs.add(provider);
            }
        }

        return provs;
    }

    /**
     * for the given id, return the provider. This cannot be a comma separated
     * list.
     *
     * @param providerId
     * @return
     */
    public IMetadataProvider getProvider(String providerId) {
        // just a normal single id provider
        IMetadataProvider provider = metadataProviders.get(providerId);

        for (Map.Entry<String, IMetadataProvider> me : metadataProviders.entrySet()) {
            log.debug("getProvider('" + providerId + "'): " + me.getKey() + " -> " + me.getValue());
        }
        if (provider == null) {
            log.warn("Mising or Unknown Provider: " + providerId);
        }

        return provider;
    }

    /**
     * Tests if the given provider can accept the query.
     *
     * @param provider
     * @param query
     * @return
     */
    public boolean canProviderAcceptQuery(IMetadataProvider provider, SearchQuery query) {
        if (provider.getInfo().getSupportedSearchTypes() == null)
            return true;

        boolean accept = false;
        for (MediaType t : provider.getInfo().getSupportedSearchTypes()) {
            if (t == query.getMediaType())
                return true;
        }

        return accept;
    }

    /**
     * Reads the MediaType from the query, and selects a provider(s) based on
     * that. For each type, it will return the configured type from the
     * configuration.
     *
     * @param query
     * @return
     */
    public String getProviderForQuery(SearchQuery query) {
        String providerId = null;
        if (query.getMediaType() == MediaType.TV) {
            providerId = config.getTVProviders();
        } else if (query.getMediaType() == MediaType.MOVIE) {
            providerId = config.getMovieProviders();
        } else if (query.getMediaType() == MediaType.MUSIC) {
            providerId = config.getMusicProviders();
        } else {
            log.info("No providers registered for type: " + query.getMediaType());
        }
        return providerId;
    }

    /**
     * get a list of results for the given query. The results may be an empty
     * list.
     *
     * @param query
     * @return
     * @throws Exception if there was error performing the search
     */
    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
        return search(getProviderForQuery(query), query);
    }

    /**
     * Given the existing MediaFile and optional metadata, find the metadata/fanart for this item.
     * This is useful to "refresh" the metadata for an item.
     *
     * @param file
     * @param md
     * @return
     * @throws MetadataException
     */
    public IMetadata searchByExisting(IMediaFile file, IMetadata md) throws MetadataException {
        md = (md==null?file.getMetadata():md);
        SearchQuery q = new SearchQuery(getDefaultMetadataOptions());
        q.set(Field.PROVIDER, md.getMediaProviderID());
        q.set(Field.ID, md.getMediaProviderDataID());
        q.set(Field.IMDBID, md.getIMDBID());
        if (file.isType(MediaResourceType.TV.value())) {
            if (md.getEpisodeNumber() > 0) {
                q.set(Field.EPISODE, String.valueOf(md.getEpisodeNumber()));
                q.set(Field.SEASON, String.valueOf(md.getSeasonNumber()));
            }
        }

        if (sagex.phoenix.util.StringUtils.isAnyEmpty(q.get(Field.PROVIDER))) {
            log.info("Unable to find metadata for item by id.  Missing provider for " + file);
            throw new MetadataException("Can't find by ID, since provider is missing.", file, md);
        }

        // if IMDB is set as the provider, then remap to TMDB
        if ("imdb".equalsIgnoreCase(q.getProvider())) {
            q.setProvider("tmdb");
            q.setIMDBID(q.getId());
            q.setId(null);
        }

        if (!sagex.phoenix.util.StringUtils.hasAny(q.get(Field.ID), q.getIMDBId())) {
            log.info("Unable to find metadata for item by id.  Missing id or imdbid for " + file);
            throw new MetadataException("Can't find by ID, since id or imdbid is missing.", file, md);
        }

        List<IMetadataSearchResult> results = search(q);
        if (results==null || results.size()!=1) {
            throw new MetadataException("Search by ID failed", file, md);
        }

        return getMetdata(results.get(0));
    }

    /**
     * get a list of results for the given query. The results may be an empty
     * list.
     *
     * @param id    commas separated list of providers to use
     * @param query search query
     * @return
     * @throws Exception if the search cannot be performed
     */
    public List<IMetadataSearchResult> search(String id, SearchQuery query) throws MetadataException {
        if (id == null) {
            throw new MetadataException("No Metadata Provider was specified", query);
        }

        log.info("search(): " + id + "; " + query);
        List<IMetadataProvider> providers = getProviders(id);
        if (providers.size() == 0) {
            log.warn("Invalid provider id: " + id + " using defaults");
            providers = getProviders(query.getMediaType());
        }

        if (providers.size() == 0) {
            log.warn("No metadata providers: " + providers + " registered.  Query was: " + query);
            throw new MetadataException("Unable to create a list of valid providers for " + providers, query);
        }

        log.debug("Can search using " + providers.size() + " providers");

        List<IMetadataSearchResult> results = new ArrayList<IMetadataSearchResult>();
        for (IMetadataProvider p : providers) {
            try {
                if (StringUtils.isEmpty(query.get(Field.QUERY))) {
                    query.set(Field.QUERY, query.get(Field.RAW_TITLE));
                }
                log.info("Searching: " + query.get(Field.QUERY) + " using " + p);
                List<IMetadataSearchResult> curResults = p.search(query);
                if (curResults != null) {
                    results.addAll(curResults);
                    if (MetadataSearchUtil.isGoodSearch(curResults)) {
                        // break since we have a good results
                        // TODO: have an option to allow the search to continue
                        // for
                        // all providers
                        break;
                    }
                }

                // search failed, so let's use the clean title for searching
                // only search the clean title, if it's different than our QUERY
                String q = query.get(Field.QUERY);
                if (!q.equals(query.get(Field.CLEAN_TITLE)) && !StringUtils.isEmpty(query.get(Field.CLEAN_TITLE))) {
                    query.set(Field.QUERY, query.get(Field.CLEAN_TITLE));
                    log.info("Searching Using Cleaned Title: " + query.get(Field.QUERY) + " using " + p);
                    curResults = p.search(query);
                    if (curResults != null) {
                        results.addAll(curResults);

                        if (MetadataSearchUtil.isGoodSearch(curResults)) {
                            break;
                        }
                    }
                }
                log.info("No good matches for " + query.get(Field.QUERY) + " will try other providers if available.");
            } catch (MetadataException me) {
                log.warn("Search Failed for: " + query + " using provider " + p + "; Message: " + me.getMessage(), me);
                throw me;
            } catch (Throwable e) {
                throw new MetadataException("Search Failed Badly for " + query, query, e);
            }
        }

        if (results.size() == 0) {
            throw new MetadataException("No Results for " + query, query);
        }

        return results;
    }

    /**
     * Simply uses the {@link SearchQueryFactory} to create a basic query from a
     * media file. By default, it will try to create a query using the existing
     * metadata for Airings and Recorded TV Shows
     *
     * @param file
     * @return
     */
    public SearchQuery createQuery(IMediaFile file, Hints hints) {
        return Phoenix.getInstance().getSearchQueryFactory().createSageFriendlyQuery(file, hints);
    }

    /**
     * for a given search result, return the metadata
     *
     * @param result
     * @return
     * @throws Exception if the metadata cannot be fetched
     */
    public IMetadata getMetdata(IMetadataSearchResult result) throws MetadataException {
        log.info("Fetching Metadata for " + result);
        IMetadataProvider prov = getProvider(result.getProviderId());
        if (prov == null) {
            log.warn("Result does not contain valid provider id: " + result);
            throw new MetadataException("Invalid Search Result: " + result, result);
        }

        IMetadata md = prov.getMetaData(result);

        // have the metadata, now see if we need to get fanart - only used if the fanart provider is OTHER THAN the metadata provider
        if (config.isFanartEnabled()) {
            String fid = prov.getInfo().getFanartProviderId();
            if (!StringUtils.isEmpty(fid) && !fid.equals(prov.getInfo().getId())) {
                IMetadataProvider fanprov = getProvider(fid);
                if (fanprov == null) {
                    log.warn("Invalid fanart provider id: " + fid);
                } else {
                    log.info("Fetching Fanart from alternate source: " + fanprov + " for result: " + result);
                    if (!StringUtils.isEmpty(md.getIMDBID()) && fanprov instanceof HasFindByIMDBID) {
                        IMetadata mdFan = ((HasFindByIMDBID) fanprov).getMetadataForIMDBId(md.getIMDBID());
                        if (mdFan != null) {
                            md.getFanart().addAll(mdFan.getFanart());
                        } else {
                            log.warn("No Fanart for " + result + " using " + fid);
                        }
                    } else {
                        try {
                            SearchQuery q = new SearchQuery(result.getMediaType(), result.getTitle(), String.valueOf(result
                                    .getYear()));
                            List<IMetadataSearchResult> results = search(fid, q);
                            IMetadataSearchResult fanRes = MetadataSearchUtil.getBestResultForQuery(results, q);
                            if (fanRes == null) {
                                throw new Exception("Fanart Search return nothing");
                            }
                            IMetadata fanMD = getMetdata(fanRes);
                            md.getFanart().addAll(fanMD.getFanart());
                        } catch (Exception e) {
                            log.warn("Failed to get Fanart for result: " + result, e);
                        }
                    }
                }
            } else {
                log.info("Skipping Fanart since "
                        + ((StringUtils.isEmpty(fid) ? "Fanart Provider is empty" : "We are the fanart provider")));
            }
        } else {
            log.info("Fanart is disabled, so no fetching of fanart artificats.");
        }

        return md;
    }

    /**
     * For a given list of search results, return the metadata for the best
     * matched result. The {@link SearchQuery} is used to so that a best match
     * can match on title, year, and not ONLY on the score.
     *
     * @param results List of search results
     * @param query   the original search query.
     * @return
     * @throws Exception if the metadata cannot be fetched
     */
    public IMetadata getMetdata(List<IMetadataSearchResult> results, SearchQuery query) throws MetadataException {
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        if (result == null) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't find a valid match for results for list: " + results);
            }
            throw new MetadataException("Metadata lookup failed for " + ((query == null) ? "?" : query.get(Field.QUERY)), query);
        }
        return getMetdata(result);
    }

    /**
     * Updates the metadata for the given {@link IMediaFile} using the
     * {@link IMetadata} provided.
     *
     * @param file
     * @param data
     * @param options
     * @throws IOException
     */
    public void updateMetadata(IMediaFile file, IMetadata data, Hints options) throws MetadataException {
        if (data == null) {
            throw new MetadataException("No metadata for: " + file, null, file, null, null);
        }

        if (options == null) {
            options = getDefaultMetadataOptions();
        }

        // note we need to update the metadata if fanart is being written
        // but Sage7Persistence only updates the custom metadata if fanart only
        if (options.getBooleanValue(MetadataHints.UPDATE_METADATA, true)
                || options.getBooleanValue(MetadataHints.UPDATE_FANART, true)) {
            log.info("Saving Metadata for " + file + "; Hints: " + options);
            Sage7Persistence storage = new Sage7Persistence();
            storage.storeMetadata(file, data, options);
        }

        try {
            if (options.getBooleanValue(MetadataHints.UPDATE_FANART, true)) {
                if (config.isFanartEnabled()) {
                    log.info("Saving Phoenix Fanart for " + file + "; Hints: " + options);
                    File dir = new File(config.getFanartCentralFolder());
                    if (!dir.exists()) {
                        if (!dir.mkdirs()) {
                            throw new MetadataException("Unable to create Fanart Central Folder: " + dir);
                        }
                    }
                    FanartStorage fanart = new FanartStorage();
                    fanart.saveFanart(file, data.getMediaTitle(), data, options, config.getFanartCentralFolder());
                } else {
                    if (file.exists()) {
                        log.info("Saving Local Fanart for " + file);
                        LocalFanartStorage local = new LocalFanartStorage();
                        local.saveFanart(file, data);
                    }
                }
            }
        } catch (Throwable t) {
            log.warn("Some or all fanart was not saved due to some unkown problem", t);
        }
    }

    /**
     * Automatically search, select, and update the metadata/fanart for the
     * given search query and mediafile
     *
     * @param file
     * @param options
     * @throws Exception
     */
    public void automaticUpdate(IMediaFile file, Hints options) throws MetadataException {
        SearchQuery query = createQuery(file, options);
        automaticUpdate(getProviderForQuery(query), file, query, options);
    }

    /**
     * Automatically search, select, and update the metadata/fanart for the
     * given search query and mediafile
     *
     * @param file
     * @param query
     * @param options
     * @throws Exception
     */
    public void automaticUpdate(IMediaFile file, SearchQuery query, Hints options) throws MetadataException {
        automaticUpdate(getProviderForQuery(query), file, query, options);
    }

    /**
     * Automatically search, select, and update the metadata/fanart for the
     * given search query and mediafile
     *
     * @param id      metadata provider ids
     * @param file    {@link IMediaFile} instance
     * @param query   {@link SearchQuery} query instance to use for searching
     * @param options {@link Hints} any persistence options
     */
    public void automaticUpdate(String id, IMediaFile file, SearchQuery query, Hints options) throws MetadataException {

        // new SearcTask(query).then(new
        // GetMetadataTask(query).then(update(file, options)));
        updateMetadata(file, getMetdata(search(id, query), query), options);
    }

    /**
     * Return true if this file can be scanned. This will determine if the media
     * can be scanned based on whether or not it fits the scannable media types
     * and whether or not the media has been excluded based on the exclude
     * filter.
     *
     * @param file    {@link IMediaFile} to scan
     * @param options
     */
    public boolean canScanMediaFile(IMediaFile file, Hints options) {
        MediaResourceTypeFilter typeFilter = new MediaResourceTypeFilter(config.getScannableMediaTypes());
        boolean accept = typeFilter.accept(file);

        if (accept) {
            accept = !isExcluded(file);
        }

        if (accept && options != null && options.getBooleanValue(MetadataHints.SCAN_MISSING_METADATA_ONLY, true)) {
            MissingMetadataFilter missing = new MissingMetadataFilter();
            accept = missing.accept(file);
        }

        return accept;
    }

    /**
     * Returns true if the mediafile has been excluded based on the global
     * exclude pattern
     *
     * @param file
     * @return
     */
    public boolean isExcluded(IMediaResource file) {
        if (!StringUtils.isEmpty(config.getExcludePattern())) {
            FilePathFilter regFilter = new FilePathFilter();
            regFilter.setValue(config.getExcludePattern());
            regFilter.setExclude();
            return !regFilter.accept(file);
        }
        return false;
    }

    /**
     * Returns the Default Metadata Options that will be used for scanning and
     * persistence. These options are immutable, so you can change them in this
     * instance, and it will NOT change the default options, except in this
     * instance.
     *
     * @return {@link Hints} that contain one of Hints as defined in
     * {@link MetadataHints}
     */
    public Hints getDefaultMetadataOptions() {
        Hints options = new Hints();
        options.setBooleanHint(MetadataHints.IMPORT_TV_AS_RECORDING, config.getImportTVAsRecordings());
        options.setBooleanHint(MetadataHints.SCAN_MISSING_METADATA_ONLY, true);
        options.setBooleanHint(MetadataHints.SCAN_SUBFOLDERS, true);
        options.setBooleanHint(MetadataHints.UPDATE_FANART, true);
        options.setBooleanHint(MetadataHints.UPDATE_METADATA, true);
        return options;
    }

    /**
     * Imports a mediafile as a Recording so that it will show up in the Sage
     * Recordings.
     * <p/>
     * This will handle importing Movies and TV shows into the recordings area.
     *
     * @param file
     */
    public boolean importMediaFileAsRecording(IMediaFile file) {
        try {
            if (file.isType(MediaResourceType.RECORDING.value())) {
                log.warn("Cannot import as recording.  MediaFile is already a Recording.");
                return false;
            }

            if (file.isType(MediaResourceType.VIDEODISC.value())) {
                log.warn("Cannot import DVD or Bluray Discs as Recordings");
                return false;
            }

            IMetadata md = file.getMetadata();
            String showId = md.getExternalID();
            if (file.isType(MediaResourceType.TV.value())) {
                if (md.getDiscNumber() > 0) {
                    showId = createShowId("EP", String.format("EPmt%sS%02dD%02d", md.getMediaProviderDataID(),
                            md.getSeasonNumber(), md.getDiscNumber()));
                } else {
                    showId = createShowId(
                            "EP",
                            String.format("EPmt%sS%02dE%02d", md.getMediaProviderDataID(), md.getSeasonNumber(),
                                    md.getEpisodeNumber()));
                }
            } else if (file.isType(MediaResourceType.ANY_VIDEO.value())) {
                showId = createShowId("MV", String.format("MVmt%sD%02d", md.getMediaProviderDataID(), md.getDiscNumber()));
            } else {
                log.warn("Can't Import Media as Recording; Invalid Type: " + file);
                return false;
            }

            if (showId == null) {
                log.warn("Did not create a show id???: " + file);
                return false;
            }

            if (showId.equals(md.getExternalID())) {
                log.warn("ShowID didn't change???: " + file);
                return false;
            }

            // now check to see if this show id is in use...
            Object sagemf = ShowAPI.GetShowForExternalID(showId);
            if (sagemf != null) {
                log.warn("Cannot import recording, since the showid already exists: " + showId + ": " + file);
                return false;
            }

            // import as recording...
            md.setExternalID(showId);

            // check if we should be updating the archived flag
            // by default imported recordings are archived
            if (!config.getArchiveRecordings()) {
                file.setLibraryFile(false);
            }

            log.info("Imported MediaFile as Recording: " + file);

            return true;
        } catch (Throwable t) {
            log.warn("Failed to import media item as tv for some unknown reason!", t);
            return false;
        }
    }

    /**
     * Takes a Recording, and it removes it as a Sage Recording, so that it will
     * show up in the standard video library. This will actually have to delete
     * the mediafile, physically, and then re-add it, in order to complete the
     * task. This is a destructive operation.
     *
     * @param file
     * @returns the newly unimported {@link IMediaFile} instance which will be
     * different than the one passed in
     */
    public IMediaFile unimportMediaFileAsRecording(IMediaFile file) {
        String oldval = Configuration.GetServerProperty(AUTO_IMPORT_AS_RECORDINGS_KEY, "false");
        try {
            Configuration.SetServerProperty(AUTO_IMPORT_AS_RECORDINGS_KEY, "false");
            if (!file.isType(MediaResourceType.RECORDING.value())) {
                log.warn("Skipping unimport operation, since file is not a recording: " + file);
                return null;
            }

            IMetadata cur = file.getMetadata();
            IMetadata old = MetadataUtil.createMetadata();

            // backing up the metadata
            try {
                MetadataUtil.copyMetadata(cur, old);
            } catch (Exception e) {
                log.warn("Failed to backup old metadata!", e);
                return null;
            }

            // if there is a .properties file, then remove it
            File props = FanartUtil.resolvePropertiesFile(PathUtils.getFirstFile(file));
            if (props != null && props.exists()) {
                if (!props.delete()) {
                    log.warn("Can't remove the old properties file for media file, so we can't unimport this media item: " + file);
                    return null;
                }
            }

            // create a new non-tv show id
            old.setExternalID(createShowId("MF", null));

            Object sageMF = phoenix.util.RemoveMetadataFromMediaFile(file);
            if (sageMF == null) {
                log.warn("Failed to unimport the old mediafile!");
                return null;
            }

            SageMediaFile smf = new SageMediaFile(file.getParent(), sageMF);
            IMetadata md = smf.getMetadata();
            try {
                MetadataUtil.copyMetadata(old, md);
            } catch (Exception e) {
                log.warn("Failed to re-populate the old metadata, so it will have no metadata :(");
            }

            return smf;
        } finally {
            Configuration.SetServerProperty(AUTO_IMPORT_AS_RECORDINGS_KEY, oldval);
        }
    }

    /**
     * creates a new showId using the prefix, if the preferredId is already
     * taken.
     *
     * @return
     */
    private String createShowId(String prefix, String preferredId) {
        if (preferredId != null && ShowAPI.GetShowForExternalID(preferredId) == null) {
            // this is ok, doesn't exist
            return preferredId;
        }

        String id = null;
        do {
            // keep gen'd EPGID less than 12 chars
            // SEANS: Taken from nielm's xmlinfo... I'm guessing there is a 12
            // char limit, so i won't mess with it
            id = prefix + "mt" + Integer.toHexString((int) (java.lang.Math.random() * 0xFFFFFFF));
        } while (ShowAPI.GetShowForExternalID(id) != null);

        return id;
    }

    /**
     * Creates a new Search Result that can be retrieved by ID
     *
     * @param provId
     * @param id
     * @return
     */
    public IMetadataSearchResult createResultForId(String provId, String id) {
        MediaSearchResult res = new MediaSearchResult();
        res.setProviderId(provId);
        res.setId(id);
        return res;
    }

    public void addConfiguration(SystemConfigurationFileManager config) {
        configurations.add(config);
    }

    @Override
    public void loadConfigurations() {
        log.info("Begin Loading Metadata Providers");
        metadataProviders.clear();
        accept(this);
        for (SystemConfigurationFileManager c : configurations) {
            c.loadConfigurations();
        }
        log.info("End Loading Metadata Providers");
    }

    @Override
    public void visitConfigurationFile(ConfigurationType type, File file) {
        log.info("Loading Metadata Provider: " + file);
        MetadataProviderBuilder builder = new MetadataProviderBuilder(file.getAbsolutePath());
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            try {
                reader.setFeature("http://xml.org/sax/features/validation", true);
            } catch (SAXException e) {
                log.warn("Cannot activate validation.", e);
            }
            reader.setContentHandler(builder);
            reader.setErrorHandler(builder);
            reader.setEntityResolver(dtdResolver);
            reader.parse(new InputSource(new FileInputStream(file)));
            IMetadataProvider p = builder.getProvider();
            if (p == null) {
                throw new IOException("Failed to parse provider for file " + file);
            }

            // do not allow overriding of core
            if (metadataProviders.containsKey(p.getInfo().getId())) {
                throw new IOException("Not allowed to override system providers, please use a different provider id for file "
                        + file);
            }

            addMetaDataProvider(p);
        } catch (FileNotFoundException e) {
            log.warn("File not found", e);
        } catch (IOException e) {
            log.warn("IO Error", e);
        } catch (SAXException e) {
            log.warn("Xml Parsing Error", e);
        }
    }
}
