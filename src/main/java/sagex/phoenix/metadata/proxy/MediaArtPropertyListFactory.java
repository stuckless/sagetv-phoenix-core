package sagex.phoenix.metadata.proxy;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;

public class MediaArtPropertyListFactory extends StringPropertyListFactory {
	public MediaArtPropertyListFactory() {
		super(";");
	}

	public MediaArtPropertyListFactory(String listItemSeparator) {
		super(listItemSeparator);
	}

	@Override
	public Object decode(String item) {
		if (item==null) return null;
		String parts[] = item.split("\\s*\\|\\s*");
		if (parts==null||parts.length<3) return null;
		MediaArt ma = new MediaArt();
		ma.setSeason(NumberUtils.toInt(parts[0]));
		ma.setType(MediaArtifactType.toMediaArtifactType(parts[1]));
		ma.setDownloadUrl(parts[2]);
		return ma;
	}

	@Override
	public String encode(Object item) {
		if (item==null) return "";
		IMediaArt ma = (IMediaArt) item;
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(ma.getSeason())).append("|").append(ma.getType()).append("|").append(ma.getDownloadUrl());
		return sb.toString();
	}
}
