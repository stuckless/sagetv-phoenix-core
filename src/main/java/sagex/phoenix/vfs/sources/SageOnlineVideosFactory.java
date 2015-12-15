package sagex.phoenix.vfs.sources;

import org.apache.commons.lang.StringUtils;
import sagex.UIContext;
import sagex.api.WidgetAPI;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.util.PropertiesUtils;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.VirtualOnlineMediaFolder;
import sagex.phoenix.vfs.ov.XmlFolder;
import sagex.phoenix.vfs.ov.XmlOptions;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class SageOnlineVideosFactory extends Factory<IMediaFolder> {

    public SageOnlineVideosFactory() {
        super();
        addOption(new ConfigurableOption("videodir", "Online Videos Directory", null, DataType.directory));
    }

    @Override
    public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
        UIContext ctx = UIContext.getCurrentContext();

        File onlineVideosDir = null;
        String baseDir = getOption("videodir", configurableOptions).getString(null);

        if (StringUtils.isEmpty(baseDir)) {
            baseDir = WidgetAPI.GetCurrentSTVFile(ctx);
            if (baseDir == null) {
                baseDir = "STVs/SageTV7";
            } else {
                baseDir = new File(baseDir).getParent();
            }
            onlineVideosDir = new File(baseDir, "OnlineVideos");
        } else {
            onlineVideosDir = new File(baseDir);
        }

        if (!onlineVideosDir.exists()) {
            log.warn("Missing Online Videos Dir: " + onlineVideosDir);
            return null;
        }

        log.info("Loading OnlineVideoLinks.properties");
        File videoLinks = new File(onlineVideosDir, "OnlineVideoLinks.properties");
        Properties links = new Properties();
        try {
            PropertiesUtils.load(links, videoLinks);
        } catch (IOException e) {
            log.warn("Failed to load online videos files: " + videoLinks);
            return null;
        }

        log.info("Loading OnlineVideoUIText.properties");
        File labelsFile = new File(onlineVideosDir, "OnlineVideoUIText.properties");
        Properties labels = new Properties();
        try {
            PropertiesUtils.load(labels, labelsFile);
        } catch (IOException e) {
            log.warn("Failed to load Online Videos Labels: " + labelsFile);
            return null;
        }

        String sourcesStr = links.getProperty("Sources");
        if (StringUtils.isEmpty(sourcesStr)) {
            log.warn("No Sources defined for: " + videoLinks);
            return null;
        }

        log.info("Processing Sources: " + sourcesStr);
        VirtualOnlineMediaFolder folder = new VirtualOnlineMediaFolder(null, "SageOnlineVideos", sourcesStr, getLabel());
        String sources[] = sourcesStr.split("\\s*,\\s*");
        if (sources == null || sources.length == 0) {
            log.warn("No Sources for " + sourcesStr);
            return null;
        }
        for (String source : sources) {
            log.debug("Adding online video source: " + source);
            VirtualOnlineMediaFolder child = new VirtualOnlineMediaFolder(folder, labels.getProperty("Source/" + source
                    + "/ShortName", source));
            folder.addMediaResource(child);
            child.setThumbnail(labels.getProperty("Source/" + source + "/ThumbURL", null));

            // addin the pre/post fixes, if available
            String urlPrefix = links.getProperty(source + "/URLPrefix");
            String urlPostfix = links.getProperty(source + "/URLPostfix");

            String cats[] = getCategories(links, source);
            if (cats != null) {
                for (String cat : cats) {
                    loadCategory(child, cat, links, labels, urlPrefix, urlPostfix);
                }
            }
        }

        // now iterate all xPostCast items and build up those...
        for (Object k : links.keySet()) {
            String key = (String) k;
            if (key.startsWith("xFeedPodcast/")) {
                loadFeed(folder, key, links, labels);
            }
        }

        return folder;
    }

    private void loadFeed(VirtualOnlineMediaFolder folder, String key, Properties links, Properties labels) {
        try {
            String rssdata = links.getProperty(key);
            log.debug("Loadding feed " + rssdata);
            if (StringUtils.isEmpty(rssdata)) {
                log.warn("Invalid RSS Entry: " + key);
                return;
            }

            String keyParts[] = key.split("/");
            String feedId = keyParts[1];

            // rssdata looks like, cata,catb,catc;url
            String fields[] = rssdata.split("\\s*;\\s*");
            String catStr = fields[0];
            String url = fields[1];
            String cats[] = catStr.split("\\s*,\\s*");

            for (String cat : cats) {
                // skip this one, don't know why it exists
                if ("xFlagTitleNone".equals(cat))
                    continue;

                String catName = getTitle(labels, cat);
                log.debug("adding folder " + catName);
                VirtualOnlineMediaFolder vmf = (VirtualOnlineMediaFolder) folder.getChild(catName);
                if (vmf == null) {
                    vmf = createFolder(folder, cat, links, labels);
                    folder.addMediaResource(vmf);
                }

                XmlOptions opts = new XmlOptions();
                opts.setFeedUrl(url);
                XmlFolder rss = new XmlFolder(vmf, getTitle(labels, feedId), url);
                rss.setThumbnail(getThumb(labels, feedId));
                vmf.addMediaResource(rss);
            }
        } catch (Exception e) {
            log.warn("Failed to process feed: " + key, e);
        }
    }

    private String getTitle(Properties labels, String key) {
        return labels.getProperty("Source/" + key + "/ShortName", labels.getProperty("Category/" + key + "/ShortName", key));
    }

    private String getThumb(Properties labels, String key) {
        return labels.getProperty("Source/" + key + "/ThumbURL", labels.getProperty("Category/" + key + "/ThumbURL", null));
    }

    private VirtualOnlineMediaFolder createFolder(VirtualOnlineMediaFolder parent, String cat, Properties links, Properties labels) {
        log.debug("Creating Virtual Online Folder: " + cat);
        String catName = getTitle(labels, cat);
        VirtualOnlineMediaFolder vmf = new VirtualOnlineMediaFolder(parent, catName);
        return vmf;
    }

    private void loadCategory(VirtualOnlineMediaFolder parent, String cat, Properties links, Properties labels, String urlPrefix,
                              String urlPostfix) {
        try {
            log.info("Adding Category: " + cat);
            String urlContext = links.getProperty(cat + "/URLContext");
            String label = getTitle(labels, cat);
            String thumb = getThumb(labels, cat);
            String rssUrl = "";
            if (urlPrefix != null)
                rssUrl = urlPrefix;
            rssUrl += urlContext;
            if (urlPostfix != null) {
                if (urlPostfix.endsWith("=")) {
                    // fix the youtube url postfix
                    urlPostfix += "1";
                }
                rssUrl += urlPostfix;
            }
            log.info("Adding RSS Feed: " + rssUrl + "; " + label);

            XmlFolder rss = new XmlFolder(parent, label, rssUrl);
            rss.setThumbnail(thumb);
            parent.addMediaResource(rss);
        } catch (Exception e) {
            log.warn("Failed to load category: " + cat, e);
        }
    }

    private String[] getCategories(Properties props, String base) {
        String s = props.getProperty(base + "/Categories");
        if (StringUtils.isEmpty(s))
            return null;
        return s.split("\\s*,\\s*");
    }
}
