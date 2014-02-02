package phoenix.impl;

import java.util.Properties;

import sagex.api.AiringAPI;
import sagex.phoenix.metadata.IMetadataProviderInfo;
import sagex.phoenix.metadata.IMetadataSearchResult;

/**
 * This is where phoenix apis go when they are no longer used, supported, and
 * ready to die.
 * 
 * @author seans
 * 
 */
public class DeprecatedAPI {
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
	 * @param provider
	 *            provider to use for scanning, or null, to use the default
	 *            metadata provider
	 * @param mediaFiles
	 *            Object Array of Sage MediaFile or Airing objects
	 * @return ProgressTracker instance for the current scan, or null, if a scan
	 *         could not be started
	 * @deprecated provider is no longer passed
	 */
	public Object StartMetadataScan(Object provider, Object[] mediaFiles) {
		return null;
	}

	/**
	 * Returns true if a metadata scan is currently in progress.
	 * 
	 * @return true if scan in progress
	 * @deprecated use method that accepts a tracker
	 */
	public boolean IsMetadataScanRunning() {
		return false;
	}

	/**
	 * Returns the percent complete in the scan. 0 mean not started, 1 means
	 * completed.
	 * 
	 * @return float value representing the percent complete of the current scan
	 * @deprecated use method that accepts a tracker
	 */
	public float GetMetadataScanComplete() {
		return 0;
	}

	/**
	 * Performa metadata search using a specific metadata provider. If the
	 * provider is null, then the default provider will be used.
	 * 
	 * @param provider
	 *            metadata provider or null for the default
	 * @param mediaFile
	 *            mediafile or mediafile title
	 * 
	 * @return metadata
	 * @deprecated provider is no longer used
	 */
	public IMetadataSearchResult[] GetMetadataSearchResults(Object provider, Object mediaFile) {
		return null;
	}

	/**
	 * Get an array of metadata providers that are installed, but are not
	 * currently in use.
	 * 
	 * @return array of metadata providers not currently being used
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public IMetadataProviderInfo[] GetInActiveMetadataProviders() {
		return null;
	}

	/**
	 * Get an array of metadata providers that are currently being used to
	 * perform metadata lookups/fanart downloading. The array is ordered in the
	 * order in which they will be querried for results.
	 * 
	 * @return Array of metadata providers that is being used for metadata
	 *         lookups
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public IMetadataProviderInfo[] GetActiveMetadataProviders() {
		return null;
	}

	/**
	 * Get an array of all the available metadata providers installed in the
	 * system.
	 * 
	 * @return all metadata providers
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public IMetadataProviderInfo[] GetInstalledMetadataProviders() {
		return null;
	}

	/**
	 * Tests whether a given metadata provider is currently active and will
	 * potentially be used for metadata lookups
	 * 
	 * @param pi
	 *            metadata provider
	 * @return true if the metadata provider is active
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public boolean IsActiveMetadataProvider(IMetadataProviderInfo pi) {
		return false;
	}

	/**
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	private boolean isActiveMetadataProvider(IMetadataProviderInfo provs[], IMetadataProviderInfo pi) {
		return false;
	}

	/**
	 * within the array of active metadata providers, increase the priority of
	 * this provide. ie, move it up in the list.
	 * 
	 * @param pi
	 *            metadata provider
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public void IncreaseMetadataProviderPriority(IMetadataProviderInfo pi) {
	}

	/**
	 * within the array of active metadata providers, decrease the priority of
	 * this provider. ie, move it down in the list.
	 * 
	 * @param pi
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public void DecreaseMetadataProviderPriority(IMetadataProviderInfo pi) {
	}

	/**
	 * Add the given metadata provider to the list of active metadata providers.
	 * 
	 * @param pi
	 *            metadata provider
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public void AddActiveMetadataProvider(IMetadataProviderInfo pi) {
	}

	/**
	 * remove the metadata provider from the list of metadata providers. It is
	 * still in the instaled list of providers.
	 * 
	 * @param pi
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public void RemoveActiveMetadataProvider(IMetadataProviderInfo pi) {
	}

	/**
	 * return the name of the provider id for the given provider.
	 * 
	 * @param pi
	 *            privder object
	 * @return provider id
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public String GetMetadataProviderId(IMetadataProviderInfo pi) {
		return pi.getId();
	}

	/**
	 * return the metadata provider name
	 * 
	 * @param pi
	 *            provider object
	 * @return provider name
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public String GetMetadataProviderName(IMetadataProviderInfo pi) {
		return pi.getName();
	}

	/**
	 * return the provider description
	 * 
	 * @param pi
	 *            provider object
	 * @return description
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public String GetMetadataProviderDescription(IMetadataProviderInfo pi) {
		return pi.getDescription();
	}

	/**
	 * returns true if the current metadata provider support api is enabled. in
	 * the event that no api is installed, a stub api is created which return
	 * false. this is here to enable stv developers to check for the existence
	 * of metadata search support, and if this is enabled, then calling search
	 * methods on this api should also work.
	 * 
	 * @return true if metadata searching support is enabled
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public boolean IsMetadataProviderSupportEnabled() {
		return true;
	}

	/**
	 * Get the known metadata keys for a given type. The type can be "S" for
	 * Core Sage, "A" for all keys, or "E" for extended keys.
	 * 
	 * @param type
	 *            "A" for all, "S" for core sage, "E" for extended
	 * @return
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public String[] GetMetadataKeys(String type) {
		return null;
	}

	private Properties getLabelProperties() {
		Properties props = new Properties();
		return props;
	}

	/**
	 * Return a custom metadata label for a given metadata item.
	 * 
	 * @param key
	 *            Metadata key
	 * @return label
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public String GetMetadataLabel(String key) {
		return getLabelProperties().getProperty(key, key);
	}

	/**
	 * return the metadata key description for a given key. The description can
	 * be a simple description of the property, such as telling the user the
	 * preferred data type, range, etc.
	 * 
	 * @param key
	 *            metadata key
	 * @return description
	 * @deprecated configure using web ui, or using Configuration Metadata
	 */
	public String GetMetadataDescription(String key) {
		return getLabelProperties().getProperty(key + "-Description", "");
	}

	public long GetAiringStartTime(Object mediaFile) {
		Object sage = phoenix.api.GetSageMediaFile(mediaFile);
		return AiringAPI.GetAiringStartTime(sage);
	}

	public long GetAiringEndTime(Object mediaFile) {
		Object sage = phoenix.api.GetSageMediaFile(mediaFile);
		return AiringAPI.GetAiringEndTime(sage);
	}

	public boolean IsAiringManualRecord(Object mediaFile) {
		Object sage = phoenix.api.GetSageMediaFile(mediaFile);
		return AiringAPI.IsManualRecord(sage);
	}
}
