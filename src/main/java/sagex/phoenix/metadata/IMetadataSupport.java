package sagex.phoenix.metadata;

import java.util.Date;

import sagex.phoenix.util.Hints;


/**
 * Defines the base Metadata Scanning API that is exposed via the Phoenix APIs
 * 
 * @author seans
 */
public interface IMetadataSupport {
    public IMetadataSearchResult[] getMetadataSearchResults(Object mediaFile, String name, String type);
    public IMetadataSearchResult[] getMetadataSearchResults(Object mediaFile);
    public boolean updateMetadataForResult(Object mediaFile, IMetadataSearchResult searchResult, Hints options);
    
    public Object startMetadataScan(Object mediaFiles, Hints options);
    public boolean isMetadataScanRunning(Object progress);
    public float getMetadataScanComplete(Object progress);
    public int getSkippedCount(Object progress);
    public int getFailedCount(Object progress);
    public int getSuccessCount(Object progress);
    public Object[] getFailed(Object progress);
    public Object[] getSuccess(Object progress);
    public Object[] getSkipped(Object progress);
	public Object[] getTrackers();
	public int getTotalWork(Object progress);
	public int getWorked(Object progress);
	
	public boolean isMetadataScanCancelled(Object progress);
	public String getMetadataScanStatus(Object progress);
	public Date getMetadataScanLastUpdated(Object progress);
	public String getMetadataScanLabel(Object progress);
    
     /**
     * Return true if scan was cancelled
     * 
     * @param progress
     * @return
     */
    public boolean cancelMetadataScan(Object progress);
    public void removeMetadataScan(Object progress);
}
