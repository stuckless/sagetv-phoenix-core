package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Sorts based on the Date Added to SageDB (MediaFileID)
 * 
 * @author bialio
 *
 */
public class MediaFileIDSorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    public MediaFileIDSorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1==null) return 1;
        if (o2==null) return -1;   
        
        // Get the relevant info
        if (o1 instanceof IMediaFile && o2 instanceof IMediaFile) {
        	
        	int id1 = NumberUtils.toInt(((IMediaFile)o1).getId());
        	int id2 = NumberUtils.toInt(((IMediaFile)o2).getId());     
        	
	        return (id1 - id2);
        }        

        // If they are not both IMediaFile return 0
        return 0;        
    }
}