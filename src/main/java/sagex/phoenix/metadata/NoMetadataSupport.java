package sagex.phoenix.metadata;

import sagex.phoenix.util.Hints;

import java.util.Date;

/**
 * Stub class that simply implements the IMetadataSupport calls. Only used to
 * satisfy the API contract when BMT does not exist.
 *
 * @author seans
 */
public class NoMetadataSupport implements IMetadataSupport {

    public boolean cancelMetadataScan(Object progress) {
        return true;
    }

    public float getMetadataScanComplete(Object progress) {
        return 0;
    }

    public IMetadataSearchResult[] getMetadataSearchResults(Object mediaFile, String name, String type) {
        return null;
    }

    public IMetadataSearchResult[] getMetadataSearchResults(Object mediaFile) {
        return null;
    }

    public boolean isMetadataScanRunning(Object progress) {
        return false;
    }

    public Object startMetadataScan(Object mediaFiles, Hints options) {
        return null;
    }

    public boolean updateMetadataForResult(Object mediaFile, IMetadataSearchResult searchResult, Hints options) {
        return false;
    }

    public int getSuccessCount(Object progress) {
        return 0;
    }

    public Object[] getFailed(Object progress) {
        return new Object[]{};
    }

    public int getFailedCount(Object progress) {
        return 0;
    }

    public Object[] getSkipped(Object progress) {
        return new Object[]{};
    }

    public int getSkippedCount(Object progress) {
        return 0;
    }

    public Object[] getSuccess(Object progress) {
        return new Object[]{};
    }

    @Override
    public Object[] getTrackers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMetadataScanLabel(Object progress) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getMetadataScanLastUpdated(Object progress) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMetadataScanStatus(Object progress) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isMetadataScanCancelled(Object progress) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getTotalWork(Object progress) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getWorked(Object progress) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void removeMetadataScan(Object progress) {
        // TODO Auto-generated method stub

    }
}
