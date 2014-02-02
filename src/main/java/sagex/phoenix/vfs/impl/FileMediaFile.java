package sagex.phoenix.vfs.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sagex.SageAPI;
import sagex.api.AiringAPI;
import sagex.api.MediaFileAPI;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

public class FileMediaFile extends VirtualMediaFile implements IMediaFile, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * File MediaFile can only be created using the {@link FileResourceFactory},
	 * to ensure that the correct type of file is created, ie, normal, dvd,
	 * bluray, etc.
	 * 
	 * @param file
	 */
	FileMediaFile(File file) {
		this(null, file);
	}

	FileMediaFile(IMediaFolder parent, File file) {
		super(parent, file.getAbsolutePath(), file, file.getName());
		addFile(file);
		setTitle(FileResourceFactory.getRealTitle(file));
	}

	@Override
	protected String createId(Object resource) {
		if (resource instanceof File) {
			try {
				return ((File) resource).getCanonicalPath();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public List<File> createFiles() {
		return new ArrayList<File>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.vfs.impl.MediaResource#delete()
	 */
	@Override
	public boolean delete(Hints hints) {
		boolean deleted = false;
		for (File f : getFiles()) {
			FileCleaner.clean(f);
			deleted = !f.exists();
		}
		return deleted;
	}

	private File getFile() {
		if (getFiles().size() > 0) {
			return getFiles().get(0);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.vfs.impl.MediaResource#exists()
	 */
	@Override
	public boolean exists() {
		File f = getFile();
		if (f != null)
			return f.exists();
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.vfs.impl.MediaResource#lastModified()
	 */
	@Override
	public long lastModified() {
		File f = getFile();
		if (f != null)
			return f.lastModified();
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.vfs.impl.MediaResource#touch()
	 */
	@Override
	public void touch(long time) {
		for (File f : getFiles()) {
			FileToucher.touch(f, time);
		}
	}

	public boolean isType(int type) {
		MediaResourceType rtype = MediaResourceType.toMediaResourceType(type);
		if (rtype == null)
			return false;

		if (rtype == MediaResourceType.FILE) {
			return true;
		} else if (rtype == MediaResourceType.FOLDER) {
			return false;
		} else if (rtype == MediaResourceType.BLURAY) {
			return FileResourceFactory.isBluRay(PathUtils.getFirstFile(this));
		} else if (rtype == MediaResourceType.VIDEODISC) {
			return isType(MediaResourceType.DVD.value()) && isType(MediaResourceType.BLURAY.value());
		} else if (rtype == MediaResourceType.DVD) {
			return FileResourceFactory.isDVD(PathUtils.getFirstFile(this));
		} else if (rtype == MediaResourceType.HD) {
			return isType(MediaResourceType.BLURAY.value()) || AiringAPI.IsAiringHDTV(getMediaObject());
		} else if (rtype == MediaResourceType.MUSIC) {
			return FileResourceFactory.isMusicFile(PathUtils.getFirstFile(this));
		} else if (rtype == MediaResourceType.PICTURE) {
			return FileResourceFactory.isVideoFile(PathUtils.getFirstFile(this));
		} else if (rtype == MediaResourceType.RECORDING) {
			return FileResourceFactory.isRecordingFile(PathUtils.getFirstFile(this));
		} else if (rtype == MediaResourceType.TV) {
			return FileResourceFactory.isTvFile(PathUtils.getFirstFile(this)) || isType(MediaResourceType.RECORDING.value());
		} else if (rtype == MediaResourceType.VIDEO) {
			return FileResourceFactory.isVideoFile(PathUtils.getFirstFile(this));
		} else if (rtype == MediaResourceType.ANY_VIDEO) {
			return isType(MediaResourceType.VIDEO.value()) || isType(MediaResourceType.TV.value())
					|| isType(MediaResourceType.DVD.value()) || isType(MediaResourceType.BLURAY.value());
		} else if (rtype == MediaResourceType.EPG_AIRING) {
			return false;
		} else {
			log.warn("isType(" + rtype + "(" + type + ")) is unhandled.");
			return false;
		}
	}

	@Override
	protected IMetadata createMetadata() {
		if (!SageAPI.isRemote()) {
			// if we are embedded then check if this file has a sagetv
			// corresponding mediafile with metadata
			Object sageFile = MediaFileAPI.GetMediaFileForFilePath(getFile());
			if (sageFile != null) {
				log.debug("Using SageTV metadata for file " + getMediaObject());
				// creates a metadata object that has direct writes to the
				// wiz.bin metadata
				return MetadataUtil.createMetadata(getMediaObject());
			}
		}

		log.debug("Using read/only metadata for file " + getMediaObject());
		return super.createMetadata();
	}
}
