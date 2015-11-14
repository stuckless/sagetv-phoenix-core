package sagex.phoenix.vfs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.download.DownloadItem;
import sagex.phoenix.event.PhoenixEvent;
import sagex.phoenix.event.PhoenixEventID;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.factory.FactoryRegistry;
import sagex.phoenix.vfs.builder.VFSBuilder;
import sagex.phoenix.vfs.filters.FilterFactory;
import sagex.phoenix.vfs.groups.GroupingFactory;
import sagex.phoenix.vfs.sorters.SorterFactory;
import sagex.phoenix.vfs.views.ViewFactory;

public class VFSManager extends SystemConfigurationFileManager implements SystemConfigurationFileManager.ConfigurationFileVisitor {
	private Logger log = Logger.getLogger(VFSManager.class);

	private FactoryRegistry<GroupingFactory> VFSGroupFactory = new FactoryRegistry<GroupingFactory>("groups");
	private FactoryRegistry<SorterFactory> VFSSortFactory = new FactoryRegistry<SorterFactory>("sorts");
	private FactoryRegistry<FilterFactory> VFSFilterFactory = new FactoryRegistry<FilterFactory>("filters");
	private FactoryRegistry<ViewFactory> VFSViewFactory = new FactoryRegistry<ViewFactory>("views");
	private FactoryRegistry<Factory<IMediaFolder>> VFSSourceFactory = new FactoryRegistry<Factory<IMediaFolder>>("sources");
	private Map<String, Tag> tags = new TreeMap<String, Tag>();

	private Exception lastError;

	public VFSManager(File systemDir, File userDir) {
		super(systemDir, userDir, new SuffixFileFilter(".xml", IOCase.INSENSITIVE));

		Comparator<File> nameComparator = new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				// we want vfs.xml to loaded first, always.
				if (f1.getName().equals("x-vfs.xml"))
					return Integer.MIN_VALUE;
				if (f2.getName().equals("x-vfs.xml"))
					return Integer.MAX_VALUE;
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		};

		getSystemFiles().setNameComparator(nameComparator);
	}

	public void clear() {
		VFSGroupFactory.clear();
		VFSSortFactory.clear();
		VFSFilterFactory.clear();
		VFSViewFactory.clear();
		VFSSourceFactory.clear();
	}

	/**
	 * @return the vFSGroupFactory
	 */
	public FactoryRegistry<GroupingFactory> getVFSGroupFactory() {
		return VFSGroupFactory;
	}

	/**
	 * @return the vFSSortFactory
	 */
	public FactoryRegistry<SorterFactory> getVFSSortFactory() {
		return VFSSortFactory;
	}

	/**
	 * @return the vFSFilterFactory
	 */
	public FactoryRegistry<FilterFactory> getVFSFilterFactory() {
		return VFSFilterFactory;
	}

	/**
	 * @return the vFSViewFactory
	 */
	public FactoryRegistry<ViewFactory> getVFSViewFactory() {
		return VFSViewFactory;
	}

	/**
	 * @return the vFSSourceFactory
	 */
	public FactoryRegistry<Factory<IMediaFolder>> getVFSSourceFactory() {
		return VFSSourceFactory;
	}

	/**
	 * Adds a new VFS tag to the system. If the tag exists, then it will be
	 * updated with the new label, provided the new label is not empty.
	 * 
	 * @param tag
	 * @param label
	 * @param visible
	 */
	public void addTag(String tag, String label, String visible) {
		if (!tags.containsKey(tag)) {
			tags.put(tag, new Tag(tag, label, (visible == null) ? true : BooleanUtils.toBoolean(visible)));
		} else {
			Tag t = tags.get(tag);
			if (label != null) {
				t.setLabel(label);
			}
			if (visible != null) {
				t.setVisible(BooleanUtils.toBoolean(visible));
			}
			tags.put(tag, t);
		}
	}

	/**
	 * Retuns a set of the all the knowns tags
	 * 
	 * @return
	 */
	public Set<String> getTags(boolean includeInvisible) {
		if (includeInvisible) {
			return tags.keySet();
		} else {
			Set<String> s = new TreeSet<String>();
			for (Tag t : tags.values()) {
				if (t.isVisible()) {
					s.add(t.getTag());
				}
			}
			return s;
		}
	}

	/**
	 * Gets the label for the given tags
	 * 
	 * @param tag
	 * @return
	 */
	public String getTagLabel(String tag) {
		Tag t = tags.get(tag);
		return StringUtils.isEmpty(t.getLabel()) ? tag : t.getLabel();
	}

	/**
	 * Returns a {@link Map} of the known tags.
	 * 
	 * @return
	 */
	public Map<String, Tag> getTagMap() {
		return tags;
	}

	/**
	 * Returns true if the tag is visible
	 * 
	 * @param tag
	 * @return
	 */
	public boolean isTagVisible(String tag) {
		Tag t = tags.get(tag);
		return t == null || t.isVisible();
	}

	/**
	 * Sets the tag to be visible or not
	 * 
	 * @param tag
	 * @param visible
	 */
	public void setTagVisible(String tag, boolean visible) {
		Tag t = tags.get(tag);
		if (t != null) {
			t.setVisible(visible);
		}
	}

	@Override
	public void visitConfigurationFile(ConfigurationType type, File file) {
		log.info("Processing VFS File: " + file);
		try {
			VFSBuilder.registerVFSSources(file, getSystemFiles().getDir(), this);
		} catch (Exception e) {
			lastError = e;
			log.warn("Failed to load vfs sources from: " + file
					+ "; Dynamic Views/Sources will most likely be empty until the problem is resolved.", e);
			Phoenix.fireError("Error in VFS File: " + file + "; " + e.getMessage(), e);
		}
	}

	@Override
	@PhoenixEvent(PhoenixEventID.VFS_Reload)
	public void loadConfigurations() {
		downloadMissingViews();
		clear();

		File cachedFile = new File(Phoenix.getInstance().getUserCacheDir(), "vfs-cached.xml");
		rebuildCache(cachedFile);
	}

	public class MaxLastModifiedVisitor implements SystemConfigurationFileManager.ConfigurationFileVisitor {
		long lastModified = 0;

		@Override
		public void visitConfigurationFile(ConfigurationType type, File file) {
			lastModified = Math.max(lastModified, file.lastModified());
		}
	}

	private void rebuildCache(File cachedFile) {
		// MaxLastModifiedVisitor vis = new MaxLastModifiedVisitor();
		// accept(vis);
		// long lastMod = vis.lastModified;

		// disabling caching for now
		long lastMod = Long.MAX_VALUE;

		if (!cachedFile.exists() || lastMod > cachedFile.lastModified()) {
			log.info("Rebuilding VFS Cache " + cachedFile);

			final VFSOrganizer organizer = new VFSOrganizer(getSystemFiles().getDir());
			accept(new ConfigurationFileVisitor() {
				@Override
				public void visitConfigurationFile(ConfigurationType type, File file) {
					try {
						// only organize xml files
						if (file.getName().toLowerCase().endsWith(".xml")) {
							organizer.organize(file);
						}
					} catch (Exception e) {
						log.warn("VFS FILE: " + file + " HAS Validataion Errors and will not be included.", e);
					}
				}
			});

			try {
				FileWriter fw = new FileWriter(cachedFile);
				organizer.writeTo(fw);
				fw.flush();
				fw.close();
				log.info("New VFS Cache Written to " + cachedFile);

				// now load the VFS from cache
				log.info("Loading VFS entries from " + cachedFile);
				visitConfigurationFile(ConfigurationType.System, cachedFile);
			} catch (IOException e) {
				log.warn("Failed to Write VFS Cache File: " + cachedFile, e);
			}
		} else {
			log.info("VFS Cache is not modified so, we'll reload the cache.");
			visitConfigurationFile(ConfigurationType.System, cachedFile);
		}
	}

	public Exception getLastError() {
		return lastError;
	}

	public void clearErrors() {
		lastError = null;
	}

	private void downloadMissingViews() {
		// don't download views when in stand-alone mode, assume that
		// the stand-alone packaging includes everything.
		if (Phoenix.isStandalone())
			return;

		String dlurl = "https://raw.githubusercontent.com/stuckless/sagetv-phoenix-core/master/STVs/Phoenix/vfs/x-vfs.xml";
		File masterXml = new File(Phoenix.getInstance().getVFSManager().getSystemFiles().getDir(), "x-vfs.xml");
		log.info("Checking for core Phoenix Views: " + masterXml);
		if (!masterXml.exists() || masterXml.length() == 0) {
			// download the missing views
			try {
				log.warn("Downloading x-vfs.xml view from Phoenix, since it appears to be missing");
				DownloadItem item = new DownloadItem(new URL(dlurl), masterXml);
				item.setOverwrite(false);
				item.setRetries(0);
				item.setMaxReties(1);
				// note this is set here because otherwise we get a race
				// condition on startup since the vfs download
				// happens before phoenix is completely initialized.
				item.setUserAgent("Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.1 (KHTML, like Gecko) Ubuntu/11.10 Chromium/14.0.835.202 Chrome/14.0.835.202 Safari/535.1");
				item.setTimeout(3000);

				// wait up to 10 seconds for the file to download
				Phoenix.getInstance().getDownloadManager().downloadAndWait(item, 10000);
			} catch (Throwable e) {
				e.printStackTrace();
				Phoenix.fireError("Unable to download/update the phoenix views... Phoenix may not work correctly.", e);
			}
		} else {
			log.info("Found core Phoenix Views file (no need to download update)" + masterXml);
		}
	}
}
