package sagex.phoenix.metadata.fixes;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.visitors.FileVisitor;

/**
 * Sets SageTV compatible ratings for both MPAA Ratings an TV ParentalRatings
 * for media files. Ratings are looked up from the ratings.properties
 *
 * @author sean
 */
public class FixParentalRatingsVisitor extends FileVisitor {
    public FixParentalRatingsVisitor() {
    }

    @Override
    public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
        if (res.isType(MediaResourceType.ANY_VIDEO.value())) {
            IMetadata md = res.getMetadata();
            log.debug("Updating Ratings for " + res.getTitle() + " " + md.getEpisodeName());
            monitor.setTaskName("Updating Ratings for " + res.getTitle() + " " + md.getEpisodeName());

            if (fixParentalRating(res, md)) {
                incrementAffected();
            }
        }

        return true;
    }

    /**
     * return true if the metadata was fixed
     *
     * @param res
     * @param md
     * @return
     */
    public static boolean fixParentalRating(IMediaFile res, IMetadata md) {
        boolean updated = false;
        if (MediaType.TV.sageValue().equals(md.getMediaType())) {
            String rating = md.getParentalRating();
            if (StringUtils.isEmpty(rating)) {
                rating = md.getRated();
            }

            String mapped = Phoenix.getInstance().getRatingsManager().getRating(MediaType.TV, rating);
            if (mapped != null && !mapped.equals(rating)) {
                md.setParentalRating(mapped);
                updated = true;
            }

            // clear the rated field
            md.setRated(null);
        } else if (MediaType.MOVIE.sageValue().equals(md.getMediaType())) {
            String rating = md.getRated();
            if (StringUtils.isEmpty(rating)) {
                rating = md.getParentalRating();
            }

            String mapped = Phoenix.getInstance().getRatingsManager().getRating(MediaType.MOVIE, rating);
            if (mapped != null && !mapped.equals(rating)) {
                md.setRated(mapped);
                updated = true;
            }

            // clear the parental rated field
            md.setParentalRating(null);
        }
        return updated;
    }
}
