package sagex.phoenix.vfs.sources;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.ov.XmlFolder;
import sagex.phoenix.vfs.ov.XmlOptions;
import sagex.phoenix.vfs.ov.XmlSourceFactory;
import sagex.phoenix.vfs.util.ConfigList;

import java.util.Set;

/**
 * Convenience source for configuring a base {@link XmlSourceFactory} for either
 * RSS or Atom feeds
 *
 * @author sls
 */
public class RSSFeedFactory extends Factory<IMediaFolder> {
    public RSSFeedFactory() {
        super();
        addOption(new ConfigurableOption("feedurl", "RSS Feed Url", null, DataType.string));
        addOption(new ConfigurableOption("mediatype", "Feed's Media Type", "Movie", DataType.string, true, ListSelection.single,
                ConfigList.mediaTypeList()));
        addOption(new ConfigurableOption("feedtype", "rss or atom feed", "rss", DataType.string, true, ListSelection.single,
                "rss:RSS,atom:Atom"));
    }

    @Override
    public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
        String feed = getOption("feedurl", configurableOptions).getString(null);
        String type = getOption("mediatype", configurableOptions).getString(null);
        String feedtype = getOption("feedtype", configurableOptions).getString(null);

        XmlOptions options = new XmlOptions();
        options.setFeedUrl(feed);
        options.setMediaType(MediaType.toMediaType(type, MediaType.MOVIE).sageValue());
        if ("atom".equals(feedtype)) {
            // adjust the options for atom feed
            options.setItemElement("entry");
            options.addMetadataOption(FieldName.MediaTitle, "im:name", null);
            options.addMetadataOption(FieldName.Description, "summary", "Description");
            options.addMetadataOption(FieldName.MediaUrl, "link@href", null);
            options.addMetadataOption(FieldName.AlbumArtist, "im:artist", "Album Artist");
            options.addMetadataOption(FieldName.OriginalAirDate, "im:releaseDate", "Aired Date");
            options.addMetadataOption(FieldName.Duration, "im:duration", "Duration");
            options.addMetadataOption(FieldName.Genre, "category@term", null);
        }

        XmlFolder folder = new XmlFolder(null, getLabel(), options);
        return folder;
    }
}
