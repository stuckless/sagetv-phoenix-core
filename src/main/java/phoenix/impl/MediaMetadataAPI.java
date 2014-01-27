package phoenix.impl;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.cache.ICache;
import sagex.phoenix.cache.SimpleWeakMapCache;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.IMetadataSupport;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataHints;
import sagex.phoenix.metadata.PhoenixMetadataSupport;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Service Calls for Dealing with media file metadata. ie, for searching for
 * metadata, updating metadata and enabling/disabling metadata providers.
 * 
 * @author seans
 */
@API(group="metadatascan")
public class MediaMetadataAPI {
    private static final Logger         log                        = Logger.getLogger(MediaMetadataAPI.class);

    private ICache<Properties>          cachedMetadataLabels       = new SimpleWeakMapCache<Properties>();
    private IMetadataSupport            support                    = null;

    public MediaMetadataAPI() {
    	support = new PhoenixMetadataSupport();
    }

   /**
    * Starts the Metadata/Fanart Scanner will an array of MediaFile objects.
    * 
    * If a scan is in progress, then this scan is immediately aborted, and this
    * scan will not happen.
    * 
    * The Scanner will start a separate thread for the scan and return
    * immediately
    * 
    * TODO: Figure out a way to have the scanner notify on complete
    * 
    * @param mediaFiles
    *            Object Array of Sage MediaFile or Airing objects, or a File object specifying a directory
    * @param options {@link IMetadataOptions} instance
    * @return ProgressTracker instance for the current scan, or null, if a scan
    *         could not be started
    */
    public Object StartMetadataScan(Object source, Object options) {
   		return support.startMetadataScan(source, getHints(options));
    }
    
    private Hints getHints(Object options) {
    	Hints opts = Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions();
    	if (options==null) {
    	} else if (options instanceof Hints){
    		opts.addHints((Hints) options);
    	} else  if (options instanceof String) {
    		Map<String,String> json = phoenix.util.ToMap(options);
    		if (json!=null) {
    			opts.addHints(json);
    		}
    	} else {
    		log.warn("Unknown Scan Options: " + options);
    	}
    	return opts;
    }

    /**
     * Returns true if a metadata scan is currently in progress.
     * 
     * @param progress
     *            ProgressTracker from a StartMetadataScan operation
     * @return true if scan in progress
     */
    public boolean IsMetadataScanRunning(Object progress) {
        return support.isMetadataScanRunning(progress);
    }

    /**
     * Returns the percent complete in the scan. 0 mean not started, 1 means
     * completed.
     * 
     * @param tracker
     *            Progress Tracker from a StartMetadataScan operation
     * @return float value representing the percent complete of the current scan
     */
    public float GetMetadataScanComplete(Object tracker) {
        return support.getMetadataScanComplete(tracker);
    }

    /**
     * Cancel a scan
     * 
     * @param tracker
     * @return
     */
    public boolean CancelMetadataScan(Object tracker) {
        return support.cancelMetadataScan(tracker);
    }

    /**
     * Simply calls GetMetadataSearchResults(null, mediafile)
     * 
     * @param mediaFile
     *            mediafile or title
     * @return search results
     */
    public IMetadataSearchResult[] GetMetadataSearchResults(Object mediaFile) {
        return GetMetadataSearchResults(mediaFile, null, null);
    }

    /**
     * Get the title for the given metadata search result
     * 
     * @param result
     *            metadata search result
     * @return title
     */
    public String GetMetadataSearchResultTitle(IMetadataSearchResult result) {
        return result.getTitle();
    }

    /**
     * Get the score for the given metadata search result. A scor is a float
     * value from 0-1 indicating the strength of the search resule in relation
     * to the original title, file. A value is stronger as it appoaches 1.
     * 
     * @param result
     *            metadata search result
     * @return float score
     */
    public float GetMetadataSearchResultScore(IMetadataSearchResult result) {
        return result.getScore();
    }

    /**
     * Get the Year for the given metadata search result.
     * 
     * @param result
     *            metadata search result
     * @return year
     */
    public String GetMetadataSearchResultYear(IMetadataSearchResult result) {
    	if (result.getYear()<=0) return null;
        return String.valueOf(result.getYear());
    }

    /**
     * get the provider id that was used to return the given result. A provider
     * id basically identifies which provider actually performed the search, ie,
     * imdb, dvd profiler, tvdb, etc.
     * 
     * @param result
     *            metadata search result
     * @return provider id
     */
    public String GetMetadataSearchResultProviderId(IMetadataSearchResult result) {
        return result.getProviderId();
    }

    /**
     * Used to find metadata for a media file using a specified name and type.
     * ie, it will ignore the name and type of the mediaFile and search using
     * the passed name and time
     * @param mediaFile Sage MediaFile object
     * @param name Show Title, Movie Title, etc
     * @param type TV, Movie, Music
     * @return search results or null if no results.
     */
    public IMetadataSearchResult[] GetMetadataSearchResults(Object mediaFile, String name, String type) {
        return support.getMetadataSearchResults(mediaFile, name, type);
    }

    /**
     * udpate the metadata for the given media file with the specified search
     * result. This will download all fanart and update the sage properties
     * associated with the mediafile object.
     * 
     * if wait is true, then this call will block until the metadata has been
     * updated. If false, then it will run the background, and it will notify
     * the user of errors using the SystemMessage event service.
     * 
     * @param mediaFile
     *            SageTV media file object
     * @param result
     *            metadata search result
     * 
     * @param wait
     *            if false, then it will run in the background
     * 
     */
    public void UpdateMediaFileMetadata(Object mediaFile, IMetadataSearchResult result) {
        UpdateMediaFileMetadata(mediaFile, result, false, null);
    }

    /**
     * udpate the metadata for the given media file with the specified search
     * result. This will download all fanart and update the sage properties
     * associated with the mediafile object.
     * 
     * if wait is true, then this call will block until the metadata has been
     * updated. If false, then it will run the background, and it will notify
     * the user of errors using the SystemMessage event service.<br/>
     * <br/> 
     * The valid options are overwrite-fanart, overwrite-metadata.
     * All options must be set as a string such as "true" or "false"
     * 
     * @param mediaFile
     *            SageTV media file object
     * @param result
     *            metadata search result
     * @param wait
     *            if false, then it will run in the background
     * @param options a Map of options
     * 
     */
    public void UpdateMediaFileMetadata(Object mediaFile, IMetadataSearchResult result, boolean wait, Object options) {
   		support.updateMetadataForResult(mediaFile, result, getHints(options));
    }

    /**
     * Return the number of failed items in this progress.   You can call this while a scan is in progress.
     * 
     * @param progress
     * @return
     */
    public int GetMetadataProgressFailedCount(Object progress) {
        return support.getFailedCount(progress);
    }
    /**
     * Return the number of success items in this progress.   You can call this while a scan is in progress.
     * 
     * @param progress
     * @return
     */
    public int GetMetadataProgressSuccessCount(Object progress) {
        return support.getSuccessCount(progress);
    }
    /**
     * Return the number of skipped items in this progress.   You can call this while a scan is in progress.
     * 
     * @param progress
     * @return
     */
    public int GetMetadataProgressSkippedCount(Object progress) {
        return support.getSkippedCount(progress);
    }
    
    /**
     * return the {@link IMediaFile} that failed in this progress, use the {@link MediaBrowserAPI} to
     * access this object
     * 
     * @param progress
     * @return
     */
    public Object[] GetMetadataFailedItems(Object progress) {
        return support.getFailed(progress);
    }
    
    /**
     * return the {@link IMediaFile} that succeeded in this progress, use the {@link MediaBrowserAPI} to
     * access this object
     * 
     * @param progress
     * @return
     */
    public Object[] GetMetadataSuccessItems(Object progress) {
        return support.getFailed(progress);
    }
    
    /**
     * return the {@link IMediaFile} that skipped in this progress, use the {@link MediaBrowserAPI} to
     * access this object
     * 
     * @param progress
     * @return
     */
    public Object[] GetMetadataSkippedItems(Object progress) {
        return support.getFailed(progress);
    }
    
    /**
     * Return a list of all the known Trackers that are in the system.  Some may have been completed.
     * 
     * @return
     */
    public Object[] getMetadataScanTrackers() {
    	return support.getTrackers();
    }
    
    /**
     * Creates a base set of 'hints' or options that can be passed to a scan.  For a complete list of 
     * the hint keys, see the {@link MetadataHints} static constants
     * 
     * @return Map that can be used to set hints
     */
    public Map<String,String> createHints() {
    	return getHints(null).getHints();
    }
    
    /**
     * Return the Metadata for a given search result
     * 
     * @param result
     * @return
     */
    public IMetadata GetMetadata(IMetadataSearchResult result) {
    	try {
    		return Phoenix.getInstance().getMetadataManager().getMetdata(result);
    	} catch (Exception e) {
    		log.warn("Failed to get Metadata for result " + result);
    		return null;
    	}
    }
    
    /**
     * Updates the metadata for a given media file
     * 
     * @param mediafile {@link IMediaFile} or native SageTV object
     * @param metadata {@link IMetadata} object
     * @param hints peristence options, can be null.
     * @return
     */
    public boolean UpdateMetadata(Object mediafile, IMetadata metadata, Object hints) {
    	try {
			Phoenix.getInstance().getMetadataManager().updateMetadata(phoenix.media.GetMediaFile(mediafile), metadata, getHints(hints));
		} catch (MetadataException e) {
			log.warn("Failed to update Metadata for Object " + mediafile);
			return false;
		}
		return true;
    }
    
    /**
     * Create a new Search Query for metadata or Fanart
     * 
     * @param mediaType search type, TV, MOVIE, MUSIC
     * @param mediaTitle title to search for
     * @param hints optional hints, can be null
     * @return
     */
    public SearchQuery CreateQuery(String mediaType, String mediaTitle, Object hints) {
    	SearchQuery q = new SearchQuery(getHints(null));
    	q.setMediaType(MediaType.toMediaType(mediaType));
    	SetSearchTitle(q, mediaTitle);
    	return q;
    }

    /**
     * Sets the search title 
     * @param q
     * @param mediaTitle
     */
	public void SetSearchTitle(SearchQuery q, String mediaTitle) {
		q.set(Field.CLEAN_TITLE, mediaTitle);
		q.set(Field.RAW_TITLE, mediaTitle);
		q.set(Field.QUERY, mediaTitle);
	}
	
	/**
	 * Sets the year for the search
	 * @param q
	 * @param year
	 */
	public void SetSearchYear(SearchQuery q, int year) {
		q.set(Field.YEAR, String.valueOf(year));
	}

	/**
	 * Sets the Episode Aired Date in the search, in the format, 'YYYY-MM-DD'
	 * 
	 * @param q
	 * @param date
	 */
	public void SetSearchEpisodeDate(SearchQuery q, String date) {
		q.set(Field.EPISODE_DATE, date);
	}

	/**
	 * Sets the Epsisode Title in the search
	 * @param q
	 * @param title
	 */
	public void SetSearchEpisodeTitle(SearchQuery q, String title) {
		q.set(Field.EPISODE_TITLE, title);
	}
	
	/**
	 * Sets the provider id and the provider's unique id, ie, the imdb id, or the tvdb id in the search.
	 * This is used to search based on the known id for an item.
	 * 
	 * @param q
	 * @param provider
	 * @param id
	 */
	public void SetSearchId(SearchQuery q, String provider, String id) {
		q.set(Field.PROVIDER, provider);
		q.set(Field.ID, id);
	}

	/**
	 * sets the Episode's season number in the query
	 * 
	 * @param q
	 * @param season
	 */
	public void SetSearchSeason(SearchQuery q, int season) {
		q.set(Field.SEASON, String.valueOf(season));
	}
	
	/**
	 * Sets the Episode's episode number in the query
	 * 
	 * @param q
	 * @param episode
	 */
	public void SetSearchEpisode(SearchQuery q, int episode) {
		q.set(Field.EPISODE, String.valueOf(episode));
	}
	
	/**
	 * Returns null if no search results were found
	 * 
	 * @param query
	 * @return
	 */
	public IMetadataSearchResult[] Search(SearchQuery query) {
		try {
			return Phoenix.getInstance().getMetadataManager().search(query).toArray(new IMetadataSearchResult[] {});
		} catch (MetadataException e) {
			log.warn("Search failed: " + query);
			return null;
		}
	}
}
