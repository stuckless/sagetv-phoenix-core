package sagex.phoenix.vfs.sorters;

import sagex.api.AiringAPI;
import sagex.api.ChannelAPI;
import sagex.api.Database;
import sagex.api.FavoriteAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Sorts based on channel nummber - anything without a channel number are unsorted at the bottom of
 * this sort
 *
 * @author bialio 
 */
public class ChannelNumberSorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<String> sortedChannels;
    StartTimeSorter secondarySort = new StartTimeSorter();

    public ChannelNumberSorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        int p1 = getChannelIndex(o1);
        int p2 = getChannelIndex(o2);

        if (p1 == p2) {
            // they are the same channel, sort by start time
            // return the opposite of the actual result to get the
            // list in the right order
            return (secondarySort.compare(o1, o2)) * -1;
        }
        return p1 - p2;
    }

    private int getChannelIndex(IMediaResource o) {

        if (sortedChannels == null) {
            Object channels[] = (Object[]) Database.Sort(ChannelAPI.GetAllChannels(), false, "ChannelNumber");
            sortedChannels = new ArrayList<String>();

            for (Object channel : channels){
                sortedChannels.add(ChannelAPI.GetChannelNumber(channel));
            }
        }

        if (sortedChannels.isEmpty()) {
            return -1;
        }

        if (o instanceof IMediaFile) {
            String theChannel = AiringAPI.GetAiringChannelNumber(o.getMediaObject());

            if (theChannel == null || theChannel.isEmpty()) {
                // no channel number, put it at the bottom
                return -1;
            }
            return sortedChannels.indexOf(theChannel);
        }

        if (o instanceof IMediaFolder) {
            // Folders don't have channel numbers.  But we'll move them to the top
            return 1;
        }

        // If it's not a File or Folder just return a -1
        return -1;
    }
}