package sagex.phoenix.metadata;

import sagex.phoenix.metadata.proxy.SageProperty;

import java.util.Date;
import java.util.List;

/**
 * Sage Metadata that represents the ShowInfo for a given TV show.
 *
 * @author seans
 */
public interface ISageShowInfo extends ISageMetadata {
    @SageProperty(value = "Actor", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
    public List<ICastMember> getActors();

    @SageProperty("Category")
    public String getCategory();

    @SageProperty("Category")
    public void setCategory(String cat);

    @SageProperty("SubCategory")
    public String getSubCategory();

    @SageProperty("SubCategory")
    public void setSubCategory(String cat);

    @SageProperty("Title")
    public String getTitle();

    @SageProperty("Title")
    public void setTitle(String title);

    @SageProperty("Description")
    public String getDescription();

    @SageProperty("Description")
    public void setDescription(String description);

    @SageProperty("OriginalAirDate")
    public Date getOriginalAirDate();

    @SageProperty("OriginalAirDate")
    public void setOriginalAirDate(Date date);

    @SageProperty("TimeSlot")
    public Date getTimeSlot();

    @SageProperty("TimeSlot")
    public void setTimeSlot(Date time);

    @SageProperty("Network")
    public String getNetwork();

    @SageProperty("Network")
    public void setNetwork(String network);
}
