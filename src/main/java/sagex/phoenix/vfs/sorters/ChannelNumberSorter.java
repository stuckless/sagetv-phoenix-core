package sagex.phoenix.vfs.sorters;

import sagex.api.AiringAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sorts based on the Channel number of the media file in question
 *
 * @author bialio
 */
public class ChannelNumberSorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    public ChannelNumberSorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        if (o1 instanceof IMediaFile && o2 instanceof IMediaFile) {
            Object theairing1 = o1.getMediaObject();
            final String channel1 = AiringAPI.GetAiringChannelNumber(theairing1);

            Object theairing2 = o2.getMediaObject();
            final String channel2 = AiringAPI.GetAiringChannelNumber(theairing2);

            return majorMinorCompare(channel1, channel2);
        }

        // If we get here it's likely a folder- Folders don't have channel numbers.  But we'll move them to the top
        // by returning 1
        return 1;
    }

    private int majorMinorCompare(final String s1, final String s2) {

        // if anything is empty or null return 0
        if (s1 == null || s1.isEmpty() || s2 == null || s2.isEmpty()) {
            return 0;
        }
        // split each string into arrays of strings that are just digits
        String[] s1_nums = s1.split("\\D+");
        String[] s2_nums = s2.split("\\D+");

        final int s1_major = s1_nums.length > 0 ? Integer.parseInt(s1_nums[0]) : -1;
        final int s1_minor = s1_nums.length > 1 ? Integer.parseInt(s1_nums[1]) : -1;

        final int s2_major = s2_nums.length > 0 ? Integer.parseInt(s2_nums[0]) : -1;
        final int s2_minor = s2_nums.length > 1 ? Integer.parseInt(s2_nums[1]) : -1;

        if (s1_major == -1 && s2_major == -1) {
            // both don't have digits - but both have something - so do alphabetic compare
            return s1.compareToIgnoreCase(s2);
        }

        if (s1_major == -1) {
            // s2 has something and s1 doesn't - so put it before s1
            return -1;
        }

        if (s2_major == -1) {
            // s1 has something and s2 doesn't - so put s1 first
            return 1;
        }

        // we know both are not empty and both have at least the major number at this point
        switch (s1_major - s2_major) {
            case 0 :
                // these are the same major number - check for trailing digits
                if (s1_minor == -1 && s2_minor == -1) {
                    // neither has a minor digit, so they are equal
                    return 0;
                }

                if (s1_minor == -1) {
                    // s2 has a minor digit - so it goes last
                    return 1;
                }
                if (s2_minor == -1) {
                    // only s1 has a minor digit - so s1 goes last
                    return -1;
                }

                // at this point they both have a minor digit - return the comparison of those
                return s1_minor - s2_minor;

            default:
                return s1_major - s2_major;
        }
    }
}
