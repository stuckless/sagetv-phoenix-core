package phoenix.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import sagex.UIContext;
import sagex.api.Global;
import sagex.api.MediaFileAPI;
import sagex.api.MediaNodeAPI;
import sagex.api.MediaPlayerAPI;
import sagex.api.PlaylistAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.progress.NullProgressMonitor;
import sagex.phoenix.remote.Search;
import sagex.phoenix.stv.OnlineVideoPlayer;
import sagex.phoenix.stv.Toaster;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.ElapsedTimer;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.vfs.CombinedMediaFolder;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.HasPlayableUrl;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.sage.MediaFilesMediaFolder;
import sagex.phoenix.vfs.sage.SageSourcesMediaFolder;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import sagex.phoenix.vfs.visitors.SearchVisitor;

/**
 * MediaBrowserAPI is a set of APIs for Browsing and Playing Media Files.
 * <p/>
 * The Basic concept of the MediaBrowser is that media files are organized into
 * Folders. Once you have a folder, you can then navigate the folder's children,
 * which may be other folders, or media files.
 * <p/>
 * Folders can be filtered, grouped, and sorted, and these operations can be
 * nested in unlimited ways.
 * <p/>
 * For example, you could get a TV folder, and then Group it by Title, then Sort
 * it, and then Group it again by Season.
 * <p/>
 * Filter, Groups, and Sorts will apply itself to child folders if it's set at
 * the parent. ie, if you have a folder and set a Filter, then that filter will
 * also be applied to all child folders of the main folder recurcively.
 *
 * @author seans
 */
@API(group = "umb")
public class MediaBrowserAPI {
    private Logger log = Logger.getLogger(MediaBrowserAPI.class);

    /**
     * Safe way to get a ViewFolder from anothe object. Probably never needs to
     * be called externally, but it is used internally to ensure that the object
     * being acted upon is a view folder
     *
     * @param folder ViewFolder or IMediaFolder instance
     * @return ViewFolder or null if the Object is not a ViewFolder or cannot be
     * turned into a ViewFolder
     */
    public ViewFolder GetView(Object folder) {
        if (folder instanceof ViewFolder) {
            return (ViewFolder) folder;
        } else if (folder instanceof IMediaFolder) {
            return new ViewFolder(new ViewFactory(), 0, null, (IMediaFolder) folder);
        } else if (folder instanceof String) {
            return phoenix.umb.CreateView((String) folder, null);
        } else {
            log.warn("Can't convert to ViewFolder: " + folder);
            return null;
        }
    }

    /**
     * Returns an array of Sage Objects as a Media Folder. (Internal Use)
     *
     * @param sageObjects
     * @param label
     * @return
     */
    public IMediaFolder GetMediaAsFolder(Object[] sageObjects, String label) {
        return new MediaFilesMediaFolder(null, sageObjects, label);
    }

    /**
     * Returns an array of Sage Objects as a Media Folder. (Internal Use)
     *
     * @param sageObjects
     * @param label
     * @return
     */
    public IMediaFolder GetMediaAsFolder(List sageObjects, String label) {
        return new MediaFilesMediaFolder(null, sageObjects.toArray(), label);
    }

    /**
     * Given the SageTV media mask, return the resulting media items as a
     * folder. The media mask is defined in the sagetv api for
     * GetMediaFiles(mediaMask), but basically, "B" - bluray, "D" - DVD, "T" -
     * TV, "V" videos, "M" - music, "P" pictures, "L" - library.
     * <p/>
     * Internal Use
     *
     * @param mediaMask
     * @param label
     * @return
     */
    public IMediaFolder GetSageMediaFiles(String mediaMask, String label) {
        if (mediaMask == null) {
            return GetMediaAsFolder(MediaFileAPI.GetMediaFiles("DBVL"), label);
        } else {
            return GetMediaAsFolder(MediaFileAPI.GetMediaFiles(mediaMask), label);
        }
    }

    /**
     * Given an object, try to play it. If the object is a Sage MediaFile, then
     * it will be played as is. If the object is a {@link IMediaFile} then it
     * will be played as a regular media item. If the object is a
     * {@link IMediaFolder} (ie, maybe a grouped folder), then it will be played
     * as a Playlist.
     * <p/>
     * Although it's not implemented yet, but if you pass a url, then the API
     * will eventually start to download the url media object and then begin to
     * play it. Currently, it does not do this.
     * <p/>
     * <p/>
     * THIS IS FOR MY OWN REFERENCE Keyboard shortcuts... F5 = Page Up F6 = Page
     * Down F7 = Page Left F8 = Page Right {Up} = Up {Down} = Down {Left} = Left
     * {Right}=Right {Home} = Home/Main Menu {PageUp} = Channel Up/Page Up
     * {PageDown} = Channel Down/Page Down {ctrl} left arrow = Left/Volume Down
     * {ctrl} right arrow = Right/Volume Up {ctrl} up arrow = Up/Channel Up
     * {ctrl} down arrow = Down/Channel Down {ctrl} A Skip Bkwd/Page Left {ctrl}
     * D Play {ctrl} E Volume Down {ctrl} F = Skip Fwd/Page Right {ctrl} G =
     * Time Scroll {ctrl} I = Info {ctrl} J = Don't Like {ctrl} K = Favorite
     * {ctrl} M = Play Faster {ctrl} N = Play Slower {ctrl} O = Options {ctrl} R
     * = Volume Up {ctrl} S = Pause {ctrl} V = TV {ctrl} w = Watched {ctrl} X =
     * Guide {ctrl} Y = Record {ctrl} Z = Power {ctrl}{shift} F = Full
     * Screen/Windows Screen {ctrl}{shift} S = Play/Pause {ctrl}{shift} M = Mute
     * {pause] = Pause {alt}F4 = Exit/Close Program
     * <p/>
     * <p/>
     * CTRL+G seems to do a STOP
     *
     * @param file      - Sage MediaFile, {@link IMediaFile}, {@link IMediaFolder} or
     *                  url.
     * @param uiContext
     */
    public boolean Play(Object file, String uiContext) {
        log.info("PlayMediaFile called for: " + file + "; Context: " + uiContext);
        if (uiContext == null) {
            uiContext = Global.GetUIContextName();
            log.info("No UI Context... discovered: " + uiContext);
        }
        UIContext ctx = new UIContext(uiContext);
        if (file instanceof IMediaFile) {
            // unwrap decorated items
            if (file instanceof DecoratedMediaFile) {
                return Play(((DecoratedMediaFile) file).getDecoratedItem(), uiContext);
            }

            if (file instanceof HasPlayableUrl) {
                // create online video player
                OnlineVideoPlayer ovp = new OnlineVideoPlayer(ctx, (IMediaFile) file);
                return ovp.play();
            } else {
                return playSageFile(ctx, getSageItem(file));
            }
        } else if (file instanceof IMediaFolder) {
            log.info("Building a dynamic playlist for folder: " + file);
            // create a playlist, and play it.
            Object plist = PlaylistAPI.GetNowPlayingList(ctx);

            // remove current items
            while (PlaylistAPI.GetNumberOfPlaylistItems(ctx, plist) > 0) {
                PlaylistAPI.RemovePlaylistItemAt(ctx, plist, 0);
            }

            // for each mediafile in our folder, play it.
            for (IMediaResource r : ((IMediaFolder) file)) {
                Object o = getSageItem(r);
                if (o != null) {
                    if (o instanceof DecoratedMediaFile) {
                        // folders created on the fly (via GetMediaAsFolder())
                        // end up here.
                        // the sage playlist needs a Sage MediaFile, and won't
                        // work if it's populated
                        // with anything else.
                        Object p = ((DecoratedMediaFile) o).getMediaObject();
                        log.debug("adding " + p + " to playlist");
                        PlaylistAPI.AddToPlaylist(ctx, plist, p);
                    } else {
                        log.debug("adding " + o + " to playlist");
                        PlaylistAPI.AddToPlaylist(ctx, plist, o);
                    }
                } else {
                    log.debug("couldn't add " + r + " to playlist - getSageItem() returned null");
                }
            }

            if (PlaylistAPI.GetNumberOfPlaylistItems(ctx, plist) > 0) {
                log.debug("Playing Playlist: " + plist);
                MediaPlayerAPI.StartPlaylist(ctx, plist);
                return true;
            } else {
                Toaster.toast("Unable to play folder", 2000);
            }
        } else {
            return playSageFile(ctx, file);
        }
        return false;
    }

    private Object getSageItem(Object r) {
        Object o = phoenix.media.GetSageMediaFile(r);
        if ("MediaFile".equals(MediaNodeAPI.GetNodeDataType(o))) {
            o = MediaNodeAPI.GetNodeDataObject(o);
        }
        return o;
    }

    private Set<ConfigurableOption> getSetOptions(Object options) {
        if (options == null)
            return new TreeSet<ConfigurableOption>();
        if (options instanceof Set) {
            return (Set<ConfigurableOption>) options;
        } else {
            Set<ConfigurableOption> set = new TreeSet<ConfigurableOption>();
            Map<String, String> opts = phoenix.util.ToMap(options);
            for (Map.Entry<String, String> me : opts.entrySet()) {
                set.add(new ConfigurableOption(me.getKey(), me.getValue()));
            }
            return set;
        }
    }

    /**
     * Create a new {@link ViewFolder} containing the files from view. A view
     * contains one or more Sources that are grouped, filtered, sorted, etc.
     * <p/>
     * ViewFolders, while instances of IMediaFolder are different in that they
     * can "Operations" applied to them, such as Filtering, Sorting, and
     * Grouping.
     *
     * @param view    {@link Factory} id of the {@link IMediaFolder} view factory
     * @param options either a Map of name value pairs or a string in json map
     *                format (ie, name: "value", name2: "value )
     * @return {@link ViewFolder} instance
     */
    public ViewFolder CreateView(String view, Object options) {
        try {
            return (ViewFolder) Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactory(view)
                    .create(getSetOptions(options));
        } catch (Exception e) {
            log.warn("Failed to create View: " + view, e);
        }
        return null;
    }

    private boolean playSageFile(UIContext ctx, Object file) {
        if (file == null) {
            return false;
        }

        log.info("Playing SageTV MediaFile: " + file + " on UI Context: " + ctx + "; Title: " + MediaFileAPI.GetMediaTitle(file));

        Object watching = MediaPlayerAPI.Watch(ctx, file);
        log.info("Is Watching: " + watching);
        return true;
    }

    /**
     * Return true if the file can be handled by PlayMediaFile(). This call is
     * handy to use when you are rendering the media resources, since some media
     * resources, such as grouped resources can be played as a single item.
     *
     * @param media resource (file, folder, sage object)
     * @return
     */
    public boolean IsPlayable(Object file) {
        if (file instanceof IMediaFile) {
            IMediaFile IMF = (IMediaFile) file;
            if (IMF.isType(MediaResourceType.DUMMY.value())) {
                log.debug("IsPlayable: " + file + "; not playable for type : MISSINGTV");
                return false;
            } else if (IMF.isType(MediaResourceType.MISSINGTV.value())) {
                log.debug("IsPlayable: " + file + "; not playable for type : DUMMY");
                return false;
            }
        }
        boolean playable = (file instanceof IMediaFile) || (file instanceof HasPlayableUrl) || MediaFileAPI.IsMediaFileObject(file);
        log.debug("IsPlayable: " + file + "; " + playable);
        return playable;
    }

    /**
     * Returns true if the current resource has children.
     *
     * @param res
     * @return
     */
    public boolean HasChildren(Object res) {
        if (res instanceof IMediaFolder) {
            return ((IMediaFolder) res).getChildren().size() > 0;
        }
        return false;
    }

    /**
     * Returns true if the given object is a Media Folder
     *
     * @param res
     * @return
     */
    public boolean IsFolder(Object res) {
        return res instanceof IMediaFolder;
    }

    /**
     * Returns the # of children for the given folder
     *
     * @param res
     * @return
     */
    public int GetChildCount(Object res) {
        if (res instanceof IMediaFolder) {
            return ((IMediaFolder) res).getChildren().size();
        }
        return 0;
    }

    /**
     * Get's the child at the given offset for the folder.
     */
    public IMediaResource GetChild(IMediaFolder folder, int pos) {
        if (pos < folder.getChildren().size()) {
            return folder.getChildren().get(pos);
        } else {
            return null;
        }
    }

    /**
     * Gets the parent for the given folder
     *
     * @param res
     * @return
     */
    public IMediaFolder GetParent(IMediaResource res) {
        return res.getParent();
    }

    /**
     * Return true if the given folder has a parent.
     *
     * @param res
     * @return
     */
    public boolean HasParent(IMediaResource res) {
        return res.getParent() != null;
    }

    /**
     * Returns the Sage Sources (ie, top level folders) for the given media
     * masks
     * <p/>
     * Internal Use
     *
     * @param mediaMask SageTV media mask (B D V T M P L)
     * @return
     */
    public IMediaFolder GetSageSourcesMediaFolder(boolean combine, String mediaMask) {
        return new CombinedMediaFolder(new SageSourcesMediaFolder(mediaMask, mediaMask), combine);
    }

    /**
     * Return the Folder for the given object, or null, if not folder exists
     *
     * @param Object folder or filer object
     * @return IMediaFolder or null
     */
    public IMediaFolder GetFolder(Object res) {
        if (res == null)
            return null;
        if (res instanceof IMediaFolder) {
            return (IMediaFolder) res;
        } else if (res instanceof IMediaFile) {
            return ((IMediaFile) res).getParent();
        } else {
            return null;
        }
    }

    /**
     * Use with caution. This call will force all views to be fully reloaded.
     */
    public void ReloadViews() {
        Phoenix.getInstance().getVFSManager().loadConfigurations();
    }

    /**
     * Reloads the MediaTitles.xml file that is used to match Media Titles to
     * their metadata provider id
     */
    public void ReloadMediaTitles() {
        Phoenix.getInstance().getMediaTitlesManager().loadConfigurations();
    }

    /**
     * @param keywords
     * @param type     one of Airings, MediaFiles, TVFiles
     * @param options  json object map of options
     * @return
     */
    public IMediaFolder Search(String keywords, String type, Map options) {
        Search s = new Search();
        s.setSearchString(keywords);
        s.setSearchType(type);

        if (options != null) {
            String fields[] = (String[]) options.get("fields");
            if (fields != null) {
                s.setFields(fields);
            }
        }

        try {
            Object results[] = (Object[]) s.doSearch();
            return new MediaFilesMediaFolder(null, results, keywords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Performs a quick search against the video media files
     *
     * @param search
     * @return
     */
    public IMediaFolder SearchMediaFiles(String search) {
        ElapsedTimer timer = new ElapsedTimer();
        try {
            IMediaFolder folder = GetMediaAsFolder(MediaFileAPI.GetMediaFiles("DBVT"), search);
            SearchVisitor sv = new SearchVisitor(search);
            folder.accept(sv, NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);

            VirtualMediaFolder vmf = new VirtualMediaFolder(search);
            for (IMediaResource r : sv.getFiles()) {
                vmf.addMediaResource(r);
            }

            if (vmf.getChildren().size() > 0) {
                log.info(String.format("Search for '%s' returned %s results in %sms", search, vmf.getChildren().size(),
                        timer.delta()));
            } else {
                log.info("Search for '" + search + "' failed in " + timer.delta() + "ms");
            }
            return vmf;
        } catch (Throwable pe) {
            throw new RuntimeException("Search Failed " + search, pe);
        }
    }

    /**
     * Returns the list of tags
     *
     * @param includeInvisible if true then ALL tags are returns, if false then ONLY VISIBLE
     *                         tags are returned
     * @return
     */
    public Set<String> GetTags(boolean includeInvisible) {
        return Phoenix.getInstance().getVFSManager().getTags(includeInvisible);
    }

    /**
     * Returns the label for this tag, or the tag itself if the label is null.
     *
     * @param tag
     * @return
     */
    public String GetTagLabel(String tag) {
        return Phoenix.getInstance().getVFSManager().getTagLabel(tag);
    }

    /**
     * Returns true if a tag is visible
     *
     * @param tag
     * @return
     */
    public boolean IsTagVisible(String tag) {
        return Phoenix.getInstance().getVFSManager().isTagVisible(tag);
    }

    /**
     * Shows/Hides a given tag
     *
     * @param tag
     * @param visible
     * @return
     */
    public void SetTagVisible(String tag, boolean visible) {
        Phoenix.getInstance().getVFSManager().setTagVisible(tag, visible);
    }

    /**
     * Cancels the playback of a mediafile. Should be called AFTER the user has
     * cancelled/stopped playback on a mediafile.
     *
     * @param file
     */
    public void CancelPlayback(IMediaFile file) {
        if (file instanceof HasPlayableUrl) {
            String url = ((HasPlayableUrl) file).getUrl();
            File destFile = Phoenix.getInstance().getUserCacheEntry("onlinevideos", url);
            Global.CancelBackgroundFileDownload(destFile);
            FileUtils.deleteQuietly(destFile);
        }
    }

    /**
     * Given the {@link IMediaResource} return the {@link Factory} that created
     * it.
     *
     * @param res
     * @return
     */
    public String GetFactoryId(IMediaResource res) {
        if (res == null)
            return null;
        if (res instanceof ViewFolder) {
            return ((ViewFolder) res).getViewFactory().getName();
        } else if (res.getParent() != null) {
            return GetFactoryId(res.getParent());
        }
        return null;
    }
}
