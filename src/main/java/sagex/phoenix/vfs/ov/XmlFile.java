package sagex.phoenix.vfs.ov;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualOnlineMediaFile;
import sagex.phoenix.vfs.impl.MetadataAlbumnInfo;

public class XmlFile extends VirtualOnlineMediaFile {
    static long idcounter = 0;

    public XmlFile(IMediaFolder parent) {
        super(parent, "TMP-" + (idcounter++));
    }

    private boolean hasTitle() {
        return !(StringUtils.isEmpty(getTitle()) || getTitle().startsWith("TMP"));
    }

    public void updateIds() {
        if ("Music".equals(getMetadata().getMediaType())) {
            if (StringUtils.isEmpty(getMetadata().getMediaTitle())) {
                getMetadata().setMediaTitle(
                        sagex.phoenix.util.StringUtils.firstNonEmpty(getMetadata().getRelativePathWithTitle(), getMetadata()
                                .getEpisodeName(), getAlbumInfo().getName()));
            }
        }

        // take the song title from episode name
        if (!hasTitle()) {
            setTitle(sagex.phoenix.util.StringUtils.firstNonEmpty(getMetadata().getRelativePathWithTitle(), getMetadata()
                    .getEpisodeName(), getMetadata().getMediaTitle()));
        }

        if (StringUtils.isEmpty(getTitle())) {
            setTitle("ERROR: No Title");
        }

        if (StringUtils.isEmpty(getMetadata().getMediaTitle())) {
            getMetadata().setMediaTitle(getTitle());
        }

        if (StringUtils.isEmpty(getMetadata().getEpisodeName())) {
            getMetadata().setEpisodeName(getTitle());
        }

        if (StringUtils.isEmpty(getMetadata().getRelativePathWithTitle())) {
            getMetadata().setRelativePathWithTitle(getTitle());
        }
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    protected IAlbumInfo createAlbumInfo() {
        return new MetadataAlbumnInfo(this);
    }

    public static XmlFile newMusicFile(IMediaFolder parent, String artist, String song) {
        XmlFile file = new XmlFile(parent);
        file.setTitle(song);
        file.getMetadata().setMediaType(MediaType.MUSIC.sageValue());
        file.getMetadata().set(MetadataUtil.getSageProperty(FieldName.Artist), artist);
        return file;
    }

    public boolean isType(int type) {
        MediaType mediaType = MediaType.toMediaType(getMetadata().getMediaType(), MediaType.MOVIE);
        MediaResourceType rtype = MediaResourceType.toMediaResourceType(type);
        if (rtype == null)
            return false;

        if (rtype == MediaResourceType.FILE) {
            return true;
        } else if (rtype == MediaResourceType.ONLINE) {
            return true;
        } else if (rtype == MediaResourceType.ANY_VIDEO) {
            return isType(MediaResourceType.VIDEO.value());
        } else if (rtype == MediaResourceType.VIDEO) {
            return mediaType == MediaType.MOVIE || mediaType == MediaType.TV;
        } else if (rtype == MediaResourceType.TV) {
            return mediaType == MediaType.TV;
        } else if (rtype == MediaResourceType.MUSIC) {
            return mediaType == MediaType.MUSIC;
        }

        return super.isType(type);
    }
}