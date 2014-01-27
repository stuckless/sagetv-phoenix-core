package phoenix;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sagex.api.MediaFileAPI;
import sagex.phoenix.fanart.SimpleMediaFile;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.remote.sync.*;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * API Generated: Thu Oct 10 19:40:46 EDT 2013<br/>
  */
public final class sync {
   private static sagex.phoenix.remote.sync.SyncService syncservice = new sagex.phoenix.remote.sync.SyncService();
   /**

	 * Convenience method for syncMediaFiles(null, mediaMask, 0).  This creates a new 
	 * Sync Session each time it is called.  Typically after calling this, you would
	 * either call syncMediaFiles(syncId, page) or destroySession(syncId).
	 * 
	 * @param mediaMask
	 * @return
	 
    */
   public static SyncReply SyncMediaFiles(String mediaMask) {
      return syncservice.syncMediaFiles(mediaMask);
   }

   /**

	 * Continues a sync session, requesting the next page for the given sync session id
	 * 
	 * @param syncId
	 * @param page
	 * @return
	 
    */
   public static SyncReply SyncMediaFiles(String syncId, int page) {
      return syncservice.syncMediaFiles(syncId, page);
   }

   /**

	 * Begins/Continues a Sync Session whereby a SyncReply is returned containing the session id
	 * the first page of sync data.  This sync is a "quick" sync, in that only a minimal
	 * amount of data is returned.  Subsequent calls will pass a syncId and a page until
	 * all data is synced.  Once the data is synced, destroySession(syncId) should be called
	 * to free up memory.
	 * 
	 * @param syncId
	 * @param mediaMask
	 * @param page
	 * @return
	 
    */
   public static SyncReply SyncMediaFiles(String syncId, String mediaMask, int page) {
      return syncservice.syncMediaFiles(syncId, mediaMask, page);
   }

   public static boolean DestroySession(String id) {
      return syncservice.destroySession(id);
   }

}

