package sagex.phoenix.metadata.provider.htb;

import java.util.ArrayList;

import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.search.MediaSearchResult;

public class HTBSearchResult extends MediaSearchResult {
	private static final long serialVersionUID = 1L;

	private ArrayList<MediaArt> artwork = new ArrayList<MediaArt>();
	
	public HTBSearchResult() {
	}

	public HTBSearchResult(String providerId, MediaType type, float score) {
		super(providerId, type, score);
	}

	public HTBSearchResult(MediaType type, String providerId, String id, String title, int year, float score) {
		super(type, providerId, id, title, year, score);
	}

	public HTBSearchResult(IMetadataSearchResult result) {
		super(result);
	}

	public ArrayList<MediaArt> getArtwork() {
		return artwork;
	}
}
