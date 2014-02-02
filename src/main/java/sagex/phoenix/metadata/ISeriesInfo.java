package sagex.phoenix.metadata;

import java.util.List;

import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.tools.annotation.API;

/**
 * Represents the Series Info for a given TV Show
 * 
 * @author seans
 */
@API(group = "series", proxy = true)
public interface ISeriesInfo {
	/**
	 * This is the SageTV SeriesInfoID field
	 * 
	 * @return
	 */
	@SageProperty("SeriesInfoID")
	public String getSeriesInfoID();

	@SageProperty("SeriesInfoID")
	public void setSeriesInfoID(String id);

	@SageProperty("Title")
	public String getTitle();

	@SageProperty("Title")
	public void setTitle(String title);

	@SageProperty("Network")
	public String getNetwork();

	@SageProperty("Network")
	public void setNetwork(String network);

	@SageProperty("Description")
	public String getDescription();

	@SageProperty("Description")
	public void setDescription(String desc);

	@SageProperty("History")
	public String getHistory();

	@SageProperty("History")
	public void setHistory(String history);

	@SageProperty("PremiereDate")
	public String getPremiereDate();

	@SageProperty("PremiereDate")
	public void setPremiereDate(String date);

	@SageProperty("FinaleDate")
	public String getFinaleDate();

	@SageProperty("FinaleDate")
	public void setFinaleDate(String date);

	@SageProperty("AirDOW")
	public String getAirDOW();

	@SageProperty("AirDOW")
	public void setAirDOW(String airdow);

	@SageProperty("AirHrMin")
	public String getAirHrMin();

	@SageProperty("AirHrMin")
	public void setAirHrMin(String airtime);

	@SageProperty("Image")
	public Object getImage();

	@SageProperty("Image")
	public void setImage(String url);

	// TODO: New type of array that binds 2 different properties to a single
	// list, separating out the people and charaters into 2 fields
	@SageProperty(value = "People;Characters", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getCast();

	// NON-Sage Fields
	@SageProperty(value = "UserRating")
	public int getUserRating();

	@SageProperty(value = "UserRating")
	public void setUserRating(int rating);

	@SageProperty(value = "ContentRating")
	public String getContentRating();

	@SageProperty(value = "ContentRating")
	public void setContentRating(String rating);

	@SageProperty(value = "Genre", listFactory = "sagex.phoenix.metadata.proxy.GenrePropertyListFactory")
	public List<String> getGenres();

	@SageProperty(value = "Runtime")
	public long getRuntime();

	@SageProperty(value = "Runtime")
	public void setRuntime(long runtime);

	@SageProperty(value = "Zap2ItID")
	public String getZap2ItID();

	@SageProperty(value = "Zap2ItID")
	public void setZap2ItID(String id);
}
