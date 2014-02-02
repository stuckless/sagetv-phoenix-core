package phoenix.impl;

import java.io.File;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.api.AiringAPI;
import sagex.api.MediaFileAPI;
import sagex.api.SeriesInfoAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.SageSeriesInfo;
import sagex.phoenix.metadata.persistence.TVSeriesUtil;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.progress.BasicProgressMonitor;
import sagex.phoenix.progress.NullProgressMonitor;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.TextReplacement;
import sagex.phoenix.util.TextReplacement.IVariableResolver;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.HasPlayableUrl;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaConfiguration;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.sage.SageMediaFile;
import sagex.phoenix.vfs.util.PathUtils;
import sagex.phoenix.vfs.views.OnlineViewFolder;
import sagex.phoenix.vfs.views.ViewItem;
import sagex.phoenix.vfs.visitors.ClearCustomMetadataFieldsVisitor;
import sagex.phoenix.vfs.visitors.CollectorResourceVisitor;

/**
 * The MediaAPI is a set of API calls that will deal with getting information
 * out of the {@link IMediaFile} and {@link IMediaFolder} objects.
 * 
 * In time, we should have completely wrapped the SageTV {@link MediaFileAPI}.
 * The reason is that we want to abstract the fact that we are dealing with sage
 * tv media files, since we may not always be dealing with SageTV media files.
 * For example, if a trailers application is written, they may provider a vfs
 * implementation for there {@link IMediaFile} and {@link IMediaFolder}
 * implementation and they would still work with these apis and be navigatable
 * and playable via the UI
 * 
 * @author seans
 * 
 */
@API(group = "media")
public class MediaAPI {
	private final Logger log = Logger.getLogger(MediaAPI.class);

	private static class MetadataResolver implements IVariableResolver<IMediaFile> {
		private Map<String, SageProperty> props = MetadataUtil.getProperties(IMetadata.class);

		@Override
		public String resolve(IMediaFile file, String varName) {
			if (file == null || file.getMediaObject() == null || varName == null) {
				return null;
			}
			IMetadata md = file.getMetadata();
			SageProperty key = props.get(varName);
			if (key == null) {
				Loggers.LOG.warn("MetadataResolver: Not a valid property name " + varName);
				return null;
			}

			String s = md.get(key);
			// sometimes the Title is blank for, so we'll use the EpisodeName
			if (StringUtils.isEmpty(s) && "EpisodeName".equals(varName)) {
				s = md.get(props.get("Title"));
			}

			return s;
		}
	}

	private MetadataResolver metadataResolver = new MetadataResolver();

	/**
	 * Given the media file, return a formatted title. The title is formatted
	 * according to several different criteria. A title can have a title mask if
	 * it's a Recording, TV Show, Video or Multi-CD video.
	 * 
	 * Title Masks are processed by the {@link TextReplacement} tool and masks
	 * can contain formatting instructions, based on {@link MessageFormat},
	 * {@link SimpleDateFormat}, or {@link DecimalFormat}.
	 * 
	 * Media Masks can be configured using the {@link MediaConfiguration} fields
	 * 
	 * @param mediaFile
	 *            {@link IMediaFile} or native SageTV media file object
	 * @return
	 */
	public String GetFormattedTitle(Object mediaFile) {
		IMediaFile mf = GetMediaFile(mediaFile);
		if (mf == null)
			return null;

		String title = null;

		try {
			MediaType mt = null;
			MediaConfiguration cfg = GroupProxy.get(MediaConfiguration.class);
			String mask = null;
			if (mf.isType(MediaResourceType.RECORDING.value()) || mf.isType(MediaResourceType.EPG_AIRING.value())) {
				if (MetadataUtil.isRecordedMovie(mf)) {
					mask = cfg.getMovieTitleMask();
					mt = MediaType.MOVIE;
				} else {
					mask = cfg.getRecordingTitleMask();
					mt = MediaType.TV;
				}
			} else if (mf.isType(MediaResourceType.TV.value())) {
				if (mf.getMetadata().getDiscNumber() > 0) {
					mask = cfg.getTvTitleMaskMultiCD();
				} else {
					mask = cfg.getTvTitleMask();
				}
				mt = MediaType.TV;
			} else {
				if (mf.getMetadata().getDiscNumber() > 0) {
					mask = cfg.getMovieTitleMaskMultiCD();
				} else {
					mask = cfg.getMovieTitleMask();
				}
				mt = MediaType.MOVIE;
			}

			if (mask == null) {
				log.warn("Failed to get a title mask for " + mediaFile);
				return sagex.phoenix.util.StringUtils.fixTitle(mf.getTitle());
			}

			if (mt == MediaType.MOVIE) {
				if (mf.getMetadata().getYear() <= 1800) {
					log.warn("Invalid year for Movie, so ignoring Title Mask for " + mediaFile);
					if (!StringUtils.isEmpty(mf.getMetadata().getEpisodeName())) {
						return sagex.phoenix.util.StringUtils.fixTitle(mf.getMetadata().getEpisodeName());
					} else {
						// just return the title
						return sagex.phoenix.util.StringUtils.fixTitle(mf.getTitle());
					}
				}
			}

			if (mt == MediaType.TV) {
				if (mf.getMetadata().getSeasonNumber() <= 0) {
					log.warn("Invalid Season for TV, so ignoring Title Mask for " + mediaFile);

					// use the recording mask, because the recording mask uses
					// title
					// and episode name
					mask = cfg.getRecordingTitleMask();
					// return mf.getTitle();
				}
			}

			title = TextReplacement.replaceVariables(mask, mf, metadataResolver);
		} catch (Throwable t) {
			log.warn("GetFormattedTitle failed for " + mediaFile, t);
			title = mf.getTitle();
		}

		return sagex.phoenix.util.StringUtils.fixTitle(title);
	}

	/**
	 * Given a mediaFile (or any type) return a native Sage MediaFile object. If
	 * the input is a sage media object, then it is returned. If it's not, then
	 * the object is converted into a sage media file, and then returned.
	 * 
	 * @param mediaFile
	 * @return sage's native media object, or null if this is not a native
	 *         sagetv mediafile
	 */
	public Object GetSageMediaFile(Object mediaFile) {
		if (mediaFile == null)
			return null;

		if (mediaFile instanceof SageMediaFile) {
			return ((SageMediaFile) mediaFile).getMediaObject();
		} else if (mediaFile instanceof ViewItem) {
			return GetSageMediaFile(((ViewItem) mediaFile).getDecoratedItem());
		} else if (sagex.api.MediaFileAPI.IsMediaFileObject(mediaFile) || sagex.api.AiringAPI.IsAiringObject(mediaFile)) {
			return mediaFile;
		} else if (mediaFile instanceof IMediaFile) {
			File f = PathUtils.getFirstFile((IMediaFile) mediaFile);
			if (f != null) {
				return MediaFileAPI.GetMediaFileForFilePath(f);
			}
		} else if (mediaFile instanceof File) {
			return MediaFileAPI.GetMediaFileForFilePath((File) mediaFile);
		} else if (mediaFile instanceof Integer) {
			Object o = MediaFileAPI.GetMediaFileForID((Integer) mediaFile);
			if (o == null) {
				o = AiringAPI.GetAiringForID((Integer) mediaFile);
			}
			return o;
		}

		if (log.isDebugEnabled()) {
			log.warn("GetSageMediaFile() failed for: " + mediaFile);
		}
		return null;
	}

	/**
	 * Returns the FileSystem File object for this media file. In the event that
	 * there are multiple files, then only the first one is returned.
	 * 
	 * @param mediaFile
	 *            Sage MediaFile of VFS MediaFile
	 * @return File if one exists.
	 */
	public File getFileSystemMediaFile(Object mediaFile) {
		if (mediaFile instanceof IMediaFile) {
			return PathUtils.getFirstFile((IMediaFile) mediaFile);
		} else if (MediaFileAPI.IsMediaFileObject(mediaFile)) {
			return MediaFileAPI.GetFileForSegment(mediaFile, 0);
		} else if (mediaFile instanceof File) {
			return (File) mediaFile;
		}

		log.warn("Failed to get File System File for media object: " + mediaFile);
		return null;
	}

	/**
	 * Given the mediafile return a {@link IMediaResource}. The media resource
	 * may be a file or folder. If the object is not a {@link IMediaResource}
	 * then it is converted into a {@link IMediaResource} and then returned.
	 * 
	 * @param mediaFile
	 * @return
	 */
	public IMediaResource GetMediaResource(Object mediaFile) {
		if (mediaFile instanceof IMediaResource) {
			return (IMediaResource) mediaFile;
		} else if (sagex.api.MediaFileAPI.IsMediaFileObject(mediaFile) || sagex.api.AiringAPI.IsAiringObject(mediaFile)) {
			return (new SageMediaFile(null, mediaFile));
		}
		return null;
	}

	/**
	 * Given the object return it as a {@link IMediaFile} object. If the object
	 * cannot be converted to a {@link IMediaFile}, then null is returned.
	 * 
	 * @param mediaFile
	 * @return
	 */
	public IMediaFile GetMediaFile(Object mediaFile) {
		if (mediaFile instanceof IMediaFile) {
			return (IMediaFile) mediaFile;
		} else if (sagex.api.MediaFileAPI.IsMediaFileObject(mediaFile)) {
			return (new SageMediaFile(null, mediaFile));
		} else if (AiringAPI.IsAiringObject(mediaFile)) {
			return (new SageMediaFile(null, mediaFile));
		} else if (mediaFile instanceof Integer) {
			Object mf = MediaFileAPI.GetMediaFileForID((Integer) mediaFile);
			if (mf != null) {
				return new SageMediaFile(null, mf);
			}
		}

		if (mediaFile != null) {
			log.warn("Failed to Create IMediaFile object from " + mediaFile);
		}
		return null;
	}

	/**
	 * returns the Album for the Given Media File
	 * 
	 * @param album
	 *            MediaFile Object
	 * @return
	 */
	public IAlbumInfo GetAlbum(Object album) {
		if (album == null)
			return null;
		if (album instanceof IAlbumInfo) {
			return (IAlbumInfo) album;
		}

		if (album instanceof IMediaFile) {
			return ((IMediaFile) album).getAlbumInfo();
		}

		return null;
	}

	/**
	 * return true if the media type is one of the ones listed in the
	 * {@link MediaResourceType} constants
	 * 
	 * @param type
	 *            {@link MediaResourceType} contant as a String
	 * 
	 * @return true if the media has the given type
	 */
	public boolean IsMediaType(Object file, String type) {
		if (file == null)
			return false;
		MediaResourceType rt = MediaResourceType.toMediaResourceType(type);
		if (rt == null)
			return false;

		IMediaResource r = GetMediaResource(file);
		if (r != null) {
			return r.isType(rt.value());
		}
		return false;
	}

	/**
	 * returns true if the media is an Online Video type
	 * 
	 * @param file
	 * @return
	 */
	public boolean IsOnlineVideo(Object file) {
		return IsMediaType(file, MediaResourceType.ONLINE.name());
	}

	/**
	 * Returns true if the media file is a Dummy node, ie, cannot be played,
	 * just informational placeholder.
	 * 
	 * @param file
	 * @return
	 */
	public boolean IsDummyVideo(Object file) {
		return IsMediaType(file, MediaResourceType.DUMMY.name());
	}

	/**
	 * returns true if the given media file is a Playon File
	 * 
	 * @param file
	 * @return
	 */
	public boolean IsPlayonVideo(Object file) {
		File seg = phoenix.media.GetFileSystemMediaFile(file);
		if (seg == null)
			return false;
		File f = new File(seg.getParentFile(), seg.getName() + ".playon");
		return f != null && f.exists() && f.length() > 0;
	}

	public ICastMember GetCastMember(Object cm) {
		if (cm == null)
			return null;
		if (cm instanceof ICastMember) {
			return ((ICastMember) cm);
		}
		log.warn("GetCastMember(): Invalid Object Type: " + cm);
		return null;
	}

	/**
	 * Resets the custom metadata for the given media file object. This is
	 * useful when you fanart metadata is wrong and you want to forget it.
	 * 
	 * @param mediaFile
	 */
	public void ClearCustomMetadata(Object mediaFile) {
		IMediaFile mf = phoenix.media.GetMediaFile(mediaFile);
		mf.accept(new ClearCustomMetadataFieldsVisitor(), NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);
	}

	/**
	 * Given a MediaFile return the SeriesInfo, if it's been assigned.
	 * 
	 * @param mediaFile
	 * @return
	 */
	public ISeriesInfo GetSeriesInfo(IMediaFile mediaFile) {
		if (mediaFile == null)
			return null;

		boolean update = false;
		IMetadata md = mediaFile.getMetadata();

		Object seriesInfo = null;

		int id = md.getSeriesInfoID();
		if (id > 0) {
			seriesInfo = SeriesInfoAPI.GetSeriesInfoForID(String.valueOf(id));
			if (seriesInfo != null)
				return new SageSeriesInfo(seriesInfo);
		}

		// we don't have a series info yet, so udpate it when we do
		update = true;

		// Ask sagetv for it
		seriesInfo = ShowAPI.GetShowSeriesInfo(phoenix.media.GetSageMediaFile(mediaFile));
		if (seriesInfo == null) {
			// still no series info, check the provider id
			id = TVSeriesUtil.createNewSeriesInfoId(md.getMediaProviderID(), md.getMediaProviderDataID());
			seriesInfo = SeriesInfoAPI.GetSeriesInfoForID(String.valueOf(id));
		}

		if (seriesInfo != null) {
			if (update) {
				md.setSeriesInfoID(NumberUtils.toInt(SeriesInfoAPI.GetSeriesID(seriesInfo)));
			}
			return new SageSeriesInfo(seriesInfo);
		}

		// nothing found
		return null;
	}

	/**
	 * return the duration of this mediaitem
	 * 
	 * @return
	 */
	public long GetDuration(IMediaFile mf) {
		if (mf == null)
			return 0;
		return AiringAPI.GetAiringDuration(mf.getMediaObject());
	}

	/**
	 * Return the native file or url of the given mediafile.
	 * 
	 * @param file
	 * @return file/url or empty String
	 */
	public String GetNativeFile(IMediaFile file, String defaultFile) {
		if (file == null)
			return defaultFile;
		String val = null;
		if (file instanceof DecoratedMediaFile) {
			return GetNativeFile(((DecoratedMediaFile) file).getDecoratedItem(), defaultFile);
		} else if (file instanceof HasPlayableUrl) {
			val = ((HasPlayableUrl) file).getUrl();
		} else {
			File f = PathUtils.getFirstFile(file);
			if (f != null) {
				val = f.getAbsolutePath();
			}
		}

		if (val == null) {
			return defaultFile;
		}

		return val;
	}

	/**
	 * Returns the Native File/Url, but only a path containing the last maxlen
	 * characters.
	 * 
	 * @param file
	 * @param defaultFile
	 * @param maxlen
	 * @return
	 */
	public String GetNativeFile(IMediaFile file, String defaultFile, int maxlen) {
		String f = GetNativeFile(file, defaultFile);
		if (f != null && f.length() > maxlen) {
			return f.substring(f.length() - maxlen);
		}
		return f;
	}

	/**
	 * Returns all child files from the given folder and it's subfolders
	 * 
	 * @param folder
	 * @return
	 */
	public List GetAllChildren(IMediaFolder folder) {
		return GetAllChildren(folder, Integer.MAX_VALUE, MediaResourceType.FILE.name());
	}

	/**
	 * Returns all child files from the given folder and it's subfolders but
	 * only for a max number of items
	 * 
	 * @param folder
	 * @param max
	 *            max number of items to return
	 * @return
	 */
	public List GetAllChildren(IMediaFolder folder, int max) {
		return GetAllChildren(folder, max, MediaResourceType.FILE.name());
	}

	/**
	 * Returns a list of all children matching the {@link MediaResourceType} for
	 * a max number of results.
	 * 
	 * @param folder
	 *            {@link IMediaFolder} to scan
	 * @param max
	 *            max number of results
	 * @param type
	 *            {@link MediaResourceType}
	 * @return list of resources
	 */
	public List GetAllChildren(IMediaFolder folder, int max, String type) {
		if (folder == null)
			return null;

		// don't process online videos
		if (folder.isType(MediaResourceType.ONLINE.value())) {
			return folder.getChildren();
		}

		// collect all files
		CollectorResourceVisitor crv = new CollectorResourceVisitor(max, MediaResourceType.toMediaResourceType(type));
		folder.accept(crv, new BasicProgressMonitor(), IMediaResource.DEEP_UNLIMITED);
		return crv.getCollection();
	}

	/**
	 * Returns the path relative to the root folder, whereas GetPath() return
	 * the complete path including the root folder name
	 * 
	 * @param res
	 * @param relativeToRoot
	 * @return
	 */
	public String GetPath(IMediaResource res, boolean relativeToRoot) {
		String path = res.getPath();
		if (path == null || !relativeToRoot) {
			return path;
		}
		// skip first char, since it is always a /
		int pos = path.indexOf('/', 1);
		if (pos == -1)
			return null; // is it's ourself, so we are not relative to ourself
		return path.substring(pos);
	}

	/**
	 * Safely gets the count of items in a Video Folder. For online videos this
	 * will NOT force the children to be loaded, and the size will return 0 if
	 * the files have not been loaded
	 * 
	 * @param folder
	 * @return
	 */
	public int GetCount(IMediaFolder folder) {
		if (folder == null)
			return 0;

		if (folder instanceof OnlineViewFolder) {
			return ((OnlineViewFolder) folder).count();
		}

		if (phoenix.umb.IsOnlineFolder(folder)) {
			return 0;
		}

		return folder.getChildren().size();
	}
}
