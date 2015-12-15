package sagex.phoenix.vfs.music;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;

/**
 * A Music Mapper Factory takes 2 VFS Music views and maps an offline view to
 * files that are in your local library, it builds a new view of playable items
 * from your library. This is used to map a Top-## Chart view to files that you
 * already have in your Music Library.
 *
 * @author sls
 */
public class MusicMapperFactory extends Factory<IMediaFolder> {
    private long lastCreated = 0;
    private transient IMediaFolder view;

    public MusicMapperFactory() {
        super();
        addOption(new ConfigurableOption("offline-view", "View name of the Offline Music Files", null, DataType.string));
        addOption(new ConfigurableOption("library-view", "View name of the Libary music files", "phoenix.view.source.music",
                DataType.string));
        addOption(new ConfigurableOption("hide-offline", "Removes files from the view that could not be located in your library",
                "false", DataType.bool));
        addOption(new ConfigurableOption("cache-expiry", "Cache the view's results for the given # of minutes", "1440",
                DataType.integer));
    }

    @Override
    public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
        long expiry = getOption("cache-expiry", configurableOptions).getInt(1440) * 60 * 1000;

        if (view != null && (lastCreated + expiry) > System.currentTimeMillis()) {
            return view;
        }

        String offlineViewName = getOption("offline-view", configurableOptions).getString(null);
        String libraryViewName = getOption("library-view", configurableOptions).getString(null);
        boolean hideOffline = getOption("hide-offline", configurableOptions).getBoolean(false);

        IMediaFolder offline = phoenix.umb.CreateView(offlineViewName);
        IMediaFolder library = phoenix.umb.CreateView(libraryViewName);

        VirtualMediaFolder f = new VirtualMediaFolder(getLabel());

        for (IMediaResource r : offline) {
            if (r instanceof IMediaFile) {
                IMediaResource lib = findLibrary(library, (IMediaFile) r);
                if (lib != null) {
                    addToLibrary(f, lib);
                } else if (!hideOffline) {
                    addToLibrary(f, r);
                }
            }
        }

        // cache the results
        view = f;
        lastCreated = System.currentTimeMillis();

        return f;
    }

    private IMediaResource findLibrary(IMediaFolder library, IMediaFile toFind) {
        String toFindSong = toFind.getTitle();
        for (IMediaResource r : library) {
            if (r instanceof IMediaFile) {
                String song = r.getTitle();
                float score = MetadataSearchUtil.calculateScore(song, toFindSong);
                if (score > .95) {
                    return r;
                }
            }
        }
        return null;
    }

    private void addToLibrary(VirtualMediaFolder library, IMediaResource lib) {
        if (lib instanceof DecoratedMediaFile) {
            lib = ((DecoratedMediaFile) lib).getDecoratedItem();
        }
        library.addMediaResource(lib);
    }

    public void expireCache() {
        lastCreated = 0;
    }
}
