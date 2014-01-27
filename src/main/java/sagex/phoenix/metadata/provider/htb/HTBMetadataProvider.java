package sagex.phoenix.metadata.provider.htb;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProviderInfo;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataProvider;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.url.UrlUtil;

public class HTBMetadataProvider extends MetadataProvider {
	private static final String API_KEY = "f95b13e9fc9f655651c0789500c2a801";
	private static final String DETAIL_URL = "http://htbackdrops.com/api/{0}/searchXML?mbid={1}&aid=1,5&fields=title,keywords,caption,mb_name,mb_alias&inc=keywords,caption,mb_name,mb_aliases";
	private static final String SEARCH_URL = "http://htbackdrops.com/api/{0}/searchXML?keywords={1}&default_operator=and&aid=1,5&fields=title,keywords,caption,mb_name,mb_alias&inc=keywords,caption,mb_name,mb_aliases";
	private static final String FANART_IMAGE_URL="http://htbackdrops.com/api/{0}/download/{1}/fullsize";
	
	public HTBMetadataProvider(IMetadataProviderInfo info) {
		super(info);
	}

	@Override
	public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
		IMetadata md = MetadataUtil.createMetadata();
		if (!(result instanceof HTBSearchResult)) {
			SearchQuery q = new SearchQuery(MediaType.MUSIC, result.getTitle());
			q.set(Field.ID, result.getId());
			List<IMetadataSearchResult> results = search(q);
			if (results.size()==1) {
				result = results.get(0);
			} else {
				throw new MetadataException("Failed to get match for result " + result);
			}
		}

		md.getFanart().addAll(((HTBSearchResult)result).getArtwork());
		md.setMediaTitle(result.getTitle());
		md.setMediaType(MediaType.MUSIC.sageValue());
		md.setMediaProviderID(getInfo().getId());
		md.setMediaProviderDataID(result.getId());
		return md;
	}

	@Override
	public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
		String url = null;
		String id = query.get(Field.ID);
		
		String artist = query.get(SearchQuery.Field.ARTIST);
		if (StringUtils.isEmpty(id)) {
			if (StringUtils.isEmpty(artist)) throw new MetadataException("Missing ARTIST for Query: " + query);
			url = getSearchUrl(artist);
		} else {
			url = getDetailUrl(id);
			artist=null;
		}
		
		HTBParser parser = new HTBParser(url, artist, this);
		return new ArrayList<IMetadataSearchResult>(parser.getResults());
	}
	
	public String getSearchUrl(String keywords) {
		return MessageFormat.format(SEARCH_URL, API_KEY, UrlUtil.encode(keywords));
	}
	
	public String getFanartDownloadUrl(String imgId) {
		return MessageFormat.format(FANART_IMAGE_URL, API_KEY, imgId);
	}

	public String getDetailUrl(String mbId) {
		return MessageFormat.format(DETAIL_URL, API_KEY, mbId);
	}
}
