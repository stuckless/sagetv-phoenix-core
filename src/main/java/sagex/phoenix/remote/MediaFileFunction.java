package sagex.phoenix.remote;

import org.apache.commons.lang.math.NumberUtils;

import sagex.api.MediaFileAPI;
import sagex.phoenix.util.Function;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.sage.SageMediaFile;

public class MediaFileFunction implements Function<String, IMediaFile> {
    public MediaFileFunction() {
    }

    @Override
    public IMediaFile apply(String in) {
        int sageid = NumberUtils.toInt(in);
        Object mf = MediaFileAPI.GetMediaFileForID(sageid);
        if (mf != null) {
            return new SageMediaFile(null, mf);
        }
        return null;
    }
}
