package sagex.phoenix.vfs.trailers;

import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public class AppleTrailerItem extends VirtualMediaFile {
    public AppleTrailerItem(IMediaFolder parent, JSONObject json) {
        super(parent, json.optString("location"), json, json.optString("title"));

        // poster, download this locally
        // maybe create a "CachedImageResource" that downloads the jpg in the
        // background
        setThumbnail(json.optString("poster"));
    }

    @Override
    protected IMetadata createMetadata() {
        IMetadata md = super.createMetadata();

        JSONObject json = (JSONObject) getMediaObject();
        md.setEpisodeName(json.optString("title"));
        md.setRated(json.optString("rating"));
        md.setOriginalAirDate(DateUtils.parseDate(json.optString("releasedate")));

        if (md.getOriginalAirDate() != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(md.getOriginalAirDate());
            md.setYear(c.get(Calendar.YEAR));
        }

        JSONArray ja = json.optJSONArray("genre");
        if (ja != null && ja.length() > 0) {
            for (int i = 0; i < ja.length(); i++) {
                md.getGenres().add(ja.optString(i));
            }
        }

        ja = json.optJSONArray("directors");
        if (ja != null && ja.length() > 0) {
            for (int i = 0; i < ja.length(); i++) {
                md.getDirectors().add(new CastMember(ja.optString(i), null));
            }
        }

        ja = json.optJSONArray("actors");
        if (ja != null && ja.length() > 0) {
            for (int i = 0; i < ja.length(); i++) {
                md.getActors().add(new CastMember(ja.optString(i), null));
            }
        }

        return md;
    }

    @Override
    public boolean isType(int type) {
        if (type == MediaResourceType.ONLINE.value()) {
            return true;
        } else if (type == MediaResourceType.FILE.value()) {
            return true;
        } else if (type == MediaResourceType.VIDEO.value()) {
            return true;
        } else {
            return super.isType(type);
        }
    }

    @Override
    protected List<File> createFiles() {
        // TODO Auto-generated method stub
        return super.createFiles();
    }
}
