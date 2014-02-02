package sagex.phoenix.vfs.impl;

import java.util.List;

import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Exposed SageTV Metadata as an AlbumInfo object
 * 
 * @author seans
 */
public class MetadataAlbumnInfo implements IAlbumInfo {
	IMediaFile file = null;

	public MetadataAlbumnInfo(IMediaFile file) {
		this.file = file;
	}

	@Override
	public Object getArt() {
		return null;
	}

	private String safeGetFirstString(List<String> list) {
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public String getArtist() {
		List<ICastMember> cmlist = file.getMetadata().getAlbumArtists();
		if (cmlist == null || cmlist.size() == 0) {
			cmlist = file.getMetadata().getArtists();
		}
		if (cmlist != null && cmlist.size() > 0) {
			return cmlist.get(0).getName();
		}
		return null;
	}

	@Override
	public String getGenre() {
		return safeGetFirstString(file.getMetadata().getGenres());
	}

	@Override
	public String getName() {
		return file.getMetadata().getAlbum();
	}

	@Override
	public List<IMediaFile> getTracks() {
		return null;
	}

	@Override
	public String getYear() {
		int y = file.getMetadata().getYear();
		if (y > 0) {
			return String.valueOf(y);
		}
		return null;
	}

	@Override
	public boolean hasArt() {
		return false;
	}
}
