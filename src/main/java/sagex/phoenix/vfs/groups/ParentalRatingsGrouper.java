package sagex.phoenix.vfs.groups;

import org.apache.commons.lang.StringUtils;
import sagex.phoenix.util.Utils;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates Groups based on the Parental Controls
 *
 * @author sean
 */
public class ParentalRatingsGrouper implements IGrouper, IMultiGrouper {

    public String getGroupName(IMediaResource res) {
        String genre = null;
        if (res instanceof IMediaFile) {
            String p1 = ((IMediaFile) res).getMetadata().getRated();
            String p2 = ((IMediaFile) res).getMetadata().getParentalRating();
            return Utils.returnNonNull(p1, p2);
        }
        return genre;
    }

    @Override
    public List<String> getGroupNames(IMediaResource res) {
        if (res instanceof IMediaFile) {
            List<String> ratings = new ArrayList<String>();
            String p1 = ((IMediaFile) res).getMetadata().getRated();
            String p2 = ((IMediaFile) res).getMetadata().getParentalRating();
            if (!StringUtils.isEmpty(p1))
                ratings.add(p1);
            if (!StringUtils.isEmpty(p2))
                ratings.add(p2);
            return ratings;
        }
        return Collections.emptyList();
    }
}
