package sagex.phoenix.upnp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.StorageFolder;

import sagex.phoenix.Phoenix;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;

public class VFSContentDirectoryService extends AbstractContentDirectoryService {
	private Logger log = Logger.getLogger(this.getClass());

	public VFSContentDirectoryService() {
		super();
	}

	@Override
	public BrowseResult browse(String objectId, BrowseFlag browseFlag,
			String filter, long firstResult, long maxResults,
			SortCriterion[] sort) throws ContentDirectoryException {
		try {
			log.info("browse(): " + objectId + "; " + browseFlag + "; "
					+ filter + "; " + firstResult + "; " + maxResults + "; "
					+ sort);

			if (objectId == null || "0".equals(objectId)) {
				return getRootViews(browseFlag, filter, firstResult, maxResults, sort);
			} else {
				return getViewDetails(objectId, browseFlag,
						filter, firstResult, maxResults,
						sort);
			}
		} catch (Exception ex) {
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
		}
	}

	private BrowseResult getViewDetails(String objectId, BrowseFlag browseFlag,
			String filter, long firstResult, long maxResults,
			SortCriterion[] sort) {
		
		String parts[] = objectId.split(":");
		
		IMediaFolder children = null;
		ViewFolder folder = getViewFolder(parts[0]);
		if (parts.length>1) {
			children = (IMediaFolder) folder.getChild(parts[1]);
		}
		
		return null;
	}

	private ViewFolder getViewFolder(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	private BrowseResult getRootViews(BrowseFlag browseFlag, String filter,	long firstResult, long maxResults, SortCriterion[] sort) throws Exception {
		DIDLContent didl = new DIDLContent();
		
		VFSManager vmgr = Phoenix.getInstance().getVFSManager();
		List<ViewFactory> allviews = new ArrayList<ViewFactory>();
		Set<ViewFactory> views = vmgr.getVFSViewFactory().getFactories("upnp");
		if (views==null || views.size()==0) {
			allviews.addAll(vmgr.getVFSViewFactory().getFactories(false));
		} else {
			allviews.addAll(views);
		}

		for (ViewFactory vf: allviews) {
			StorageFolder folder = new StorageFolder(vf.getName(), "0",
					vf.getLabel(), "Phoenix", 10, 1l);
			didl.addContainer(folder);
		}

		// String album = ("Black Gives Way To Blue");
		// String creator = "Alice In Chains"; // Required
		// PersonWithRole artist = new PersonWithRole(creator,
		// "Performer");
		// MimeType mimeType = new MimeType("audio", "mpeg");
		//
		// didl.addItem(new MusicTrack("101", "3", // 101 is the Item
		// ID, 3 is
		// // the parent Container ID
		// "All Secrets Known", creator, album, artist, new Res(
		// mimeType, 123456l, "00:03:25", 8192l,
		// "http://10.0.0.1/files/101.mp3")));
		//
		// didl.addItem(new MusicTrack("102", "3", "Check My Brain",
		// creator,
		// album, artist, new Res(mimeType, 2222222l, "00:04:11",
		// 8192l, "http://10.0.0.1/files/102.mp3")));
		//
		// // Create more tracks...
		//
		// // Count and total matches is 2
		//
		// return new BrowseResult(new DIDLParser().generate(didl), 2,
		// 2);
		return new BrowseResult(new DIDLParser().generate(didl), views.size(), views.size());
	}

	@Override
	public CSV<String> getSortCapabilities() {
		// TODO Auto-generated method stub
		return super.getSortCapabilities();
	}
}
